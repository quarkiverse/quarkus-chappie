package io.quarkiverse.chappie.deployment.workspace;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.quarkiverse.chappie.runtime.dev.ChappieAssistant;
import io.quarkus.assistant.runtime.dev.Assistant;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.workspace.Action;
import io.quarkus.devui.spi.workspace.ActionBuilder;
import io.quarkus.devui.spi.workspace.Display;
import io.quarkus.devui.spi.workspace.DisplayType;
import io.quarkus.devui.spi.workspace.Patterns;
import io.quarkus.devui.spi.workspace.WorkspaceActionBuildItem;
import io.quarkus.logging.Log;

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

    @BuildStep
    BuildTimeActionBuildItem createSearchAction() {
        return new BuildTimeActionBuildItem()
                .actionBuilder()
                .methodName("searchDocs")
                .description("Search for Quarkus documentation")
                .parameter("queryMessage", String.class, "The search query")
                .parameter("maxResults", Integer.class, "The maximum number of results to return")
                .parameter("extension", String.class, "The extension to filter results by, e.g. 'hibernate-orm', may be empty")
                .enableMcpFuctionByDefault()
                .assistantFunction((assistant, p) -> {
                    if (assistant instanceof ChappieAssistant) {
                        // Assistant doesn't have search docs API
                        ChappieAssistant chappie = (ChappieAssistant) assistant;
                        // TODO change params to Map<String, Object>
                        Map<String, Object> params = (Map<String, Object>) (Map) p;
                        String queryMessage = (String) params.get("queryMessage");
                        Integer maxResults;
                        Object maxResultsObj = params.get("maxResults");
                        if (maxResultsObj instanceof Integer) {
                            maxResults = (Integer) maxResultsObj;
                        } else if (maxResultsObj instanceof String) {
                            try {
                                maxResults = Integer.parseInt((String) maxResultsObj);
                            } catch (NumberFormatException e) {
                                maxResults = null; // default
                            }
                        } else {
                            maxResults = null; // default
                        }
                        String extension = (String) params.get("extension");
                        if (queryMessage != null && !queryMessage.isBlank()) {
                            try {
                                return chappie.searchDocs(queryMessage, maxResults, extension)
                                        .exceptionally(t -> {
                                            Log.error("Search failed: " + t.getMessage(), t);
                                            return Map.of("error", t.getMessage());
                                        });
                            } catch (Exception e) {
                                Log.info("Search failed: " + e.getMessage(), e);
                            }
                        } else {
                            Log.info("Search skipped: queryMessage is not set");
                        }
                    }
                    return null;
                })
                .build();
    }

    private ActionBuilder getAddJavaDocAction() {
        return Action.actionBuilder()
                .label("Add JavaDoc")
                .assistantFunction((a, p) -> {
                    Assistant assistant = (Assistant) a;
                    Map params = (Map) p;
                    return assistant.assist(Optional.of(JAVADOC_SYSTEM_MESSAGE),
                            JAVADOC_USER_MESSAGE,
                            getVars(params),
                            List.of(getPath(params)));
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
                    return assistant.assist(Optional.of(TESTGENERATION_SYSTEM_MESSAGE),
                            TESTGENERATION_USER_MESSAGE,
                            getVars(params),
                            List.of(getPath(params)));
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
                    return assistant.assist(Optional.of(EXPLAIN_SYSTEM_MESSAGE),
                            EXPLAIN_USER_MESSAGE,
                            getVars(params),
                            List.of(getPath(params)));
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
                        return assistant.assist(Optional.of(COMPLETE_TODO_SYSTEM_MESSAGE),
                                COMPLETE_TODO_USER_MESSAGE,
                                getVars(params),
                                List.of(getPath(params)));
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

    // TODO: Auto add the top part for updates
    private static final String JAVADOC_SYSTEM_MESSAGE = """
            You will receive content that needs to be manipulated. Use the content received as input when considering the response.
            Also consider the path of the content to determine the file type of the provided content.

            Approach this task step-by-step, take your time and do not skip steps.

            The response must contain 2 fields, path and content.

            Respond with the manipulated content in the content field. This response must be valid. In the content field, include only the manipulated content, no explanation or other text.

            You must not wrap manipulated content in backticks, markdown, or in any other way, but return it as plain text.

            Your job is to add JavaDoc on Class and Method level for the provided code.
             """;

    private static final String JAVADOC_USER_MESSAGE = """
            I have the following content in this Quarkus project:
            ```
            {{content}}
            ```

            Please add or modify the JavaDoc to reflect the code. If JavaDoc exist, take that into account when modifying the content.

            """;

    private static final String COMPLETE_TODO_SYSTEM_MESSAGE = """
             You will receive content that needs to be manipulated. Use the content received as input when considering the response.
             Also consider the path of the content to determine the file type of the provided content.

             Approach this task step-by-step, take your time and do not skip steps.

             The response must contain 2 fields, path and content.

             Respond with the manipulated content in the content field. This response must be valid. In the content field, include only the manipulated content, no explanation or other text.

             You must not wrap manipulated content in backticks, markdown, or in any other way, but return it as plain text.

             Your job is to complete all TODO comments (eg. //TODO:Here something that needs to be done) in the code as best you can. If needed add comment to explain yourself.

            """;
    private static final String COMPLETE_TODO_USER_MESSAGE = """
            I have the following content in this Quarkus project:
                        ```
                        {{content}}
                        ```

                        Please replace any TODO: comments with whatever the comment specifies.
            """;

    private static final String TESTGENERATION_SYSTEM_MESSAGE = """
            You will receive content that needs to used as base for other content generation. Use the content received as input when considering the response.
            Also consider the path of the content to determine the file type of the provided content.

            Approach this task step-by-step, take your time and do not skip steps.

            The response must contain 2 fields, path and content.

            Respond with the generated content in the content field. This response must be valid content in the content type requested. Only include the generated content in the content field, no explanation or other text.

            You must not wrap generated content response in backticks, markdown, or in any other way, but return it as plain text.

            Your job is to generate a test for the provided Quarkus source code. Make sure the solutions is done in the Quarkus recomended way
            """;

    private static final String TESTGENERATION_USER_MESSAGE = """
            I have the following content in this Quarkus project:
            ```
            {{content}}
            ```

            Please generate a Unit test to test all methods in the source code provided.
            Use the same package as the provided source.
            Make the path of the generated test the same as the path of the provided source with Test appended.
            The generated code must be able to compile.
            """;

    private static final String EXPLAIN_SYSTEM_MESSAGE = """
            You will receive content that needs to be interpreted . You will receive the path and the content, consider that when creating a response.
            Also consider the file type of the provided content as it might not be {{programmingLanguage}}, but just part of a {{programmingLanguage}} project.
            Example: You might receive HTML that is part of a Java project. Then interpret it as HTML (not Java)

            Approach this task step-by-step, take your time and do not skip steps.

            Respond with an interpretation in markdown format, but make sure this markdown in encoded such that it can be added to a json file. This response must be valid markdown. Only include the markdown content, no explanation or other text.

            You must not wrap markdown content in backticks, or in any other way, but return it as plain markdown encoded for json. If the interpretation contains code, make sure to use the markdown format to display the code properly.

            The markdown content must be returned per path (the path will be provided in the provided content).
             """;

    private static final String EXPLAIN_USER_MESSAGE = """
            Please explain the provided content. Talk thought what type of content it is and what it does. If possible talk about how it might be used.

            Here are the content:
            {{content}}

            """;
    private static final String NAMESPACE = "devui-assistant";
}
