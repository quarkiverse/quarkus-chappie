package io.quarkiverse.chappie.deployment.workspace;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.ChappiePageBuildItem;
import io.quarkiverse.chappie.deployment.ContentIO;
import io.quarkiverse.chappie.deployment.action.AIFileResponse;
import io.quarkiverse.chappie.deployment.action.AIResponse;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.AIBuildItem;
import io.quarkus.deployment.dev.ai.AIClient;
import io.quarkus.deployment.dev.ai.workspace.GenerationWorkspaceActionBuildItem;
import io.quarkus.deployment.dev.ai.workspace.InterpretationWorkspaceActionBuildItem;
import io.quarkus.deployment.dev.ai.workspace.ManipulationWorkspaceActionBuildItem;
import io.quarkus.deployment.pkg.builditem.BuildSystemTargetBuildItem;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.Page;

@BuildSteps(onlyIf = IsDevelopment.class)
class WorkspaceDevUIProcessor {

    @BuildStep
    void workspacePage(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<ChappiePageBuildItem> chappiePageBuildItem) {

        if (chappieAvailable.isPresent()) {
            chappiePageBuildItem.produce(new ChappiePageBuildItem(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:code")
                    .title("Workspace")
                    .componentLink("qwc-chappie-workspace.js")));
        }
    }

    @BuildStep
    void locateWorkspaceItems(BuildSystemTargetBuildItem buildSystemTarget,
            BuildProducer<WorkspaceBuildItem> workspaceProducer) {

        Path outputDir = buildSystemTarget.getOutputDirectory();
        Path projectRoot = outputDir.getParent();

        List<WorkspaceBuildItem.WorkspaceItem> workspaceItems = new ArrayList<>();

        try {
            Files.walk(projectRoot)
                    .filter(path -> !path.startsWith(outputDir)) // Exclude everything under target/build
                    .filter(Files::isRegularFile) // Only regular files (ignore directories)
                    .forEach(file -> {
                        String name = projectRoot.relativize(file).toString();
                        System.out.println(name + "|" + file);
                        workspaceItems.add(new WorkspaceBuildItem.WorkspaceItem(name, file));
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        workspaceProducer.produce(new WorkspaceBuildItem(workspaceItems));
    }

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            WorkspaceBuildItem workspaceBuildItem,
            List<ManipulationWorkspaceActionBuildItem> manipulationActionBuildItems,
            List<GenerationWorkspaceActionBuildItem> generationActionBuildItems,
            List<InterpretationWorkspaceActionBuildItem> interpretationActionBuildItems,
            BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            AIBuildItem aiBuildItem,
            ChappieConfig chappieConfig) {

        if (chappieAvailable.isPresent()) {

            // Actions
            BuildTimeActionBuildItem buildItemActions = new BuildTimeActionBuildItem();

            buildItemActions.addAction("getWorkspaceItems", (t) -> {
                List<WorkspaceBuildItem.WorkspaceItem> workspaceItems = workspaceBuildItem.getWorkspaceItems();
                sortWorkspaceItems(workspaceItems);
                return workspaceItems;
            });

            buildItemActions.addAction("getWorkspaceItemContent", (Map<String, String> params) -> {
                if (params.containsKey("path")) {
                    Path path = Paths.get(URI.create(params.get("path")));
                    return ContentIO.readContents(path);
                }
                return null;
            });

            buildItemActions.addAction("saveWorkspaceItemContent", (Map<String, String> params) -> {
                if (params.containsKey("content")) {
                    String content = params.get("content");
                    Path path = Paths.get(URI.create(params.get("path")));
                    ContentIO.writeContent(path, content);
                    return new SavedResult(path, true, null);
                }
                return new SavedResult(null, false, "Invalid input");
            });

            List<Action> actions = new ArrayList<>();

            // Manipulations

            for (ManipulationWorkspaceActionBuildItem manipulationAction : manipulationActionBuildItems) {
                // This adds it the the Action dropdown (if the filter allows) and the methodName will be used in the buildtime actions
                actions
                        .add(new Action(manipulationAction.getLabel(), manipulationAction.getMethodName(),
                                ActionType.Manipulation, manipulationAction.getFilter()));

                // This sets up the method execution for this action
                buildItemActions.addAction(manipulationAction.getMethodName(), (Map<String, String> params) -> {
                    if (params.containsKey("path")) {
                        Path path = Paths.get(URI.create(params.get("path")));
                        String content = ContentIO.readContents(path);
                        if (content != null) {
                            AIClient aiClient = aiBuildItem.getAIClient();
                            CompletableFuture<AIFileResponse> response = aiClient
                                    .manipulate(manipulationAction.getSystemMessage(), manipulationAction.getUserMessage(),
                                            path, content)
                                    .thenApply(
                                            contents -> new AIFileResponse(path, contents.manipulatedContent()));
                            return response;
                        }
                    }
                    CompletableFuture<AIFileResponse> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(new NullPointerException("Content is empty"));
                    return failedFuture;
                });
            }

            // Generations
            for (GenerationWorkspaceActionBuildItem generationAction : generationActionBuildItems) {
                // This adds it the the Action dropdown (if the filter allows) and the methodName will be used in the buildtime actions
                actions
                        .add(new Action(generationAction.getLabel(), generationAction.getMethodName(),
                                ActionType.Generation, generationAction.getFilter()));

                // This sets up the method execution for this action
                buildItemActions.addAction(generationAction.getMethodName(), (Map<String, String> params) -> {
                    if (params.containsKey("path")) {
                        Path path = Paths.get(URI.create(params.get("path")));
                        String contents = ContentIO.readContents(path);
                        if (contents != null) {
                            AIClient aiClient = aiBuildItem.getAIClient();
                            CompletableFuture<AIFileResponse> response = aiClient
                                    .generate(generationAction.getSystemMessage(), generationAction.getMethodName(), path,
                                            contents)
                                    .thenApply(c -> new AIFileResponse(generationAction.resolveStorePath(path),
                                            c.generatedContent()));
                            return response;
                        }
                    }
                    CompletableFuture<AIFileResponse> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(new NullPointerException("Content is empty"));
                    return failedFuture;

                });
            }

            // Interpretations
            for (InterpretationWorkspaceActionBuildItem interpretationAction : interpretationActionBuildItems) {
                actions
                        .add(new Action(interpretationAction.getLabel(), interpretationAction.getMethodName(),
                                ActionType.Interpretation, interpretationAction.getFilter()));
                buildItemActions.addAction(interpretationAction.getMethodName(), (Map<String, String> params) -> {
                    if (params.containsKey("path")) {
                        Path path = Paths.get(URI.create(params.get("path")));
                        String contents = ContentIO.readContents(path);
                        if (contents != null) {
                            AIClient aiClient = aiBuildItem.getAIClient();
                            CompletableFuture<AIResponse> response = aiClient
                                    .interpret(interpretationAction.getSystemMessage(), interpretationAction.getUserMessage(),
                                            path, contents)
                                    .thenApply(c -> new AIResponse(c.interpretedContent()));
                            return response;
                        }
                    }
                    CompletableFuture<AIResponse> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(new NullPointerException("Content is empty"));
                    return failedFuture;
                });
            }

            buildItemActions.addAction("getActions", (Map<String, String> param) -> {
                return actions;
            });

            buildTimeActionProducer.produce(buildItemActions);
        }
    }

    private void sortWorkspaceItems(List<WorkspaceBuildItem.WorkspaceItem> items) {
        items.sort(Comparator.comparing((WorkspaceBuildItem.WorkspaceItem item) -> isFileInRoot(item.name()))
                .thenComparing(item -> folderPriority(item.name()))
                .thenComparing(WorkspaceBuildItem.WorkspaceItem::name));
    }

    private static int folderPriority(String name) {
        if (name.startsWith("src/main/"))
            return 1;
        if (name.startsWith("src/test/"))
            return 2;
        if (name.startsWith("src/integrationTest/"))
            return 3;
        return 4;
    }

    private boolean isFileInRoot(String name) {
        return !name.contains("/");
    }

    static record SavedResult(Path path, boolean success, String errorMessage) {
    }

}
