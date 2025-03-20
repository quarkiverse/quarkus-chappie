package io.quarkiverse.chappie.deployment.workspace;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.ChappiePageBuildItem;
import io.quarkiverse.chappie.deployment.ContentIO;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.assistant.AIBuildItem;
import io.quarkus.deployment.dev.assistant.AIClient;
import io.quarkus.deployment.dev.assistant.workspace.UserWorkspaceBuildItem;
import io.quarkus.deployment.dev.assistant.workspace.WorkspaceCreateBuildItem;
import io.quarkus.deployment.dev.assistant.workspace.WorkspaceReadBuildItem;
import io.quarkus.deployment.dev.assistant.workspace.WorkspaceUpdateBuildItem;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.Page;

@BuildSteps(onlyIf = IsDevelopment.class)
class WorkspaceDevUIProcessor {

    // TODO: Should this move to a more general place ?
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
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            UserWorkspaceBuildItem workspaceBuildItem,
            List<WorkspaceUpdateBuildItem> workspaceUpdateBuildItems,
            List<WorkspaceCreateBuildItem> workspaceCreateBuildItems,
            List<WorkspaceReadBuildItem> workspaceReadBuildItems,
            BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            AIBuildItem aiBuildItem,
            ChappieConfig chappieConfig) {

        if (chappieAvailable.isPresent()) {

            // Actions
            BuildTimeActionBuildItem buildItemActions = new BuildTimeActionBuildItem();

            buildItemActions.addAction("getWorkspaceItems", (t) -> {
                return workspaceBuildItem.getWorkspaceItems();
            });

            buildItemActions.addAction("getWorkspaceItemContent", (Map<String, String> params) -> {
                if (params.containsKey("path")) {
                    Path path = toPath(params.get("path"));
                    return ContentIO.readContents(path);
                }
                return null;
            });

            buildItemActions.addAction("saveWorkspaceItemContent", (Map<String, String> params) -> {
                if (params.containsKey("content")) {
                    String content = params.get("content");
                    Path path = Paths.get(params.get("path"));
                    ContentIO.writeContent(path, content);
                    return new SavedResult(path.toString(), true, null);
                }
                return new SavedResult(null, false, "Invalid input");
            });

            List<Action> actions = new ArrayList<>();

            // Update (Manipulation content)
            for (WorkspaceUpdateBuildItem workspaceUpdate : workspaceUpdateBuildItems) {
                // This adds it the the Action dropdown (if the filter allows) and the methodName will be used in the buildtime actions
                actions
                        .add(new Action(workspaceUpdate.getLabel(), workspaceUpdate.getMethodName(),
                                ActionType.Update, workspaceUpdate.getFilter()));

                // This sets up the method execution for this action
                buildItemActions.addAction(workspaceUpdate.getMethodName(), (Map<String, String> params) -> {
                    if (params.containsKey("path")) {
                        Path path = toPath(params.get("path"));
                        // TODO: Get the varibles
                        AIClient aiClient = aiBuildItem.getAIClient();
                        CompletableFuture<Map<String, String>> pathAndContent = aiClient
                                .workspaceUpdate(workspaceUpdate.getSystemMessage(), workspaceUpdate.getUserMessage(),
                                        path)
                                .thenApply(
                                        contents -> contents.pathAndContent());
                        return pathAndContent;
                    } else {
                        CompletableFuture<Map<String, String>> failedFuture = new CompletableFuture<>();
                        failedFuture.completeExceptionally(new NullPointerException("path parameter not provided"));
                        return failedFuture;
                    }
                });
            }

            // Create (Generate content)
            for (WorkspaceCreateBuildItem workspaceCreate : workspaceCreateBuildItems) {
                // This adds it the the Action dropdown (if the filter allows) and the methodName will be used in the buildtime actions
                actions
                        .add(new Action(workspaceCreate.getLabel(), workspaceCreate.getMethodName(),
                                ActionType.Create, workspaceCreate.getFilter()));

                // This sets up the method execution for this action
                buildItemActions.addAction(workspaceCreate.getMethodName(), (Map<String, String> params) -> {
                    if (params.containsKey("path")) {
                        Path path = toPath(params.get("path"));
                        // TODO: Get the varibles
                        AIClient aiClient = aiBuildItem.getAIClient();
                        CompletableFuture<Map<String, String>> pathAndContent = aiClient
                                .workspaceCreate(workspaceCreate.getSystemMessage(), workspaceCreate.getUserMessage(), path)
                                .thenApply(c -> {
                                    return c.pathAndContent().entrySet().stream()
                                            .collect(Collectors.toMap(
                                                    entry -> workspaceCreate.resolveStorePath(entry.getKey()),
                                                    Map.Entry::getValue));
                                });
                        return pathAndContent;

                    }
                    CompletableFuture<Map<String, String>> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(new NullPointerException("Content is empty"));
                    return failedFuture;

                });
            }

            // Read (Interpret content)
            for (WorkspaceReadBuildItem workspaceRead : workspaceReadBuildItems) {
                actions
                        .add(new Action(workspaceRead.getLabel(), workspaceRead.getMethodName(),
                                ActionType.Read, workspaceRead.getFilter()));
                buildItemActions.addAction(workspaceRead.getMethodName(), (Map<String, String> params) -> {
                    if (params.containsKey("path")) {
                        Path path = toPath(params.get("path"));
                        // TODO: Get the varibles
                        AIClient aiClient = aiBuildItem.getAIClient();
                        CompletableFuture<Map<String, String>> pathAndContent = aiClient
                                .workspaceRead(workspaceRead.getSystemMessage(), workspaceRead.getUserMessage(), path)
                                .thenApply(c -> {
                                    return c.pathAndContent();
                                });
                        return pathAndContent;
                    }
                    CompletableFuture<Map<String, String>> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(new NullPointerException("Content is empty"));
                    return failedFuture;
                });
            }

            Collections.sort(actions, Comparator.comparing(Action::label));
            buildItemActions.addAction("getActions", (Map<String, String> param) -> {
                return actions;
            });

            buildTimeActionProducer.produce(buildItemActions);
        }
    }

    static record SavedResult(String path, boolean success, String errorMessage) {
    }

    private Path toPath(String path) {
        try {
            return Paths.get(URI.create(path));
        } catch (IllegalArgumentException iae) {
            return Paths.get(path);
        }
    }

}
