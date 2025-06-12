package io.quarkiverse.chappie.deployment;

import java.util.List;

import io.quarkiverse.chappie.runtime.dev.ChappieServerManager;
import io.quarkus.assistant.deployment.spi.AssistantPageBuildItem;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.PageBuilder;
import io.quarkus.devui.spi.page.WebComponentPageBuilder;

@BuildSteps(onlyIf = IsLocalDevelopment.class)
class ChappieDevUIProcessor {

    @BuildStep
    AssistantPageBuildItem configurePage() {
        WebComponentPageBuilder pageBuilder = Page.webComponentPageBuilder()
                .icon("font-awesome-solid:gear")
                .title("Configuration")
                .componentLink("qwc-chappie-configure.js");

        return new AssistantPageBuildItem(pageBuilder, true);
    }

    @BuildStep
    void pages(List<AssistantPageBuildItem> assistantPageBuildItems,
            BuildProducer<CardPageBuildItem> cardPageProducer) {

        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();
        cardPageBuildItem.setCustomCard("qwc-chappie-custom-card.js");

        for (AssistantPageBuildItem cpbi : assistantPageBuildItems) {
            PageBuilder pageBuilder = cpbi.getPageBuilder();
            if (cpbi.isAlwaysVisible()) {
                pageBuilder.metadata("alwaysVisible", "true");
            }
            cardPageBuildItem.addPage(pageBuilder);
        }

        cardPageProducer.produce(cardPageBuildItem);

    }

    @BuildStep
    JsonRPCProvidersBuildItem createJsonRPCServiceForCache() {
        return new JsonRPCProvidersBuildItem(ChappieServerManager.class);
    }
}
