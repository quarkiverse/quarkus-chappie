package io.quarkiverse.chappie.deployment;

import java.util.Optional;

import io.quarkiverse.chappie.deployment.devservices.DevServicesConfig;
import io.quarkiverse.chappie.deployment.ollama.OllamaConfig;
import io.quarkiverse.chappie.deployment.openai.OpenAIConfig;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.assistant")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ChappieConfig {

    /**
     * The LLM to use, example, openai, ollama
     */
    Optional<LLM> llm();

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
