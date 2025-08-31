package io.quarkiverse.chappie.deployment;

import java.util.List;

import io.quarkiverse.chappie.runtime.dev.ChappieServerManager;
import io.quarkus.assistant.deployment.spi.AssistantPageBuildItem;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.PageBuilder;
import io.quarkus.devui.spi.page.SettingPageBuildItem;
import io.quarkus.devui.spi.page.UnlistedPageBuildItem;

@BuildSteps(onlyIf = IsLocalDevelopment.class)
class ChappieDevUIProcessor {

    @BuildStep
    void configurePage(BuildProducer<SettingPageBuildItem> settingPageProducer) {
        SettingPageBuildItem assistantSettingTab = new SettingPageBuildItem();

        assistantSettingTab.addPage(Page.webComponentPageBuilder()
                .title("Assistant")
                .icon("font-awesome-solid:gear")
                .componentLink("qwc-chappie-configure.js"));

        assistantSettingTab.setHeadlessComponentLink("qwc-chappie-init.js");

        settingPageProducer.produce(assistantSettingTab);
    }

    @BuildStep
    void pages(List<AssistantPageBuildItem> assistantPageBuildItems,
            BuildProducer<UnlistedPageBuildItem> unlistedPageProducer) {

        UnlistedPageBuildItem unlistedPageBuildItem = new UnlistedPageBuildItem();

        for (AssistantPageBuildItem cpbi : assistantPageBuildItems) {
            PageBuilder pageBuilder = cpbi.getPageBuilder();
            unlistedPageBuildItem.addPage(pageBuilder);
        }

        unlistedPageProducer.produce(unlistedPageBuildItem);

    }

    @BuildStep
    JsonRPCProvidersBuildItem createJsonRPCServiceForCache() {
        return new JsonRPCProvidersBuildItem(ChappieServerManager.class);
    }
}
