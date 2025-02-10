package io.quarkiverse.chappie.deployment.sourceoperation;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkus.builder.item.SimpleBuildItem;

public final class LastSourceOperationBuildItem extends SimpleBuildItem {
    private final AtomicReference<Object> lastResponse;
    private final AtomicReference<Path> path;

    public LastSourceOperationBuildItem(AtomicReference<Object> lastResponse, AtomicReference<Path> path) {
        this.lastResponse = lastResponse;
        this.path = path;
    }

    public AtomicReference<Object> getLastResponse() {
        return lastResponse;
    }

    public AtomicReference<Path> getPath() {
        return path;
    }
}
