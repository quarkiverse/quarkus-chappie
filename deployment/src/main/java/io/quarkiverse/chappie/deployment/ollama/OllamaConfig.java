package io.quarkiverse.chappie.deployment.ollama;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface OllamaConfig {

    /**
     * The default port where the inference server listens for requests
     */
    @WithDefault("11434")
    Integer port();

    /**
     * The Model to use
     */
    @WithDefault("llama2")
    String modelName();

    /**
     * Instructs Ollama to preload a model in order to get faster response times
     */
    @WithDefault("true")
    boolean preload();
}
