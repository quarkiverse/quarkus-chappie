package io.quarkiverse.chappie.deployment.console;

import java.util.List;
import java.util.Optional;

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
            Optional<List<AIConsoleBuildItem>> aiConsoleBuildItems) {

        if (aiConsoleBuildItems.isPresent()) {
            if (chappieConsoleContext == null) {
                chappieConsoleContext = ConsoleStateManager.INSTANCE.createContext("Assistant");
            }

            ConsoleCommand[] consoleCommands = aiConsoleBuildItems.get().stream()
                    .map(AIConsoleBuildItem::getConsoleCommand)
                    .toArray(ConsoleCommand[]::new);

            chappieConsoleContext.reset(consoleCommands);

        }

        featureProducer.produce(new FeatureBuildItem(Feature.FEATURE));
    }
}
