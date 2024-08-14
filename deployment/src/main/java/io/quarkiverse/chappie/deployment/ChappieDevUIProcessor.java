package io.quarkiverse.chappie.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import io.quarkiverse.chappie.deployment.ollama.OllamaBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.dev.ExceptionNotificationBuildItem;
import io.quarkus.deployment.logging.LoggingDecorateBuildItem;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.runtime.logging.DecorateStackUtil;
import io.quarkus.vertx.http.deployment.ErrorPageActionsBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

@BuildSteps(onlyIf = IsDevelopment.class)
class ChappieDevUIProcessor {
    private static final String EXCEPTION_TITLE = "Help with the latest exception";

    static volatile Path srcMainJava;
    static volatile List<String> knownClasses;

    @BuildStep
    void createBroadcasters(BuildProducer<BroadcastsBuildItem> broadcastsProducer,
            Optional<AssistantBuildItem> maybeAssistantBuildItem) {
        if (maybeAssistantBuildItem.isPresent()) {
            BroadcastProcessor<LastException> leb = BroadcastProcessor.create();
            BroadcastProcessor<Status> sb = BroadcastProcessor.create();
            broadcastsProducer.produce(new BroadcastsBuildItem(leb, sb));
        }
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep
    void setupBroadcaster(BuildProducer<ExceptionNotificationBuildItem> exceptionNotificationProducer,
            Optional<AssistantBuildItem> maybeAssistantBuildItem,
            BroadcastsBuildItem broadcastsBuildItem,
            LastExceptionBuildItem lastExceptionBuildItem) {
        if (maybeAssistantBuildItem.isPresent()) {
            exceptionNotificationProducer
                    .produce(new ExceptionNotificationBuildItem(new BiConsumer<Throwable, StackTraceElement>() {
                        @Override
                        public void accept(Throwable throwable, StackTraceElement stackTraceElement) {
                            String decorateString = DecorateStackUtil.getDecoratedString(throwable, srcMainJava, knownClasses);
                            LastException lastException = new LastException(stackTraceElement, throwable, decorateString);
                            broadcastsBuildItem.getLastExceptionBroadcastProcessor().onNext(lastException);
                            lastExceptionBuildItem.getLastException().set(lastException);
                        }
                    }));
        }
    }

    @BuildStep
    void addActionToErrorPage(BuildProducer<ErrorPageActionsBuildItem> errorPageActionsProducer,
            Optional<AssistantBuildItem> maybeAssistantBuildItem,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {
        if (maybeAssistantBuildItem.isPresent()) {
            String url = nonApplicationRootPathBuildItem.resolvePath(
                    "dev-ui/io.quarkiverse.chappie.quarkus-chappie/" + EXCEPTION_TITLE.replace(" ", "-").toLowerCase());
            errorPageActionsProducer.produce(new ErrorPageActionsBuildItem("Get help with this", url));
        }
    }

    @BuildStep
    void pages(BuildProducer<CardPageBuildItem> cardPageProducer,
            Optional<AssistantBuildItem> maybeAssistantBuildItem,
            Optional<OllamaBuildItem> maybeOllamaBuildItem,
            ChappieConfig config) {

        if (maybeAssistantBuildItem.isPresent()) {
            CardPageBuildItem chappieCard = new CardPageBuildItem();

            if (config.llm().get().equals(LLM.ollama) && maybeOllamaBuildItem.isEmpty()) { // Ollama is not installed
                chappieCard.addPage(Page.externalPageBuilder("Install Ollama")
                        .icon("font-awesome-solid:download")
                        .doNotEmbed(false)
                        .url("https://ollama.com/download"));
            }

            if (config.llm().get().equals(LLM.openai) ||
                    (config.llm().get().equals(LLM.ollama) && maybeOllamaBuildItem.isPresent())) {

                chappieCard.addPage(Page.webComponentPageBuilder()
                        .icon("font-awesome-solid:circle-question")
                        .title(EXCEPTION_TITLE)
                        .componentLink("qwc-chappie-exception.js"));

            }

            chappieCard.setCustomCard("qwc-chappie-custom-card.js");

            chappieCard.addBuildTimeData("llm", config.llm().get());
            if (config.llm().get().equals(LLM.openai)) {
                chappieCard.addBuildTimeData("modelName", config.openai().modelName());
            } else if (config.llm().get().equals(LLM.ollama)) {
                chappieCard.addBuildTimeData("modelName", config.ollama().modelName());
            }

            cardPageProducer.produce(chappieCard);
        } else {
            // TODO: Allow configuring chappie in Dev UI
        }
    }

    @BuildStep
    void createBuildTimeActions(BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            LoggingDecorateBuildItem loggingDecorateBuildItem,
            BroadcastsBuildItem broadcastsBuildItem,
            Optional<AssistantBuildItem> maybeAssistantBuildItem,
            LastExceptionBuildItem lastExceptionBuildItem,
            ChappieConfig chappieConfig) {

        if (maybeAssistantBuildItem.isPresent()) {
            AssistantBuildItem assistantBuildItem = maybeAssistantBuildItem.get();
            if (srcMainJava == null) {
                srcMainJava = loggingDecorateBuildItem.getSrcMainJava();
            }
            if (knownClasses == null) {
                knownClasses = loggingDecorateBuildItem.getKnowClasses();
            }
            BuildTimeActionBuildItem buildItemActions = new BuildTimeActionBuildItem();

            // This gets the last know exception. For initial load.
            buildItemActions.addAction("getLastException", ignored -> {
                LastException lastException = lastExceptionBuildItem.getLastException().get();
                if (lastException != null) {
                    return lastException;
                }
                return null;
            });

            // This streams exceptions as they happen
            buildItemActions.addSubscription("streamException", ignored -> {
                return broadcastsBuildItem.getLastExceptionPublisher();
            });

            // This suggest fix based on exception and code
            buildItemActions.addAction("suggestFix", ignored -> {
                LastException lastException = lastExceptionBuildItem.getLastException().get();
                if (lastException != null) {
                    StackTraceElement stackTraceElement = lastException.stackTraceElement();
                    String className = stackTraceElement.getClassName();
                    String file = stackTraceElement.getFileName();
                    if (className.contains(".")) {
                        file = className.substring(0, className.lastIndexOf('.') + 1).replace('.',
                                File.separatorChar)
                                + file;
                    }

                    try {
                        Path filePath = srcMainJava.resolve(file);
                        String sourceString = Files.readString(filePath);
                        String stacktraceString = lastException.getStackTraceString();

                        Assistant assistant = assistantBuildItem.getAssistant();

                        return assistant.suggestFix(stacktraceString, sourceString);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
                return null;
            });

            // This checks the status of the container
            buildItemActions.addAction("getStatus", ignored -> {
                //                boolean isStarting = assistantBuildItem.isContainerStarting();
                //                if (isStarting) {
                //
                //                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                //                        try {
                //                            while (assistantBuildItem.isContainerStarting()) {
                //                                Thread.sleep(1000);
                //                                broadcastsBuildItem.getStatusBroadcastProcessor().onNext(Status.starting);
                //                            }
                //                            broadcastsBuildItem.getStatusBroadcastProcessor().onNext(Status.started);
                //                        } catch (InterruptedException e) {
                //                            e.printStackTrace();
                //                            broadcastsBuildItem.getStatusBroadcastProcessor().onError(e);
                //                        }
                //                    });
                //                    return Status.starting;
                //                } else {
                //                    return Status.started;
                //                }
                return Status.started;
            });

            // This streams the status. Useful when starting up
            buildItemActions.addSubscription("streamStatus", ignored -> {
                return broadcastsBuildItem.getStatusPublisher();
            });

            // TODO: Get last suggested Fix

            buildTimeActionProducer.produce(buildItemActions);
        }
    }
}
