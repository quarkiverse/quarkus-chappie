package io.quarkiverse.chappie.deployment;

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

    }
}
