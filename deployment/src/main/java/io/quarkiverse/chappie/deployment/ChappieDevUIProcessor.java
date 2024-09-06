package io.quarkiverse.chappie.deployment;

import java.util.List;
import java.util.Optional;

import io.quarkiverse.chappie.deployment.devservice.ollama.OllamaBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

@BuildSteps(onlyIf = IsDevelopment.class)
class ChappieDevUIProcessor {

    @BuildStep
    void pages(List<ChappiePageBuildItem> chappiePageBuildItems,
            Optional<OllamaBuildItem> ollamaBuildItem,
            BuildProducer<CardPageBuildItem> cardPageProducer,
            ChappieConfig config) {

        if (config.openai().apiKey().isPresent()) {
            configuredOpenAiPage(chappiePageBuildItems, cardPageProducer, config);
        } else if (ollamaBuildItem.isPresent()) {
            configuredOllamaPage(chappiePageBuildItems, cardPageProducer, config);
        } else {
            unconfiguredPage(cardPageProducer);
        }
    }

    private void configuredOpenAiPage(List<ChappiePageBuildItem> chappiePageBuildItems,
            BuildProducer<CardPageBuildItem> cardPageProducer,
            ChappieConfig config) {
        configuredPage(chappiePageBuildItems, cardPageProducer, "OpenAI", config.openai().modelName());
    }

    private void configuredOllamaPage(List<ChappiePageBuildItem> chappiePageBuildItems,
            BuildProducer<CardPageBuildItem> cardPageProducer,
            ChappieConfig config) {
        configuredPage(chappiePageBuildItems, cardPageProducer, "Ollama", config.ollama().modelName());
    }

    private void configuredPage(List<ChappiePageBuildItem> chappiePageBuildItems,
            BuildProducer<CardPageBuildItem> cardPageProducer,
            String llm, String modelName) {
        CardPageBuildItem chappieCard = new CardPageBuildItem();
        chappieCard.setCustomCard("qwc-chappie-custom-card.js");

        chappieCard.addBuildTimeData("llm", llm);
        chappieCard.addBuildTimeData("modelName", modelName);

        for (ChappiePageBuildItem cpbi : chappiePageBuildItems) {
            chappieCard.addPage(cpbi.getPageBuilder());
        }

        cardPageProducer.produce(chappieCard);
    }

    private void unconfiguredPage(BuildProducer<CardPageBuildItem> cardPageProducer) {
        CardPageBuildItem chappieCard = new CardPageBuildItem();

        chappieCard.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:circle-question")
                .title("Configure assistant")
                .componentLink("qwc-chappie-unconfigured.js"));

        cardPageProducer.produce(chappieCard);
    }

}
