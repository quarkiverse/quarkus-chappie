package io.quarkiverse.chappie.deployment;

import java.util.concurrent.atomic.AtomicReference;

import io.quarkus.builder.item.SimpleBuildItem;

final class LastExceptionBuildItem extends SimpleBuildItem {
    private final AtomicReference<LastException> lastException;

    public LastExceptionBuildItem(AtomicReference<LastException> lastException) {
        this.lastException = lastException;
    }

    public AtomicReference<LastException> getLastException() {
        return lastException;
    }
}
