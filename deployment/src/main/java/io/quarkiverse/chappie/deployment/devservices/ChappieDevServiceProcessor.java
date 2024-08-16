package io.quarkiverse.chappie.deployment.devservices;

import java.util.Map;
import java.util.Optional;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.chappie.deployment.ChappieClient;
import io.quarkiverse.chappie.deployment.ChappieClientBuildItem;
import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.ChappieEnabled;
import io.quarkiverse.chappie.deployment.Feature;
import io.quarkiverse.chappie.deployment.LLM;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;

@BuildSteps(onlyIf = { IsDevelopment.class, ChappieEnabled.class, GlobalDevServicesConfig.Enabled.class })
public class ChappieDevServiceProcessor {

    @BuildStep
    public void createContainer(BuildProducer<DevServicesResultBuildItem> devServicesResultProducer,
            BuildProducer<ChappieClientBuildItem> chappieClientProducer,
            ChappieConfig config) {

        DockerImageName dockerImageName = DockerImageName
                .parse(config.devservices().imageName());

        ChappieContainer container = new ChappieContainer(dockerImageName, config.devservices().port());
        if (config.llm().get().equals(LLM.openai)) {
            container = container.withEnv("quarkus.langchain4j.openai.api-key", config.openai().apiKey().get());
        }
        container.start();

        String jsonRpcBase = "http://" + container.getHost() + ":" + container.getPort();

        Map<String, String> props = Map.of(
                "quarkus.assistant.devservices.url", jsonRpcBase);

        devServicesResultProducer
                .produce(new DevServicesResultBuildItem.RunningDevService(Feature.FEATURE, container.getContainerId(),
                        container::close, props)
                        .toBuildItem());

        ChappieClient chappieClient = new ChappieClient(jsonRpcBase);
        chappieClient.connect();

        chappieClientProducer.produce(new ChappieClientBuildItem(chappieClient));
    }

    private static class ChappieContainer extends GenericContainer<ChappieContainer> {
        static final int CONTAINER_PORT = 4315;

        public ChappieContainer(DockerImageName image, Optional<Integer> port) {
            super(image);
            if (port.isPresent()) {
                super.addFixedExposedPort(port.get(), CONTAINER_PORT);
            }
        }

        @Override
        protected void configure() {
            withNetwork(Network.SHARED);
            addExposedPorts(CONTAINER_PORT);
        }

        public Integer getPort() {
            return this.getMappedPort(CONTAINER_PORT);
        }
    }

}
