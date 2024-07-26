package io.quarkiverse.chappie.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import io.quarkus.builder.Version;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.ConsoleStateManager;
import io.quarkus.deployment.dev.ExceptionNotificationBuildItem;
import io.quarkus.deployment.dev.testing.MessageFormat;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.deployment.pkg.builditem.OutputTargetBuildItem;
import io.vertx.core.Vertx;

class ChappieProcessor {

    private static final String FEATURE = "chappie";
    static volatile ConsoleStateManager.ConsoleContext chappieConsoleContext;

    static volatile AIAssistant aiAssistant;

    @BuildStep(onlyIf = IsDevelopment.class)
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep(onlyIf = IsDevelopment.class)
    void setupExceptionHandler(BuildProducer<ExceptionNotificationBuildItem> exceptionNotificationProducer,
            CurateOutcomeBuildItem curateOutcomeBuildItem,
            OutputTargetBuildItem outputTargetBuildItem,
            ChappieConfig chappieConfig) {

        Path srcMainJava = ChappieProcessorHelper.getSourceRoot(curateOutcomeBuildItem.getApplicationModel(),
                outputTargetBuildItem.getOutputDirectory());

        final AtomicReference<LastException> lastExceptionReference = ChappieProcessorHelper
                .getLastException(exceptionNotificationProducer);

        if (chappieConsoleContext == null) {
            chappieConsoleContext = ConsoleStateManager.INSTANCE.createContext("Chappie");
        }

        if (aiAssistant == null) {
            aiAssistant = new AIAssistant(Version.getVersion(), chappieConfig.apiKey, chappieConfig.modelName);
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
                                LastException lastException = lastExceptionReference.get();
                                if (lastException == null) {
                                    return "none";
                                }
                                return lastException.throwable().getMessage();
                            }
                        }), new Runnable() {
                            @Override
                            public void run() {
                                LastException lastException = lastExceptionReference.get();
                                if (lastException == null) {
                                    return;
                                }

                                String sourceString = getRelevantSource(srcMainJava, lastException.stackTraceElement());

                                String stacktraceString = lastException.getStackTraceString();

                                System.out.println(
                                        "Chappie\n===============\nAssisting with the exception, please wait");

                                long timer = vertx.setPeriodic(800, id -> System.out.print("."));

                                CompletableFuture<SuggestedFix> futureFix = aiAssistant.helpFix(stacktraceString, sourceString);

                                futureFix.thenAccept(suggestedFix -> {
                                    vertx.cancelTimer(timer);
                                    System.out.println("\n\n" + suggestedFix.response());
                                    System.out.println("\n\n" + suggestedFix.explanation());
                                    System.out.println("\n------ Diff ------ ");
                                    System.out.println("\n\n" + suggestedFix.diff());
                                    System.out.println("\n------ Suggested source ------ ");
                                    System.out.println("\n\n" + suggestedFix.suggestedSource());
                                }).exceptionally(throwable -> {
                                    // Handle any errors
                                    System.out.println("\n\nCould not get a response from ai due the this exception:");
                                    throwable.printStackTrace();
                                    return null;
                                });

                            }
                        }));

    }

    private String getRelevantSource(Path srcMainJava, StackTraceElement stackTraceElement) {
        if (stackTraceElement != null) {
            String className = stackTraceElement.getClassName();
            String file = stackTraceElement.getFileName();
            if (className.contains(".")) {
                file = className.substring(0, className.lastIndexOf('.') + 1).replace('.',
                        File.separatorChar)
                        + file;
            }

            Path filePath = srcMainJava.resolve(file);

            try {
                return Files.readString(filePath);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return null;
    }

}
