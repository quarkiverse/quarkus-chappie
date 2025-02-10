package io.quarkiverse.chappie.deployment.action;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.SourceCodeFinder;
import io.quarkiverse.chappie.deployment.sourceoperation.SourceManipulationBuildItem;
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
            BuildProducer<SourceManipulationBuildItem> sourceManipulationProducer,
            AIBuildItem aiBuildItem) {
        if (chappieAvailable.isPresent()) {
            sourceManipulationProducer
                    .produce(new SourceManipulationBuildItem("Add JavaDoc", (Map<String, String> params) -> {
                        if (params.containsKey("path")) {
                            String path = params.get("path");
                            try {
                                Path sourcePath = Paths.get(new URI(path));
                                String sourceCode = SourceCodeFinder.getSourceCode(sourcePath);
                                if (sourceCode != null) {
                                    AIClient aiClient = aiBuildItem.getAIClient();
                                    CompletableFuture<AIFileResponse> response = aiClient
                                            .manipulateSource(Optional.of(SYSTEM_MESSAGE), USER_MESSAGE, sourceCode)
                                            .thenApply(
                                                    contents -> new AIFileResponse(sourcePath, contents.manipulatedSource()));
                                    return response;
                                }
                            } catch (URISyntaxException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                        return null;
                    }));
        }
    }

    private static final String SYSTEM_MESSAGE = "Your job is to add JavaDoc on Class and Method level for the provided code.";
    private static final String USER_MESSAGE = "Please add or modify the JavaDoc to reflect the code.";
}
