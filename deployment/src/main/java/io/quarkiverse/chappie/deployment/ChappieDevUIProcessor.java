package io.quarkiverse.chappie.deployment;

import java.util.List;
import java.util.Map;

import io.quarkiverse.chappie.runtime.dev.ChappieAssistant;
import io.quarkiverse.chappie.runtime.dev.ChappieJsonRpcService;
import io.quarkiverse.chappie.runtime.dev.ChappieServerManager;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.assistant.deployment.spi.AssistantPageBuildItem;
import io.quarkus.deployment.IsLocalDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.MenuPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.PageBuilder;
import io.quarkus.devui.spi.page.SettingPageBuildItem;
import io.quarkus.devui.spi.page.UnlistedPageBuildItem;
import io.quarkus.logging.Log;

@BuildSteps(onlyIf = IsLocalDevelopment.class)
class ChappieDevUIProcessor {

    @BuildStep
    void additionalBean(BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer) {

        additionalBeanProducer.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(ChappieServerManager.class)
                .setUnremovable().build());
    }

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
    void chatPage(BuildProducer<MenuPageBuildItem> menuPageProducer) {
        MenuPageBuildItem chatPage = new MenuPageBuildItem();

        chatPage.addPage(Page.assistantPageBuilder()
                .title("Chat")
                .icon("font-awesome-solid:comment-dots")
                .componentLink("qwc-chappie-chat.js"));

        menuPageProducer.produce(chatPage);
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
    BuildTimeActionBuildItem createSearchAction() {
        return new BuildTimeActionBuildItem()
                .actionBuilder()
                .methodName("searchDocs")
                .description("Search for Quarkus documentation")
                .parameter("queryMessage", String.class, "The search query")
                .parameter("maxResults", Integer.class, "The maximum number of results to return")
                .parameter("extension", String.class, "The extension to filter results by, e.g. 'hibernate-orm', may be empty")
                .enableMcpFuctionByDefault()
                .assistantFunction((assistant, p) -> {
                    if (assistant instanceof ChappieAssistant) {
                        // Assistant doesn't have search docs API
                        ChappieAssistant chappie = (ChappieAssistant) assistant;
                        // TODO change params to Map<String, Object>
                        Map<String, Object> params = (Map<String, Object>) (Map) p;
                        String queryMessage = (String) params.get("queryMessage");
                        Integer maxResults;
                        Object maxResultsObj = params.get("maxResults");
                        if (maxResultsObj instanceof Integer) {
                            maxResults = (Integer) maxResultsObj;
                        } else if (maxResultsObj instanceof String) {
                            try {
                                maxResults = Integer.parseInt((String) maxResultsObj);
                            } catch (NumberFormatException e) {
                                maxResults = null; // default
                            }
                        } else {
                            maxResults = null; // default
                        }
                        String extension = (String) params.get("extension");
                        if (queryMessage != null && !queryMessage.isBlank()) {
                            try {
                                return chappie.searchDocs(queryMessage, maxResults, extension)
                                        .exceptionally(t -> {
                                            Log.error("Search failed: " + t.getMessage(), t);
                                            return Map.of("error", t.getMessage());
                                        });
                            } catch (Exception e) {
                                Log.info("Search failed: " + e.getMessage(), e);
                            }
                        } else {
                            Log.info("Search skipped: queryMessage is not set");
                        }
                    }
                    return null;
                })
                .build();
    }

    @BuildStep
    JsonRPCProvidersBuildItem createJsonRPCServiceForCache() {
        return new JsonRPCProvidersBuildItem(ChappieJsonRpcService.class);
    }
}
