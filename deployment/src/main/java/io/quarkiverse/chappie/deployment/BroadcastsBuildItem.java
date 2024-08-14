package io.quarkiverse.chappie.deployment;

import java.util.concurrent.Flow;

import io.quarkus.builder.item.SimpleBuildItem;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

final class BroadcastsBuildItem extends SimpleBuildItem {

    private final BroadcastProcessor<LastException> lastExceptionBroadcastProcessor;
    private final BroadcastProcessor<Status> statusBroadcastProcessor;

    public BroadcastsBuildItem(BroadcastProcessor<LastException> lastExceptionBroadcastProcessor,
            BroadcastProcessor<Status> statusBroadcastProcessor) {
        this.lastExceptionBroadcastProcessor = lastExceptionBroadcastProcessor;
        this.statusBroadcastProcessor = statusBroadcastProcessor;
    }

    public BroadcastProcessor<LastException> getLastExceptionBroadcastProcessor() {
        return lastExceptionBroadcastProcessor;
    }

    public Flow.Publisher<LastException> getLastExceptionPublisher() {
        return (Flow.Publisher<LastException>) this.lastExceptionBroadcastProcessor;
    }

    public BroadcastProcessor<Status> getStatusBroadcastProcessor() {
        return statusBroadcastProcessor;
    }

    public Flow.Publisher<Status> getStatusPublisher() {
        return (Flow.Publisher<Status>) this.statusBroadcastProcessor;
    }
}
