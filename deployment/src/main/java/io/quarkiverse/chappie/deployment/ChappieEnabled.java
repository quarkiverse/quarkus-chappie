package io.quarkiverse.chappie.deployment;

import java.util.function.BooleanSupplier;

public class ChappieEnabled implements BooleanSupplier {

    private final ChappieConfig chappieConfig;

    ChappieEnabled(ChappieConfig chappieConfig) {
        this.chappieConfig = chappieConfig;
    }

    @Override
    public boolean getAsBoolean() {
        if (chappieConfig.llm().isPresent()) {
            if (chappieConfig.llm().get().equals(LLM.openai) && chappieConfig.openai().apiKey().isPresent()) {
                return true;
            }
            // TODO: Add olama support
        }
        return false;
    }
}