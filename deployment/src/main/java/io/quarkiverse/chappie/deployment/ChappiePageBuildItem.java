package io.quarkiverse.chappie.deployment;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.devui.spi.page.PageBuilder;

public final class ChappiePageBuildItem extends MultiBuildItem {
    private final PageBuilder pageBuilder;

    public ChappiePageBuildItem(PageBuilder pageBuilder) {
        this.pageBuilder = pageBuilder;
    }

    public PageBuilder getPageBuilder() {
        return pageBuilder;
    }
}
