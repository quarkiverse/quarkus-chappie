package io.quarkiverse.chappie.deployment.workspace;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * This hold all files in the project root
 */
public final class WorkspaceBuildItem extends SimpleBuildItem {
    private List<WorkspaceItem> workspaceItems;

    public WorkspaceBuildItem(List<WorkspaceItem> workspaceItems) {
        this.workspaceItems = workspaceItems;
    }

    public List<WorkspaceItem> getWorkspaceItems() {
        return workspaceItems;
    }

    public List<Path> getPaths() {
        return workspaceItems.stream()
                .map(WorkspaceBuildItem.WorkspaceItem::path)
                .collect(Collectors.toList());
    }

    public static record WorkspaceItem(String name, Path path) {

    }
}
