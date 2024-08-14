package io.quarkiverse.chappie.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import io.quarkiverse.chappie.deployment.ollama.OllamaAssistant;
import io.quarkiverse.chappie.deployment.openai.OpenAIAssistant;
import io.quarkus.builder.Version;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
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
    private final static Logger LOGGER = Logger.getLogger(ChappieProcessor.class);
    private static final String FEATURE = "chappie";

    static volatile ConsoleStateManager.ConsoleContext chappieConsoleContext;

    @BuildStep
    void createAssitant(BuildProducer<AssistantBuildItem> assistantProducer, ChappieConfig chappieConfig) {
        if (chappieConfig.llm().isPresent()) {
            LLM llm = chappieConfig.llm().get();

            if (llm.equals(LLM.openai)) {
                String apiKey = chappieConfig.openai().apiKey().orElseThrow();
                Assistant assistant = new OpenAIAssistant(Version.getVersion(), apiKey, chappieConfig.openai().modelName());
                assistantProducer.produce(new AssistantBuildItem(assistant));
            } else if (llm.equals(LLM.ollama)) {
                Assistant assistant = new OllamaAssistant(Version.getVersion(), chappieConfig.ollama().port(),
                        chappieConfig.ollama().modelName());
                assistantProducer.produce(new AssistantBuildItem(assistant));
            }
        } else {
            LOGGER.warn(
                    "Chappie is added, but not configured. You need to at least set quarkus.chappie.llm to get a working extension.");
        }
    }

    @BuildStep
    LastExceptionBuildItem createLastExceptionReference() {
        final AtomicReference<LastException> lastException = new AtomicReference<>();
        return new LastExceptionBuildItem(lastException);
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep
    FeatureBuildItem setupConsole(LastExceptionBuildItem lastExceptionBuildItem,
            LoggingDecorateBuildItem loggingDecorateBuildItem,
            Optional<AssistantBuildItem> maybeAssistantBuildItem) {

        Path srcMainJava = loggingDecorateBuildItem.getSrcMainJava();

        if (chappieConsoleContext == null) {
            chappieConsoleContext = ConsoleStateManager.INSTANCE.createContext("Chappie");
        }
        if (maybeAssistantBuildItem.isPresent()) {
            AssistantBuildItem assistantBuildItem = maybeAssistantBuildItem.get();
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

                                    String sourceString = getRelevantSource(srcMainJava, lastException.stackTraceElement());

                                    String stacktraceString = lastException.getStackTraceString();

                                    System.out.println(
                                            "Chappie\n===============\nAssisting with the exception, please wait");

                                    long timer = vertx.setPeriodic(800, id -> System.out.print("."));

                                    Assistant assistant = assistantBuildItem.getAssistant();

                                    CompletableFuture<SuggestedFix> futureFix = assistant.suggestFix(stacktraceString,
                                            sourceString);

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
        } else {
            // TODO: Allow enabling right here in the log ?
        }
        return new FeatureBuildItem(FEATURE);
    }

    /**
     * Read the source code that triggered the exception, so that we can send it to AI with the exception
     */
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
