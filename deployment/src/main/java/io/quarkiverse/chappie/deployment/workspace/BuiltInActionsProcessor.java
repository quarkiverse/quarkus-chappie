package io.quarkiverse.chappie.deployment.workspace;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import io.quarkus.assistant.runtime.dev.Assistant;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.workspace.Action;
import io.quarkus.devui.spi.workspace.ActionBuilder;
import io.quarkus.devui.spi.workspace.Display;
import io.quarkus.devui.spi.workspace.DisplayType;
import io.quarkus.devui.spi.workspace.Patterns;
import io.quarkus.devui.spi.workspace.WorkspaceActionBuildItem;

@BuildSteps(onlyIf = IsLocalDevelopment.class)
class BuiltInActionsProcessor {

    @BuildStep
    void createBuiltInActions(BuildProducer<WorkspaceActionBuildItem> workspaceActionProducer) {
        workspaceActionProducer.produce(new WorkspaceActionBuildItem(
                getAddJavaDocAction(),
                getTestGenerationAction(),
                getExplainAction(),
                getCompleteTodoAction()));

    }

    private ActionBuilder getAddJavaDocAction() {
        return Action.actionBuilder()
                .label("Add JavaDoc")
                .assistantFunction((a, p) -> {
                    Assistant assistant = (Assistant) a;
                    Map params = (Map) p;
                    return assistant.assistBuilder()
                            .systemMessage(JavaDocPrompts.SYSTEM_MESSAGE)
                            .userMessage(JavaDocPrompts.USER_MESSAGE)
                            .variables(getVars(params))
                            .addPath(getPath(params))
                            .responseType(JavaDocPrompts.JavaDocResponse.class)
                            .assist();
                })
                .display(Display.replace)
                .displayType(DisplayType.code)
                .namespace(NAMESPACE)
                .filter(Patterns.JAVA_ANY);
    }

    private ActionBuilder getTestGenerationAction() {
        return Action.actionBuilder()
                .label("Generate Test")
                .assistantFunction((a, p) -> {
                    Assistant assistant = (Assistant) a;
                    Map params = (Map) p;
                    return assistant.assistBuilder()
                            .systemMessage(TestGenerationPrompts.SYSTEM_MESSAGE)
                            .userMessage(TestGenerationPrompts.USER_MESSAGE)
                            .variables(getVars(params))
                            .addPath(getPath(params))
                            .responseType(TestGenerationPrompts.TestGenerationResponse.class)
                            .assist();
                })
                .pathConverter((Object param) -> {
                    Path contentPath = (Path) param;
                    if (isTestPath(contentPath))
                        return contentPath; // Already correct
                    String modifiedPath = contentPath.toString().replace(File.separator + "main" + File.separator,
                            File.separator + "test" + File.separator);
                    return Paths.get(modifiedPath.substring(0, modifiedPath.length() - 5) + "Test.java");
                })
                .display(Display.dialog)
                .displayType(DisplayType.code)
                .namespace(NAMESPACE)
                .filter(Patterns.JAVA_SRC);
    }

    private ActionBuilder getExplainAction() {
        return Action.actionBuilder()
                .label("Explain")
                .assistantFunction((a, p) -> {
                    Assistant assistant = (Assistant) a;
                    Map params = (Map) p;
                    return assistant.assistBuilder()
                            .systemMessage(ExplainPrompts.SYSTEM_MESSAGE)
                            .userMessage(ExplainPrompts.USER_MESSAGE)
                            .variables(getVars(params))
                            .addPath(getPath(params))
                            .responseType(ExplainPrompts.JavaDocResponse.class)
                            .assist();
                })
                .display(Display.split)
                .displayType(DisplayType.markdown)
                .namespace(NAMESPACE)
                .filter(Patterns.ANY_KNOWN_TEXT);
    }

    private ActionBuilder getCompleteTodoAction() {
        return Action.actionBuilder()
                .label("Complete //TODO:")
                .assistantFunction((a, p) -> {
                    Assistant assistant = (Assistant) a;
                    Map params = (Map) p;

                    String content = getContent(params);
                    if (content.contains("//TODO:") || content.contains("// TODO:")) {
                        return assistant.assistBuilder()
                                .systemMessage(CompleteTodoPrompts.SYSTEM_MESSAGE)
                                .userMessage(CompleteTodoPrompts.USER_MESSAGE)
                                .variables(getVars(params))
                                .addPath(getPath(params))
                                .responseType(CompleteTodoPrompts.CompleteTodoResponse.class)
                                .assist();
                    }
                    return params;
                })
                .display(Display.replace)
                .displayType(DisplayType.code)
                .namespace(NAMESPACE)
                .filter(Patterns.JAVA_ANY);
    }

    private Map<String, String> getVars(Map params) {
        return Map.of(
                "content", getContent(params),
                "extension", "any"); // Make extension explisitly null so that RAG can kick in for anything
    }

    private Path getPath(Map params) {
        if (params.containsKey("path")) {
            String filePath = (String) params.get("path");
            URI uri = URI.create(filePath);
            return Paths.get(uri);
        }
        return null;
    }

    private String getContent(Map params) {
        if (params.containsKey("content")) {
            return (String) params.get("content");
        }
        return null;
    }

    private boolean isTestPath(Path path) {
        Path normalizedPath = path.toAbsolutePath().normalize();
        return normalizedPath.toString().contains("/src/test/") ||
                normalizedPath.toString().contains("\\src\\test\\"); // Windows path handling
    }

    private static final String NAMESPACE = "devui-assistant";
}
