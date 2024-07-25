package io.quarkiverse.chappie.deployment;

import java.util.concurrent.Flow;

import io.quarkiverse.chappie.runtime.LastException;
import io.quarkus.builder.item.SimpleBuildItem;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

final class LastExceptionBuildItem extends SimpleBuildItem {

    private final BroadcastProcessor<LastException> lastExceptionBroadcastProcessor;

    public LastExceptionBuildItem(BroadcastProcessor<LastException> lastExceptionBroadcastProcessor) {
        this.lastExceptionBroadcastProcessor = lastExceptionBroadcastProcessor;
    }

    public BroadcastProcessor<LastException> getLastExceptionBroadcastProcessor() {
        return lastExceptionBroadcastProcessor;
    }

    public Flow.Publisher<LastException> getLastExceptionPublisher() {
        Flow.Publisher<LastException> publisher = this.lastExceptionBroadcastProcessor.toHotStream();
        return publisher;
    }
}
