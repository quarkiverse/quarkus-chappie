package io.quarkiverse.chappie.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "chappie")
public final class ChappieConfig {

    /**
     * Your OpenAI Api Key
     */
    @ConfigItem(defaultValue = "apiKey")
    public String apiKey;

    /**
     * The OpenAI Model to use
     */
    @ConfigItem(defaultValue = "gpt-4-turbo")
    public String modelName;
}
