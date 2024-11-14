package io.quarkiverse.chappie.deployment;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.chappie.deployment.devservice.ollama.OllamaBuildItem;
import io.quarkiverse.chappie.deployment.method.exception.LastException;
import io.quarkiverse.chappie.deployment.method.exception.LastExceptionBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.ConsoleStateManager;
import io.quarkus.deployment.dev.ai.AIBuildItem;
import io.quarkus.deployment.dev.ai.AIClient;
import io.quarkus.deployment.dev.testing.MessageFormat;
import io.quarkus.deployment.logging.LoggingDecorateBuildItem;
import io.vertx.core.Vertx;

/**
 * Main console Chappie Processor. This create a few Build Items also used by the DevUI processor
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@BuildSteps(onlyIf = IsDevelopment.class)
class ConsoleProcessor {
    static volatile ConsoleStateManager.ConsoleContext chappieConsoleContext;

    @BuildStep
    void chappieAvailable(BuildProducer<ChappieAvailableBuildItem> chappieAvailableProducer,
            ChappieConfig config,
            Optional<OllamaBuildItem> ollamaBuildItem) {

        if (config.openai().apiKey().isPresent() || ollamaBuildItem.isPresent()) {
            chappieAvailableProducer.produce(new ChappieAvailableBuildItem());
        }
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep
    void setupConsole(BuildProducer<FeatureBuildItem> featureProducer,
            LastExceptionBuildItem lastExceptionBuildItem,
            AIBuildItem aiBuildItem,
            LoggingDecorateBuildItem loggingDecorateBuildItem,
            Optional<ChappieAvailableBuildItem> chappieAvailable) {

        if (chappieAvailable.isPresent()) {

            Path srcMainJava = loggingDecorateBuildItem.getSrcMainJava();

            if (chappieConsoleContext == null) {
                chappieConsoleContext = ConsoleStateManager.INSTANCE.createContext("Assistant");
            }

            Vertx vertx = Vertx.vertx();
            chappieConsoleContext.reset(new ConsoleCommand('a', "Help with the latest exception",
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
                                    "=========================================\n"
                                            + "Assisting with the exception, please wait");

                            long timer = vertx.setPeriodic(800, id -> System.out.print("."));

                            AIClient aiClient = aiBuildItem.getAIClient();

                            CompletableFuture<String> response = aiClient
                                    .request("exception", Map.of("stacktrace", stacktraceString, "source", sourceString));

                            response.thenAccept((json) -> {
                                vertx.cancelTimer(timer);

                                try (StringReader r = new StringReader(json)) {
                                    ObjectMapper mapper = new ObjectMapper();
                                    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
                                    };

                                    try {
                                        HashMap<String, Object> result = mapper.readValue(r, typeRef);
                                        System.out.println("\n\n" + result.get("response"));
                                        System.out.println("\n\n" + result.get("explanation"));
                                        System.out.println("\n------ Diff ------ ");
                                        System.out.println("\n\n" + result.get("diff"));
                                        System.out.println("\n------ Suggested source ------ ");
                                        System.out.println("\n\n" + result.get("suggestedSource"));
                                    } catch (IOException ex) {
                                        System.out.println("\n\nCould not get a response from ai due the this exception:");
                                        ex.printStackTrace();
                                    }
                                }

                            });
                        }
                    }));
        }

        featureProducer.produce(new FeatureBuildItem(Feature.FEATURE));
    }
}
