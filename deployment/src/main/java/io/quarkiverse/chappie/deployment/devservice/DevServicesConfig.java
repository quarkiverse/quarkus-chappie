package io.quarkiverse.chappie.deployment.devservice;

import java.net.URL;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface DevServicesConfig {

    /**
     * Show the log of the dev service in the application log
     */
    @WithDefault("false")
    boolean log();

    /**
     * The default port where the inference server listens for requests
     */
    Optional<Integer> port();

    /**
     * The version to use
     */
    @WithDefault("0.0.12")
    String version();

    /**
     * The base url for chappie
     */
    Optional<URL> url();

    /**
     * The process id of the chappie server
     */
    Optional<Long> processId();

}
