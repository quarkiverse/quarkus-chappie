package io.quarkiverse.chappie.deployment;

import io.quarkus.builder.item.SimpleBuildItem;

final public class ChappieClientBuildItem extends SimpleBuildItem {
    private final ChappieClient chappieClient;

    public ChappieClientBuildItem(ChappieClient chappieClient) {
        this.chappieClient = chappieClient;
    }

    public ChappieClient getChappieClient() {
        return chappieClient;
    }
}
