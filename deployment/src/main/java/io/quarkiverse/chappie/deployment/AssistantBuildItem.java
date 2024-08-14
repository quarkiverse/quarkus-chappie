package io.quarkiverse.chappie.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

final class AssistantBuildItem extends SimpleBuildItem {
    private final Assistant assistant;

    public AssistantBuildItem(Assistant assistant) {
        this.assistant = assistant;
    }

    public Assistant getAssistant() {
        return this.assistant;
    }

}
