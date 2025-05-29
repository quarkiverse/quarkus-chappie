package io.quarkiverse.chappie.deployment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.SubmissionPublisher;

import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import io.quarkiverse.chappie.runtime.dev.ChappieAssistant;
import io.quarkiverse.chappie.runtime.dev.ChappieRecorder;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.assistant.deployment.spi.AssistantConsoleBuildItem;
import io.quarkus.assistant.runtime.dev.Assistant;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.ConsoleStateManager;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.devui.spi.buildtime.FooterLogBuildItem;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.util.ClassPathUtils;
import io.vertx.core.Vertx;

@BuildSteps(onlyIf = IsLocalDevelopment.class)
public class ChappieProcessor {
    private static final Logger LOG = Logger.getLogger(ChappieProcessor.class);
    private static final String FEATURE = "assistant";
    static volatile ConsoleStateManager.ConsoleContext chappieConsoleContext;

    static volatile ChappieAssistant assistant = new ChappieAssistant();

    @Record(ExecutionTime.RUNTIME_INIT)
    @BuildStep
    void createBeans(BuildProducer<FooterLogBuildItem> footerLogProducer,
            ChappieRecorder recorder,
            BeanContainerBuildItem beanContainer,
            ExtensionVersionBuildItem extensionVersionBuildItem) {

        RuntimeValue<SubmissionPublisher<String>> chappieLog = recorder.createChappieServerManager(beanContainer.getValue(),
                assistant,
                extensionVersionBuildItem.getVersion());

        DevConsoleManager.register("chappie.setBaseUrl", (t) -> {
            String baseUrl = null;
            if (t.containsKey("baseUrl")) {
                baseUrl = t.get("baseUrl");
            }
            assistant.setBaseUrl(baseUrl);
            return true;
        });

        DevConsoleManager.setGlobal(DevConsoleManager.DEV_MANAGER_GLOBALS_ASSISTANT, assistant);

        footerLogProducer.produce(new FooterLogBuildItem("Assistant", chappieLog));

    }

    @BuildStep
    public void findExtensionVersion(BuildProducer<ExtensionVersionBuildItem> extensionVersionProducer) {
        try {
            final Yaml yaml = new Yaml();

            ClassPathUtils.consumeAsPaths(YAML_FILE, (Path p) -> {
                try {

                    final String extensionYaml;
                    try (Scanner scanner = new Scanner(Files.newBufferedReader(p, StandardCharsets.UTF_8))) {
                        scanner.useDelimiter("\\A");
                        extensionYaml = scanner.hasNext() ? scanner.next() : null;
                    }
                    if (extensionYaml == null) {
                        // This is a internal extension
                        return;
                    }

                    final Map<String, Object> extensionMap = yaml.load(extensionYaml);

                    if (extensionMap.containsKey("name")) {
                        String artifactId = (String) extensionMap.getOrDefault("artifact", null);
                        if (artifactId != null && artifactId.startsWith(GAV_START)) {
                            String version = artifactId.substring(GAV_START.length());
                            extensionVersionProducer.produce(new ExtensionVersionBuildItem(version));
                        }
                    }
                } catch (IOException | RuntimeException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException ex) {
            LOG.error("Error getting version for the Quarkus Assistant", ex);
        }
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep
    FeatureBuildItem setupConsole(List<AssistantConsoleBuildItem> assistantConsoleBuildItems) {

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
                                            Talking to Quarkus Assistant, please wait""");

                                    long timer = vertx.setPeriodic(800, id -> System.out.print("."));

                                    Assistant assistant = DevConsoleManager
                                            .getGlobal(DevConsoleManager.DEV_MANAGER_GLOBALS_ASSISTANT);

                                    if (assistantConsoleBuildItem.getFunction().isPresent()) {
                                        CompletionStage<?> response = assistantConsoleBuildItem.getFunction().get()
                                                .apply(assistant);
                                        printResponse(response, vertx, timer);
                                    } else {
                                        CompletionStage response = assistant.assistBuilder()
                                                .systemMessage(assistantConsoleBuildItem.getSystemMessage().orElse(null))
                                                .userMessage(assistantConsoleBuildItem.getUserMessage())
                                                .variables(assistantConsoleBuildItem.getVariables())
                                                .assist();
                                        printResponse(response, vertx, timer);
                                    }
                                }
                            });

                    consoleCommands.add(consoleCommand);
                }

            }
            chappieConsoleContext.reset(consoleCommands.toArray(new ConsoleCommand[] {}));
        }

        return new FeatureBuildItem(FEATURE);
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
            System.err.println("Failed to get response from Quarkus Assistant [ " + ((Throwable) ex).getMessage() + "]");
            return null;
        });
    }

    private static final String YAML_FILE = "/META-INF/quarkus-extension.yaml";
    private static final String GROUP_ID = "io.quarkiverse.chappie";
    private static final String ARTIFACT_ID = "quarkus-chappie";
    private static final String GAV_START = GROUP_ID + ":" + ARTIFACT_ID + "::jar:";
}
