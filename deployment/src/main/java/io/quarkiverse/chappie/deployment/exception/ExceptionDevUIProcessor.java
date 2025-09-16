package io.quarkiverse.chappie.deployment.exception;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import io.quarkiverse.chappie.deployment.ContentIO;
import io.quarkiverse.chappie.runtime.dev.ExceptionOutput;
import io.quarkus.assistant.deployment.spi.AssistantConsoleBuildItem;
import io.quarkus.assistant.deployment.spi.AssistantPageBuildItem;
import io.quarkus.assistant.runtime.dev.Assistant;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.dev.ExceptionNotificationBuildItem;
import io.quarkus.deployment.dev.testing.MessageFormat;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.workspace.WorkspaceBuildItem;
import io.quarkus.runtime.logging.DecorateStackUtil;
import io.quarkus.vertx.http.deployment.ErrorPageActionsBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

@BuildSteps(onlyIf = IsLocalDevelopment.class)
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
    void addActionToErrorPage(BuildProducer<ErrorPageActionsBuildItem> errorPageActionsProducer,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {

        String url = nonApplicationRootPathBuildItem.resolvePath(
                "dev-ui/quarkus-chappie/" + EXCEPTION_TITLE.replace(" ", "-").toLowerCase());
        errorPageActionsProducer.produce(new ErrorPageActionsBuildItem("Get help with this", url));
    }

    @BuildStep
    void exceptionPage(BuildProducer<AssistantPageBuildItem> assistantPageBuildItem) {
        assistantPageBuildItem.produce(new AssistantPageBuildItem(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:bug")
                .title(EXCEPTION_TITLE)
                .componentLink("qwc-chappie-exception.js")));
    }

    @BuildStep
    void createBuildTimeActions(BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            BuildProducer<AssistantConsoleBuildItem> assistantConsoleProducer,
            WorkspaceBuildItem workspaceBuildItem,
            BroadcastsBuildItem broadcastsBuildItem,
            LastExceptionBuildItem lastExceptionBuildItem,
            LastSolutionBuildItem lastSolutionBuildItem) {

        BuildTimeActionBuildItem buildItemActions = new BuildTimeActionBuildItem();

        // This gets the last know exception. For initial load.
        buildItemActions.actionBuilder()
                .methodName("getLastException")
                .description("Gets the last known exception that happend")
                .function(ignored -> {
                    LastException lastException = lastExceptionBuildItem.getLastException().get();
                    if (lastException != null) {
                        return lastException;
                    }
                    return null;
                })
                .build();

        // This streams exceptions as they happen
        buildItemActions.subscriptionBuilder()
                .methodName("streamException")
                .function(ignored -> {
                    return broadcastsBuildItem.getLastExceptionPublisher();
                })
                .build();

        // This suggest fix based on exception and code
        buildItemActions.actionBuilder()
                .methodName("suggestFix")
                .assistantFunction((a, ignored) -> {
                    Assistant assistant = (Assistant) a;
                    LastException lastException = lastExceptionBuildItem.getLastException().get();
                    if (lastException != null) {
                        Path sourcePath = DecorateStackUtil.findAffectedPath(lastException.stackTraceElement().getClassName(),
                                workspaceBuildItem.getPaths());
                        if (sourcePath != null) {
                            String stacktraceString = lastException.getStackTraceString();
                            CompletionStage<ExceptionOutput> response = assistant.exceptionBuilder()
                                    .userMessage("The stacktrace is a Java exception")
                                    .stacktrace(stacktraceString)
                                    .path(sourcePath)
                                    .explain();
                            response.thenAccept((suggestedFix) -> {
                                lastSolutionBuildItem.getLastSolution().set(suggestedFix);
                                lastSolutionBuildItem.getPath().set(sourcePath);
                            });
                            return response;
                        } // TODO: We can still attempt this if we could not find a relevant file
                    }
                    return null;
                })
                .build();

        // This suggest fix based on exception and code
        buildItemActions.actionBuilder()
                .methodName("applyFix")
                .function(code -> {
                    ExceptionOutput exceptionOutput = (ExceptionOutput) lastSolutionBuildItem.getLastSolution().get();
                    Path path = lastSolutionBuildItem.getPath().get();
                    if (exceptionOutput != null && path != null) {
                        lastSolutionBuildItem.getLastSolution().set(null);
                        lastSolutionBuildItem.getPath().set(null);
                        return ContentIO.writeContent(path, exceptionOutput.manipulatedContent());
                    }
                    return null;
                })
                .build();

        buildTimeActionProducer.produce(buildItemActions);

        // Also allow user to do this in the console

        assistantConsoleProducer.produce(AssistantConsoleBuildItem.builder()
                .description("Help with the latest exception")
                .key('a')
                .colorSupplier(() -> MessageFormat.RED)
                .stateSupplier(() -> getInitMessage(lastExceptionBuildItem))
                .function((Assistant assistant) -> {
                    LastException lastException = lastExceptionBuildItem.getLastException().get();
                    if (lastException == null) {
                        return CompletableFuture.completedFuture("");
                    }

                    Path sourcePath = DecorateStackUtil.findAffectedPath(
                            lastException.stackTraceElement().getClassName(),
                            workspaceBuildItem.getPaths());
                    String stacktraceString = lastException.getStackTraceString();

                    return assistant.exceptionBuilder()
                            .userMessage("The stacktrace is a Java exception")
                            .stacktrace(stacktraceString)
                            .path(sourcePath)
                            .explain()
                            .thenApply((Object output) -> {
                                ExceptionOutput exceptionOutput = (ExceptionOutput) output;

                                return "\n\n" + exceptionOutput.response() +
                                        "\n\n" + exceptionOutput.explanation() +
                                        "\n------ Diff ------ " +
                                        "\n\n" + exceptionOutput.diff() +
                                        "\n------ Suggested source ------ " +
                                        "\n\n" + exceptionOutput.manipulatedContent();
                            });
                })
                .build());
    }

    private String getInitMessage(LastExceptionBuildItem lastExceptionBuildItem) {
        LastException lastException = lastExceptionBuildItem.getLastException().get();
        if (lastException == null) {
            return "none";
        }
        return lastException.throwable().getMessage();
    }

}
