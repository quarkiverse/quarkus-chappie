package io.quarkiverse.chappie.deployment;

import java.util.Optional;

import io.quarkiverse.chappie.deployment.ollama.OllamaConfig;
import io.quarkiverse.chappie.deployment.openai.OpenAIConfig;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.chappie")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ChappieConfig {

    /**
     * The LLM to use, example, openai, ollama
     */
    Optional<LLM> llm();

    /**
     * OpenAI config
     */
    //    @WithParentName
    @ConfigDocSection
    OpenAIConfig openai();

    /**
     * Ollama config
     */
    //    @WithParentName
    @ConfigDocSection
    OllamaConfig ollama();
}
