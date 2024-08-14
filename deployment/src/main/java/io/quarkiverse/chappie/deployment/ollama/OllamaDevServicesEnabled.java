package io.quarkiverse.chappie.deployment.ollama;

import java.util.function.BooleanSupplier;

import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.LLM;

public class OllamaDevServicesEnabled implements BooleanSupplier {

    private final ChappieConfig chappieConfig;

    OllamaDevServicesEnabled(ChappieConfig chappieConfig) {
        this.chappieConfig = chappieConfig;
    }

    @Override
    public boolean getAsBoolean() {
        return chappieConfig.llm().isPresent() && chappieConfig.llm().get().equals(LLM.ollama)
                && chappieConfig.ollama().enabled();
    }

}
