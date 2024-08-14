package io.quarkiverse.chappie.deployment.ollama;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface OllamaConfig {

    /**
     * If DevServices has been explicitly enabled or disabled. DevServices is generally enabled by default, unless there is
     * an
     * existing configuration present.
     * <p>
     * When DevServices is enabled Quarkus will attempt to automatically serve a model if there are any matching ones.
     */
    @WithDefault("true")
    boolean enabled();

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
