package io.quarkiverse.chappie.deployment.action;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.assistant.workspace.Patterns;
import io.quarkus.deployment.dev.assistant.workspace.WorkspaceCreateBuildItem;

@BuildSteps(onlyIf = IsDevelopment.class)
class TestGenerationDevUIProcessor {

    @BuildStep
    void createWorkspaceActions(BuildProducer<WorkspaceCreateBuildItem> workspaceCreateProducer) {
        workspaceCreateProducer
                .produce(WorkspaceCreateBuildItem.builder()
                        .label("Generate Test")
                        .systemMessage(SYSTEM_MESSAGE)
                        .userMessage(USER_MESSAGE)
                        .storePathFunction((Path contentPath) -> {
                            if (isTestPath(contentPath))
                                return contentPath; // Already correct
                            String modifiedPath = contentPath.toString().replace(File.separator + "main" + File.separator,
                                    File.separator + "test" + File.separator);
                            return Paths.get(modifiedPath.substring(0, modifiedPath.length() - 5) + "Test.java");
                        })
                        .filter(Patterns.JAVA_SRC)
                        .build());
    }

    private boolean isTestPath(Path path) {
        Path normalizedPath = path.toAbsolutePath().normalize();
        return normalizedPath.toString().contains("/src/test/") ||
                normalizedPath.toString().contains("\\src\\test\\"); // Windows path handling
    }

    private static final String SYSTEM_MESSAGE = "Your job is to generate test for the provided Quarkus source code. Make sure the solutions is done in the Quarkus recomended way";
    private static final String USER_MESSAGE = """
             Please generate a Unit test to test all methods in the source code provided.
             Use the same package as the provided source.
             Make the name of the generated test the same as the name of the provided source with Test appended.
             The generated code must be able to compile.
            """;
}
