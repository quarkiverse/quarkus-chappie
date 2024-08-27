package io.quarkiverse.chappie.deployment;

import io.quarkiverse.chappie.deployment.devservices.DevServicesConfig;
import io.quarkiverse.chappie.deployment.openai.OpenAIConfig;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.assistant")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ChappieConfig {

    /**
     * The LLM to use, for now we only support openai
     */
    @WithDefault("openai")
    LLM llm();

    /**
     * OpenAI config
     */
    @ConfigDocSection
    OpenAIConfig openai();

    /**
     * Dev Services
     */
    @ConfigDocSection
    DevServicesConfig devservices();
}
