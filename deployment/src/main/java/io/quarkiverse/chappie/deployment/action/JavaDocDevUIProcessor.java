package io.quarkiverse.chappie.deployment.action;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ContentIO;
import io.quarkiverse.chappie.deployment.workspace.ManipulationBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.AIBuildItem;
import io.quarkus.deployment.dev.ai.AIClient;

@BuildSteps(onlyIf = IsDevelopment.class)
class JavaDocDevUIProcessor {

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<ManipulationBuildItem> manipulationProducer,
            AIBuildItem aiBuildItem) {
        if (chappieAvailable.isPresent()) {
            manipulationProducer
                    .produce(new ManipulationBuildItem("Add JavaDoc", (Map<String, String> params) -> {
                        if (params.containsKey("path")) {
                            String path = params.get("path");
                            String content = ContentIO.readContents(path);
                            if (content != null) {
                                AIClient aiClient = aiBuildItem.getAIClient();
                                CompletableFuture<AIFileResponse> response = aiClient
                                        .manipulate(Optional.of(SYSTEM_MESSAGE), USER_MESSAGE, path, content)
                                        .thenApply(
                                                contents -> new AIFileResponse(path, contents.manipulatedContent()));
                                return response;
                            }
                        }
                        return null;
                    }, Patterns.JAVA_ANY));
        }
    }

    private static final String SYSTEM_MESSAGE = "Your job is to add JavaDoc on Class and Method level for the provided code.";
    private static final String USER_MESSAGE = "Please add or modify the JavaDoc to reflect the code. If JavaDoc exist, take that into account when modifying the content";
}
