package io.quarkiverse.chappie.deployment;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkus.builder.item.SimpleBuildItem;

final class LastSolutionBuildItem extends SimpleBuildItem {
    private final AtomicReference<Object> lastSolution;
    private final AtomicReference<Path> path;

    public LastSolutionBuildItem(AtomicReference<Object> lastSolution, AtomicReference<Path> path) {
        this.lastSolution = lastSolution;
        this.path = path;
    }

    public AtomicReference<Object> getLastSolution() {
        return lastSolution;
    }

    public AtomicReference<Path> getPath() {
        return path;
    }
}
