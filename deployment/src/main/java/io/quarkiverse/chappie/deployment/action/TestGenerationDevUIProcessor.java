package io.quarkiverse.chappie.deployment.action;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ContentIO;
import io.quarkiverse.chappie.deployment.workspace.GenerationBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.AIBuildItem;
import io.quarkus.deployment.dev.ai.AIClient;

@BuildSteps(onlyIf = IsDevelopment.class)
class TestGenerationDevUIProcessor {

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<GenerationBuildItem> generationProducer,
            AIBuildItem aiBuildItem) {
        if (chappieAvailable.isPresent()) {
            generationProducer
                    .produce(new GenerationBuildItem("Generate Test", (Map<String, String> params) -> {
                        if (params.containsKey("path")) {
                            String path = params.get("path");
                            String contents = ContentIO.readContents(path);
                            if (contents != null) {
                                AIClient aiClient = aiBuildItem.getAIClient();
                                CompletableFuture<AIFileResponse> response = aiClient
                                        .generate(Optional.of(SYSTEM_MESSAGE), USER_MESSAGE, path, contents)
                                        .thenApply(c -> new AIFileResponse(createTestPath(path),
                                                c.generatedContent()));
                                return response;
                            }
                        }
                        return null;
                    }, Patterns.JAVA_SRC));
        }
    }

    private String createTestPath(String path) {
        Path sourcePath = Paths.get(path);
        String modifiedPath = sourcePath.toString().replace(File.separator + "main" + File.separator,
                File.separator + "test" + File.separator);
        String testPath = modifiedPath.substring(0, modifiedPath.length() - 5) + "Test.java";
        return testPath;
    }

    private static final String SYSTEM_MESSAGE = "Your job is to generate test for the provided Quarkus source code. Make sure the solutions is done in the Quarkus recomended way";
    private static final String USER_MESSAGE = """
             Please generate a Unit test to test all methods in the source code provided.
             Use the same package as the provided source.
             Make the name of the generated test the same as the name of the provided source with Test appended.
             The generated code must be able to compile.
            """;
}
