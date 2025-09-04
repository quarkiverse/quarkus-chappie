package io.quarkiverse.chappie.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.assistant")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ChappieConfig {

    /**
     * Augmenting configuration.
     */
    Augmenting augmenting();

    @ConfigGroup
    interface Augmenting {

        /**
         * Enable CORS filter.
         */
        @WithDefault("true")
        boolean enabled();

        /**
         * Container image to run.
         * Default is resolved in the processor to:
         * ghcr.io/quarkusio/pgvector-quarkus-rag:${quarkus.version}
         */
        Optional<String> image();
    }
}
