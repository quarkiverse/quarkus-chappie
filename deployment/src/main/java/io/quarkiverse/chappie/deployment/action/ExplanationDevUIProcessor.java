package io.quarkiverse.chappie.deployment.action;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ContentIO;
import io.quarkiverse.chappie.deployment.workspace.InterpretationBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.AIBuildItem;
import io.quarkus.deployment.dev.ai.AIClient;

@BuildSteps(onlyIf = IsDevelopment.class)
class ExplanationDevUIProcessor {

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<InterpretationBuildItem> interpretationProducer,
            AIBuildItem aiBuildItem) {
        if (chappieAvailable.isPresent()) {
            interpretationProducer
                    .produce(new InterpretationBuildItem("Explain", (Map<String, String> params) -> {
                        if (params.containsKey("path")) {
                            String path = params.get("path");
                            String contents = ContentIO.readContents(path);
                            if (contents != null) {
                                AIClient aiClient = aiBuildItem.getAIClient();
                                CompletableFuture<AIResponse> response = aiClient
                                        .interpret(USER_MESSAGE, path, contents)
                                        .thenApply(c -> new AIResponse(c.interpretedContent()));
                                return response;
                            }
                        }
                        return null;
                    }, Optional.empty()));
        }
    }

    private static final String USER_MESSAGE = """
             Please explain the provided content. Don't just return the original content, narate an explanation with words.
            """;
}
