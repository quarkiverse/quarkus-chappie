package io.quarkiverse.chappie.deployment.workspace;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
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
import io.quarkus.deployment.dev.ai.workspace.WorkspaceCreateBuildItem;
import io.quarkus.deployment.dev.ai.workspace.WorkspaceReadBuildItem;
import io.quarkus.deployment.dev.ai.workspace.WorkspaceUpdateBuildItem;
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
            Files.walkFileTree(projectRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (Files.isHidden(dir)) {
                        return FileVisitResult.SKIP_SUBTREE; // Skip hidden directories and everything inside them
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!Files.isHidden(file) && !file.startsWith(outputDir)) {
                        String name = projectRoot.relativize(file).toString();
                        workspaceItems.add(new WorkspaceBuildItem.WorkspaceItem(name, file));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        workspaceProducer.produce(new WorkspaceBuildItem(workspaceItems));
    }

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            WorkspaceBuildItem workspaceBuildItem,
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

            // Update (Manipulation content)
            for (WorkspaceUpdateBuildItem workspaceUpdate : workspaceUpdateBuildItems) {
                // This adds it the the Action dropdown (if the filter allows) and the methodName will be used in the buildtime actions
                actions
                        .add(new Action(workspaceUpdate.getLabel(), workspaceUpdate.getMethodName(),
                                ActionType.Update, workspaceUpdate.getFilter()));

                // This sets up the method execution for this action
                buildItemActions.addAction(workspaceUpdate.getMethodName(), (Map<String, String> params) -> {
                    if (params.containsKey("path")) {
                        Path path = Paths.get(URI.create(params.get("path")));
                        String content = ContentIO.readContents(path);
                        if (content != null) {
                            AIClient aiClient = aiBuildItem.getAIClient();
                            CompletableFuture<AIFileResponse> response = aiClient
                                    .manipulate(workspaceUpdate.getSystemMessage(), workspaceUpdate.getUserMessage(),
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

            // Create (Generate content)
            for (WorkspaceCreateBuildItem workspaceCreate : workspaceCreateBuildItems) {
                // This adds it the the Action dropdown (if the filter allows) and the methodName will be used in the buildtime actions
                actions
                        .add(new Action(workspaceCreate.getLabel(), workspaceCreate.getMethodName(),
                                ActionType.Create, workspaceCreate.getFilter()));

                // This sets up the method execution for this action
                buildItemActions.addAction(workspaceCreate.getMethodName(), (Map<String, String> params) -> {
                    if (params.containsKey("path")) {
                        Path path = Paths.get(URI.create(params.get("path")));
                        String contents = ContentIO.readContents(path);
                        if (contents != null) {
                            AIClient aiClient = aiBuildItem.getAIClient();
                            CompletableFuture<AIFileResponse> response = aiClient
                                    .generate(workspaceCreate.getSystemMessage(), workspaceCreate.getMethodName(), path,
                                            contents)
                                    .thenApply(c -> new AIFileResponse(workspaceCreate.resolveStorePath(path),
                                            c.generatedContent()));
                            return response;
                        }
                    }
                    CompletableFuture<AIFileResponse> failedFuture = new CompletableFuture<>();
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
                        Path path = Paths.get(URI.create(params.get("path")));
                        String contents = ContentIO.readContents(path);
                        if (contents != null) {
                            AIClient aiClient = aiBuildItem.getAIClient();
                            CompletableFuture<AIResponse> response = aiClient
                                    .interpret(workspaceRead.getSystemMessage(), workspaceRead.getUserMessage(),
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

            Collections.sort(actions, Comparator.comparing(Action::label));
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
        if (name.startsWith("src/main/java"))
            return 1;
        if (name.startsWith("src/main/resources"))
            return 2;
        if (name.startsWith("src/main/"))
            return 3;
        if (name.startsWith("src/test/java"))
            return 4;
        if (name.startsWith("src/test/resources"))
            return 5;
        if (name.startsWith("src/test/"))
            return 6;
        if (name.startsWith("src/integrationTest/"))
            return 7;
        return 8;
    }

    private boolean isFileInRoot(String name) {
        return !name.contains("/");
    }

    static record SavedResult(Path path, boolean success, String errorMessage) {
    }

}
