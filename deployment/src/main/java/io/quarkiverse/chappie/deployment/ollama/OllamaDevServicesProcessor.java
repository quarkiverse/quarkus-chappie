package io.quarkiverse.chappie.deployment.ollama;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;

@BuildSteps(onlyIf = { IsDevelopment.class, OllamaDevServicesEnabled.class, GlobalDevServicesConfig.Enabled.class })
public class OllamaDevServicesProcessor {

    private final static Logger LOGGER = Logger.getLogger(OllamaDevServicesProcessor.class);

    @BuildStep
    private void handleModels(LoggingSetupBuildItem loggingSetupBuildItem,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            LaunchModeBuildItem launchMode,
            ChappieConfig config,
            BuildProducer<DevServicesResultBuildItem> producer,
            BuildProducer<OllamaBuildItem> ollamaProducer) {

        OllamaClient client = OllamaClient.create(new OllamaClient.Options("localhost", config.ollama().port()));
        try {
            Set<ModelName> localModels = client.localModels().stream().map(mi -> ModelName.of(mi.name()))
                    .collect(Collectors.toSet());

            Optional<String> modelToPull;
            if (localModels.contains(ModelName.of(config.ollama().modelName()))) {
                LOGGER.debug("Ollama already has model " + config.ollama().modelName() + " pulled locally");
                modelToPull = Optional.empty();
            } else {
                LOGGER.debug("Need to pull the following model into Ollama server: " + config.ollama().modelName());
                modelToPull = Optional.of(config.ollama().modelName());
            }

            AtomicReference<String> clientThreadName = new AtomicReference<>();
            StartupLogCompressor compressor = new StartupLogCompressor(
                    (launchMode.isTest() ? "(test) " : "") + "Ollama model pull:", consoleInstalledBuildItem,
                    loggingSetupBuildItem,
                    // ensure that the progress logging done on the async thread is also caught by the compressor
                    thread -> {
                        String t = clientThreadName.get();
                        if (t == null) {
                            return false;
                        }
                        return thread.getName().equals(t);
                    });
            if (modelToPull.isPresent()) {
                String model = modelToPull.get();
                // we pull one model at a time and provide progress updates to the user via logging
                LOGGER.info("Pulling model " + model);

                CompletableFuture<Void> cf = new CompletableFuture<>();
                client.pullAsync(model).subscribe(new Flow.Subscriber<>() {

                    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        subscription.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(OllamaClient.PullAsyncLine line) {
                        clientThreadName.compareAndSet(null, Thread.currentThread().getName());
                        if ((line.total() != null) && (line.completed() != null) && (line.status() != null)
                                && line.status().contains("pulling")) {
                            BigDecimal percentage = new BigDecimal(line.completed()).divide(new BigDecimal(line.total()), 4,
                                    RoundingMode.HALF_DOWN).multiply(ONE_HUNDRED);
                            BigDecimal progress = percentage.setScale(2, RoundingMode.HALF_DOWN);
                            if (progress.compareTo(ONE_HUNDRED) >= 0) {
                                // avoid showing 100% for too long
                                LOGGER.infof("Verifying and cleaning up\n", progress);
                            } else {
                                LOGGER.infof("Progress: %s%%\n", progress);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        cf.completeExceptionally(throwable);
                    }

                    @Override
                    public void onComplete() {
                        cf.complete(null);
                    }
                });

                // TODO: Let Quarkus start up, and just show the progress in dev ui / cli
                try {
                    cf.get(5, TimeUnit.MINUTES);
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    compressor.closeAndDumpCaptured();
                    throw new RuntimeException(e.getCause());
                }
            }

            // preload model - it only makes sense to load a single model
            if (config.ollama().preload()) {
                String modelName = config.ollama().modelName();
                LOGGER.infof("Preloading model %s", modelName);
                client.preloadChatModel(modelName);
            }

            compressor.close();

            String ollamaBaseUrl = String.format("http://localhost:%d", config.ollama().port());

            Map<String, String> modelBaseUrls = Map.of("baseUrl", ollamaBaseUrl);

            DevServicesResultBuildItem buildItem = new DevServicesResultBuildItem("chappie-ollama",
                    "Olama for chappie. We expect a running ollama, but will manage models automatically",
                    null,
                    modelBaseUrls);

            producer.produce(buildItem);
            ollamaProducer.produce(new OllamaBuildItem(ollamaBaseUrl));
        } catch (OllamaClient.ServerUnavailableException e) {
            LOGGER.warn(e.getMessage()
                    + " therefore no dev service will be started. Ollama can be installed via https://ollama.com/download");
            return;
        }
    }

    private record ModelName(String model, String tag) {

        public static ModelName of(String modelName) {
            Objects.requireNonNull(modelName, "modelName cannot be null");
            String[] parts = modelName.split(":");
            if (parts.length == 1) {
                return new ModelName(modelName, "latest");
            } else if (parts.length == 2) {
                return new ModelName(parts[0], parts[1]);
            } else {
                throw new IllegalArgumentException("Invalid model name: " + modelName);
            }
        }

    }
}
