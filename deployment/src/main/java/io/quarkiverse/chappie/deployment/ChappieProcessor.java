package io.quarkiverse.chappie.deployment;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.ConsoleStateManager;
import io.quarkus.deployment.dev.testing.MessageFormat;
import io.quarkus.deployment.logging.LoggingDecorateBuildItem;
import io.vertx.core.Vertx;

/**
 * Main Chappie Processor. This create a few Build Items also used by the DevUI processor
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@BuildSteps(onlyIf = IsDevelopment.class)
class ChappieProcessor {
    static volatile ConsoleStateManager.ConsoleContext chappieConsoleContext;

    @BuildStep
    LastExceptionBuildItem createLastExceptionReference() {
        final AtomicReference<LastException> lastException = new AtomicReference<>();
        return new LastExceptionBuildItem(lastException);
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep(onlyIf = ChappieEnabled.class)
    FeatureBuildItem setupConsole(LastExceptionBuildItem lastExceptionBuildItem,
            ChappieClientBuildItem chappieClientBuildItem,
            LoggingDecorateBuildItem loggingDecorateBuildItem) {

        Path srcMainJava = loggingDecorateBuildItem.getSrcMainJava();

        if (chappieConsoleContext == null) {
            chappieConsoleContext = ConsoleStateManager.INSTANCE.createContext("Assistant");
        }

        Vertx vertx = Vertx.vertx();
        chappieConsoleContext.reset(
                new ConsoleCommand('a', "Help with the latest exception",
                        new ConsoleCommand.HelpState(new Supplier<String>() {
                            @Override
                            public String get() {
                                return MessageFormat.RED;
                            }
                        }, new Supplier<String>() {
                            @Override
                            public String get() {
                                LastException lastException = lastExceptionBuildItem.getLastException().get();
                                if (lastException == null) {
                                    return "none";
                                }
                                return lastException.throwable().getMessage();
                            }
                        }), new Runnable() {
                            @Override
                            public void run() {
                                LastException lastException = lastExceptionBuildItem.getLastException().get();
                                if (lastException == null) {
                                    return;
                                }

                                String sourceString = SourceCodeFinder.getSourceCode(srcMainJava,
                                        lastException.stackTraceElement());

                                String stacktraceString = lastException.getStackTraceString();

                                System.out.println(
                                        "===============\nAssisting with the exception, please wait");

                                long timer = vertx.setPeriodic(800, id -> System.out.print("."));

                                ChappieClient chappieClient = chappieClientBuildItem.getChappieClient();
                                Object[] params = ParameterCreator.forExceptionHelp(stacktraceString, sourceString);

                                CompletableFuture<Object> futureResult = chappieClient.executeRPC("exception#suggestfix",
                                        params);

                                futureResult.thenAccept(r -> {
                                    vertx.cancelTimer(timer);
                                    Map result = (Map) r;

                                    System.out.println("\n\n" + result.get("response"));
                                    System.out.println("\n\n" + result.get("explanation"));
                                    System.out.println("\n------ Diff ------ ");
                                    System.out.println("\n\n" + result.get("diff"));
                                    System.out.println("\n------ Suggested source ------ ");
                                    System.out.println("\n\n" + result.get("suggestedSource"));
                                }).exceptionally(throwable -> {
                                    // Handle any errors
                                    System.out.println("\n\nCould not get a response from ai due the this exception:");
                                    throwable.printStackTrace();
                                    return null;
                                });
                            }
                        }));

        return new FeatureBuildItem(Feature.FEATURE);
    }
}
