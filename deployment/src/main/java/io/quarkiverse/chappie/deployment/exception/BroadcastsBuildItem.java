package io.quarkiverse.chappie.deployment.exception;

import java.util.concurrent.Flow;

import io.quarkus.builder.item.SimpleBuildItem;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

public final class BroadcastsBuildItem extends SimpleBuildItem {

    private final BroadcastProcessor<LastException> lastExceptionBroadcastProcessor;

    public BroadcastsBuildItem(BroadcastProcessor<LastException> lastExceptionBroadcastProcessor) {
        this.lastExceptionBroadcastProcessor = lastExceptionBroadcastProcessor;
    }

    public BroadcastProcessor<LastException> getLastExceptionBroadcastProcessor() {
        return lastExceptionBroadcastProcessor;
    }

    public Flow.Publisher<LastException> getLastExceptionPublisher() {
        return (Flow.Publisher<LastException>) this.lastExceptionBroadcastProcessor;
    }
}
