package io.quarkiverse.chappie.deployment.workspace;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.ChappiePageBuildItem;
import io.quarkiverse.chappie.deployment.ContentIO;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.AIBuildItem;
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
    WorkspaceBuildItem locateWorkspaceItems(BuildSystemTargetBuildItem buildSystemTarget) {
        Path outputDir = buildSystemTarget.getOutputDirectory();
        Path projectRoot = outputDir.getParent();

        List<WorkspaceBuildItem.WorkspaceItem> workspaceItems = new ArrayList<>();

        try {
            Files.walk(projectRoot)
                    .filter(path -> !path.startsWith(outputDir)) // Exclude everything under target/build
                    .filter(Files::isRegularFile) // Only regular files (ignore directories)
                    .forEach(file -> {
                        String name = projectRoot.relativize(file).toString();
                        workspaceItems.add(new WorkspaceBuildItem.WorkspaceItem(name, file));
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new WorkspaceBuildItem(workspaceItems);
    }

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            List<ManipulationBuildItem> manipulationBuildItems,
            List<GenerationBuildItem> generationBuildItems,
            List<InterpretationBuildItem> interpretationBuildItems,
            WorkspaceBuildItem workspaceBuildItem,
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

            buildItemActions.addAction("getWorkspaceItemContent", (Map<String, String> param) -> {
                if (param.containsKey("path")) {
                    return ContentIO.readContents(param.get("path"));
                }
                return null;
            });

            buildItemActions.addAction("saveWorkspaceItemContent", (Map<String, String> param) -> {
                if (param.containsKey("content")) {
                    String content = param.get("content");
                    String pathParam = param.get("path");
                    Path path = Path.of(URI.create(pathParam));
                    try {
                        Files.createDirectories(path.getParent());
                        if (!Files.exists(path))
                            Files.createFile(path);
                        Files.writeString(path, content, StandardOpenOption.TRUNCATE_EXISTING,
                                StandardOpenOption.CREATE);
                    } catch (IOException ex) {
                        return new SavedResult(path, false, ex.getMessage());
                    }
                    return new SavedResult(path, true, null);
                }
                throw new RuntimeException("Invalid input");
            });

            List<Action> actions = new ArrayList<>();
            // manipulations
            for (ManipulationBuildItem manipulation : manipulationBuildItems) {
                actions
                        .add(new Action(manipulation.getLabel(), manipulation.getMethodName(),
                                ActionType.Manipulation, manipulation.getFilter()));
                buildItemActions.addAction(manipulation.getMethodName(), manipulation.getAction());
            }

            // generation
            for (GenerationBuildItem generation : generationBuildItems) {
                actions
                        .add(new Action(generation.getLabel(), generation.getMethodName(),
                                ActionType.Generation, generation.getFilter()));
                buildItemActions.addAction(generation.getMethodName(), generation.getAction());
            }

            // interpretation
            for (InterpretationBuildItem interpretation : interpretationBuildItems) {
                actions
                        .add(new Action(interpretation.getLabel(), interpretation.getMethodName(),
                                ActionType.Interpretation, interpretation.getFilter()));
                buildItemActions.addAction(interpretation.getMethodName(), interpretation.getAction());
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
