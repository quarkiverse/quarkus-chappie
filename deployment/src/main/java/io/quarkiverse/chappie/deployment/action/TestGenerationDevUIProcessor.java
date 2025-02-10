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
import io.quarkiverse.chappie.deployment.sourceoperation.SourceGenerationBuildItem;
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
            BuildProducer<SourceGenerationBuildItem> sourceGenerationProducer,
            AIBuildItem aiBuildItem) {
        if (chappieAvailable.isPresent()) {
            sourceGenerationProducer
                    .produce(new SourceGenerationBuildItem("Generate Test", (Map<String, String> params) -> {
                        if (params.containsKey("path")) {
                            String path = params.get("path");
                            try {
                                Path sourcePath = Paths.get(new URI(path));
                                String sourceCode = SourceCodeFinder.getSourceCode(sourcePath);
                                if (sourceCode != null) {
                                    AIClient aiClient = aiBuildItem.getAIClient();
                                    CompletableFuture<AIFileResponse> response = aiClient
                                            .generateSource(Optional.of(SYSTEM_MESSAGE), USER_MESSAGE, sourceCode)
                                            .thenApply(contents -> new AIFileResponse(createTestPath(sourcePath),
                                                    contents.generatedSource()));
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

    // TODO: Will this work in Gradle ?
    private Path createTestPath(Path srcPath) {
        String s = srcPath.toString();
        s = s.replace("/main/", "/test/");
        s = s.substring(0, s.length() - 5);
        s = s + "Test.java";
        return Path.of(s);
    }

    private static final String SYSTEM_MESSAGE = "Your job is to generate test for the provided Quarkus source code. Make sure the solutions is done in the Quarkus recomended way";
    private static final String USER_MESSAGE = """
             Please generate a Unit test to test all methods in the source code provided.
             Use the same package as the provided source.
             Make the name of the generated test the same as the name of the provided source with Test appended.
             The generated code must be able to compile.
            """;
}
