package io.quarkiverse.chappie.deployment;

import io.quarkiverse.chappie.deployment.devservice.DevServicesConfig;
import io.quarkiverse.chappie.deployment.devservice.OpenAIConfig;
import io.quarkiverse.chappie.deployment.devservice.ollama.OllamaConfig;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.assistant")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ChappieConfig {

    /**
     * OpenAI config
     */
    @ConfigDocSection
    OpenAIConfig openai();

    /**
     * Ollama config
     */
    @ConfigDocSection
    OllamaConfig ollama();

    /**
     * Dev Services
     */
    @ConfigDocSection
    DevServicesConfig devservices();
}
