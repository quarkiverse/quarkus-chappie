package io.quarkiverse.chappie.deployment;

import io.quarkiverse.chappie.deployment.devservice.LLM;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

@BuildSteps(onlyIf = IsDevelopment.class)
class UnconfiguredDevUIProcessor {

    @BuildStep(onlyIfNot = ChappieEnabled.class)
    void notenabled(BuildProducer<CardPageBuildItem> cardPageProducer, ChappieConfig config) {
        CardPageBuildItem chappieCard = new CardPageBuildItem();

        chappieCard.addBuildTimeData("llm", config.llm());
        if (config.llm().equals(LLM.openai)) {
            chappieCard.addBuildTimeData("modelName", config.openai().modelName());
            chappieCard.addPage(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:circle-question")
                    .title("Configure assistant")
                    .componentLink("qwc-chappie-unconfigured.js"));
        }
        cardPageProducer.produce(chappieCard);
    }
}
