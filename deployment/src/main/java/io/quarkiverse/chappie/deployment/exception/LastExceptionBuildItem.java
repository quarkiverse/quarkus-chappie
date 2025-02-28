package io.quarkiverse.chappie.deployment.exception;

import java.util.concurrent.atomic.AtomicReference;

import io.quarkus.builder.item.SimpleBuildItem;

public final class LastExceptionBuildItem extends SimpleBuildItem {
    private final AtomicReference<LastException> lastException;

    public LastExceptionBuildItem(AtomicReference<LastException> lastException) {
        this.lastException = lastException;
    }

    public AtomicReference<LastException> getLastException() {
        return lastException;
    }
}
