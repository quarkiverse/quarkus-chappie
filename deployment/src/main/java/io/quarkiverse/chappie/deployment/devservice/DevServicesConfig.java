package io.quarkiverse.chappie.deployment.devservice;

import java.net.URL;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface DevServicesConfig {

    /**
     * The default port where the inference server listens for requests
     */
    Optional<Integer> port();

    /**
     * The base url for chappie
     */
    Optional<URL> url();

    /**
     * The process id of the chappie server
     */
    Optional<Long> processId();

    /**
     * Timeout for the request
     */
    @WithDefault("PT120S")
    String timeout();
}
