package io.quarkiverse.chappie.deployment.devservice.ollama;

import io.quarkus.builder.item.SimpleBuildItem;

public final class OllamaBuildItem extends SimpleBuildItem {
    private final String url;

    public OllamaBuildItem(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

}
