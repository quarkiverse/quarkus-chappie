package io.quarkiverse.chappie.deployment.workspace;

import java.nio.file.Path;
import java.util.List;

import io.quarkus.builder.item.SimpleBuildItem;

public final class WorkspaceBuildItem extends SimpleBuildItem {
    private List<WorkspaceItem> workspaceItems;

    public WorkspaceBuildItem(List<WorkspaceItem> workspaceItems) {
        this.workspaceItems = workspaceItems;
    }

    public List<WorkspaceItem> getWorkspaceItems() {
        return workspaceItems;
    }

    static record WorkspaceItem(String name, Path path) {
    }
}
