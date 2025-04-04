package io.quarkiverse.chappie.deployment.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.Feature;
import io.quarkiverse.chappie.deployment.devservice.ollama.OllamaBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.ConsoleStateManager;
import io.quarkus.deployment.dev.assistant.Assistant;
import io.quarkus.deployment.dev.assistant.AssistantBuildItem;
import io.quarkus.deployment.dev.assistant.AssistantConsoleBuildItem;
import io.vertx.core.Vertx;

/**
 * Main console Chappie Processor. This create a few Build Items also used by the DevUI processor
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@BuildSteps(onlyIf = IsDevelopment.class)
class ChappieConsoleProcessor {
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
            AssistantBuildItem assistantBuildItem,
            List<AssistantConsoleBuildItem> assistantConsoleBuildItems) {

        if (!assistantConsoleBuildItems.isEmpty()) {
            if (chappieConsoleContext == null) {
                chappieConsoleContext = ConsoleStateManager.INSTANCE.createContext("Assistant");
            }

            Collections.sort(assistantConsoleBuildItems, Comparator.comparing(AssistantConsoleBuildItem::getDescription));

            Vertx vertx = Vertx.vertx();
            List<ConsoleCommand> consoleCommands = new ArrayList<>();
            for (AssistantConsoleBuildItem assistantConsoleBuildItem : assistantConsoleBuildItems) {
                if (assistantConsoleBuildItem.getConsoleCommand() != null) {
                    consoleCommands.add(assistantConsoleBuildItem.getConsoleCommand());
                } else {
                    ConsoleCommand.HelpState helpState = null;
                    if (assistantConsoleBuildItem.getStateSupplier() != null) {
                        helpState = new ConsoleCommand.HelpState(
                                assistantConsoleBuildItem.getColorSupplier(),
                                assistantConsoleBuildItem.getStateSupplier());
                    }

                    ConsoleCommand consoleCommand = new ConsoleCommand(assistantConsoleBuildItem.getKey(),
                            assistantConsoleBuildItem.getDescription(), helpState,
                            new Runnable() {
                                @Override
                                public void run() {
                                    System.out.println("""
                                            =========================================
                                            Talking to AI, please wait""");

                                    long timer = vertx.setPeriodic(800, id -> System.out.print("."));

                                    Assistant assistant = assistantBuildItem.getAssistant();

                                    if (assistantConsoleBuildItem.getFunction().isPresent()) {
                                        CompletionStage<?> response = assistantConsoleBuildItem.getFunction().get()
                                                .apply(assistant);
                                        printResponse(response, vertx, timer);
                                    } else {
                                        CompletionStage response = assistant
                                                .assist(assistantConsoleBuildItem.getSystemMessage(),
                                                        assistantConsoleBuildItem.getUserMessage(),
                                                        assistantConsoleBuildItem.getVariables());
                                        printResponse(response, vertx, timer);
                                    }
                                }
                            });

                    consoleCommands.add(consoleCommand);
                }

            }
            chappieConsoleContext.reset(consoleCommands.toArray(new ConsoleCommand[] {}));
        }

        featureProducer.produce(new FeatureBuildItem(Feature.FEATURE));
    }

    private void printResponse(CompletionStage response, Vertx vertx, long timer) {
        response.thenAccept((output) -> {
            vertx.cancelTimer(timer);
            if (Map.class.isAssignableFrom(output.getClass())) {
                for (Object value : ((Map) output).values()) {
                    System.out.println("\n\n" + value.toString());
                }
            } else {
                System.out.println("\n\n" + output.toString());
            }
        }).exceptionally(ex -> {
            vertx.cancelTimer(timer);
            System.err.println("Failed to get response from AI [ " + ((Throwable) ex).getMessage() + "]");
            return null;
        });
    }
}
