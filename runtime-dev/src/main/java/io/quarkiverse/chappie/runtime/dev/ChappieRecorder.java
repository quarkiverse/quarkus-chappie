package io.quarkiverse.chappie.runtime.dev;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ChappieRecorder {

    private static final Logger LOG = Logger.getLogger(ChappieRecorder.class);

    public RuntimeValue<SubmissionPublisher<String>> createChappieServerManager(BeanContainer beanContainer,
            ChappieAssistant assistant,
            String chappieServerVersion,
            String devMcpPath) {
        Config config = ConfigProvider.getConfig();
        Map<String, String> chappieRAGProperties = new HashMap<>();

        // Read RAG datasource config from chappie.rag.* keys
        configMap(config, chappieRAGProperties, "chappie.rag.db-kind", "quarkus.datasource.chappie.db-kind");
        configMap(config, chappieRAGProperties, "chappie.rag.jdbc.url", "quarkus.datasource.chappie.jdbc.url");
        configMap(config, chappieRAGProperties, "chappie.rag.username", "quarkus.datasource.chappie.username");
        configMap(config, chappieRAGProperties, "chappie.rag.password", "quarkus.datasource.chappie.password");
        configMap(config, chappieRAGProperties, "chappie.rag.active", "quarkus.datasource.chappie.active");

        if (chappieRAGProperties.isEmpty()) {
            LOG.debug("No RAG datasource configuration found - RAG will be disabled");
        } else {
            LOG.debugf("RAG datasource configuration found: %d properties", chappieRAGProperties.size());
        }

        ChappieServerManager chappieServerManager = beanContainer.beanInstance(ChappieServerManager.class);
        return new RuntimeValue(chappieServerManager.init(chappieServerVersion, assistant, chappieRAGProperties, devMcpPath));
    }

    void configMap(Config config, Map<String, String> map, String sourceKey, String targetKey) {
        config.getOptionalValue(sourceKey, String.class).ifPresent(value -> map.put(targetKey, value));
    }
}
