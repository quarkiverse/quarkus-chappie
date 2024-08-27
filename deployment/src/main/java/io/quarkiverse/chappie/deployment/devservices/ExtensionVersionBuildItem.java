package io.quarkiverse.chappie.deployment.devservices;

import io.quarkus.builder.item.SimpleBuildItem;

final public class ExtensionVersionBuildItem extends SimpleBuildItem {
    private final String version;

    public ExtensionVersionBuildItem(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
