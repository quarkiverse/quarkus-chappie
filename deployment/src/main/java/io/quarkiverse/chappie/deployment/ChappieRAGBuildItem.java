package io.quarkiverse.chappie.deployment;

import java.util.Map;

import io.quarkus.builder.item.SimpleBuildItem;

final public class ChappieRAGBuildItem extends SimpleBuildItem {
    private final Map<String, String> properties;

    public ChappieRAGBuildItem(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
