package io.quarkiverse.chappie.deployment;

import java.util.function.BooleanSupplier;

import io.quarkiverse.chappie.deployment.devservice.LLM;

public class ChappieEnabled implements BooleanSupplier {

    private final ChappieConfig chappieConfig;

    ChappieEnabled(ChappieConfig chappieConfig) {
        this.chappieConfig = chappieConfig;
    }

    @Override
    public boolean getAsBoolean() {
        if (chappieConfig.llm().equals(LLM.openai) && chappieConfig.openai().apiKey().isPresent()) {
            return true;
        }
        return false;
    }
}
