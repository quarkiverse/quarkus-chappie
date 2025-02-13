package io.quarkiverse.chappie.deployment.exception;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ChappiePageBuildItem;
import io.quarkiverse.chappie.deployment.ContentIO;
import io.quarkiverse.chappie.deployment.workspace.WorkspaceBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.dev.ExceptionNotificationBuildItem;
import io.quarkus.deployment.dev.ai.AIBuildItem;
import io.quarkus.deployment.dev.ai.AIClient;
import io.quarkus.deployment.dev.ai.ExceptionOutput;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.runtime.logging.DecorateStackUtil;
import io.quarkus.vertx.http.deployment.ErrorPageActionsBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

@BuildSteps(onlyIf = IsDevelopment.class)
class ExceptionDevUIProcessor {
    private static final String EXCEPTION_TITLE = "Exception help";

    @BuildStep
    void createBroadcasters(BuildProducer<BroadcastsBuildItem> broadcastsProducer) {
        BroadcastProcessor<LastException> leb = BroadcastProcessor.create();
        broadcastsProducer.produce(new BroadcastsBuildItem(leb));
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep
    void setupBroadcaster(BuildProducer<ExceptionNotificationBuildItem> exceptionNotificationProducer,
            BroadcastsBuildItem broadcastsBuildItem,
            LastExceptionBuildItem lastExceptionBuildItem,
            WorkspaceBuildItem workspaceBuildItem) {

        exceptionNotificationProducer
                .produce(new ExceptionNotificationBuildItem(new BiConsumer<Throwable, StackTraceElement>() {
                    @Override
                    public void accept(Throwable throwable, StackTraceElement stackTraceElement) {
                        String decorateString = DecorateStackUtil.getDecoratedString(stackTraceElement,
                                workspaceBuildItem.getPaths());
                        LastException lastException = new LastException(stackTraceElement, throwable, decorateString);
                        broadcastsBuildItem.getLastExceptionBroadcastProcessor().onNext(lastException);
                        lastExceptionBuildItem.getLastException().set(lastException);
                    }
                }));
    }

    @BuildStep
    void addActionToErrorPage(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<ErrorPageActionsBuildItem> errorPageActionsProducer,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {
        if (chappieAvailable.isPresent()) {
            String url = nonApplicationRootPathBuildItem.resolvePath(
                    "dev-ui/io.quarkiverse.chappie.quarkus-chappie/" + EXCEPTION_TITLE.replace(" ", "-").toLowerCase());
            errorPageActionsProducer.produce(new ErrorPageActionsBuildItem("Get help with this", url + "?autoSuggest=true"));
        }
    }

    @BuildStep
    void exceptionPage(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<ChappiePageBuildItem> chappiePageBuildItem) {
        if (chappieAvailable.isPresent()) {
            chappiePageBuildItem.produce(new ChappiePageBuildItem(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:bug")
                    .title(EXCEPTION_TITLE)
                    .componentLink("qwc-chappie-exception.js")));
        }
    }

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            WorkspaceBuildItem workspaceBuildItem,
            AIBuildItem aiBuildItem,
            BroadcastsBuildItem broadcastsBuildItem,
            LastExceptionBuildItem lastExceptionBuildItem,
            LastSolutionBuildItem lastSolutionBuildItem) {

        if (chappieAvailable.isPresent()) {

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
                    Path sourcePath = DecorateStackUtil.findAffectedPath(lastException.stackTraceElement().getClassName(),
                            workspaceBuildItem.getPaths());

                    AIClient aiClient = aiBuildItem.getAIClient();
                    if (sourcePath != null) {
                        String sourceString = ContentIO.readContents(sourcePath);
                        String stacktraceString = lastException.getStackTraceString();
                        CompletableFuture<ExceptionOutput> response = aiClient
                                .exception("The stacktrace is a Java exception", stacktraceString, sourcePath.toString(),
                                        sourceString);
                        response.thenAccept((suggestedFix) -> {
                            lastSolutionBuildItem.getLastSolution().set(suggestedFix);
                            lastSolutionBuildItem.getPath().set(sourcePath);
                        });
                        return response;
                    } // TODO: We can still attempt this if we could not find a relevant file
                }
                return null;
            });

            // This suggest fix based on exception and code
            buildItemActions.addAction("applyFix", code -> {
                ExceptionOutput exceptionOutput = (ExceptionOutput) lastSolutionBuildItem.getLastSolution().get();
                Path path = lastSolutionBuildItem.getPath().get();
                if (exceptionOutput != null && path != null) {
                    lastSolutionBuildItem.getLastSolution().set(null);
                    lastSolutionBuildItem.getPath().set(null);
                    return ContentIO.writeContent(path, exceptionOutput.manipulatedContent());
                }
                return null;
            });

            buildTimeActionProducer.produce(buildItemActions);
        }
    }

}
