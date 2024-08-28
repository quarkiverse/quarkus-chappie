package io.quarkiverse.chappie.deployment;

import java.util.List;

import io.quarkiverse.chappie.deployment.devservice.LLM;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.page.CardPageBuildItem;

@BuildSteps(onlyIf = { IsDevelopment.class, ChappieEnabled.class })
class ConfiguredDevUIProcessor {

    @BuildStep
    void pages(List<ChappiePageBuildItem> chappiePageBuildItems,
            BuildProducer<CardPageBuildItem> cardPageProducer,
            ChappieConfig config) {
        CardPageBuildItem chappieCard = new CardPageBuildItem();
        chappieCard.setCustomCard("qwc-chappie-custom-card.js");
        chappieCard.addBuildTimeData("llm", config.llm());

        if (config.llm().equals(LLM.openai)) {
            chappieCard.addBuildTimeData("modelName", config.openai().modelName());
        }

        for (ChappiePageBuildItem cpbi : chappiePageBuildItems) {
            chappieCard.addPage(cpbi.getPageBuilder());
        }

        cardPageProducer.produce(chappieCard);
    }
}
