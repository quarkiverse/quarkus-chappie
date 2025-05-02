package io.quarkus.assistant.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * If available, a handle on the Assistant
 *
 * This is intended for use in dev mode to enable AI-enhanced development
 *
 * TODO: This needs to move to Quarkus
 */
public final class AssistantBuildItem extends SimpleBuildItem {
    private final Assistant assistant;

    public AssistantBuildItem(Assistant assistant) {
        this.assistant = assistant;
    }

    public Assistant getAssistant() {
        return assistant;
    }
}
