package io.quarkiverse.chappie.deployment;

import java.util.function.BooleanSupplier;

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