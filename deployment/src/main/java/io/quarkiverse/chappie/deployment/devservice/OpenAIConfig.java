package io.quarkiverse.chappie.deployment.devservice;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface OpenAIConfig {

    /**
     * Your OpenAI Api Key
     */
    Optional<String> apiKey();

    /**
     * Your OpenAI Base Url
     */
    Optional<String> baseUrl();

    /**
     * The Model to use
     */
    @WithDefault("gpt-4o-mini")
    String modelName();

}
