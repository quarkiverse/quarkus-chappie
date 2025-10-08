package io.quarkiverse.chappie.runtime.dev;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ChappieRecorder {

    public RuntimeValue<SubmissionPublisher<String>> createChappieServerManager(BeanContainer beanContainer,
            ChappieAssistant assistant, String chappieServerVersion) {
        Config config = ConfigProvider.getConfig();
        Map<String, String> chappieRAGProperties = new HashMap<>();
        configMap(config, chappieRAGProperties, "quarkus.datasource.chappie.db-kind");
        configMap(config, chappieRAGProperties, "quarkus.datasource.chappie.jdbc.url");
        configMap(config, chappieRAGProperties, "quarkus.datasource.chappie.username");
        configMap(config, chappieRAGProperties, "quarkus.datasource.chappie.password");
        configMap(config, chappieRAGProperties, "quarkus.datasource.chappie.active");

        ChappieServerManager chappieServerManager = beanContainer.beanInstance(ChappieServerManager.class);
        return new RuntimeValue(chappieServerManager.init(chappieServerVersion, assistant, chappieRAGProperties));
    }

    void configMap(Config config, Map<String, String> map, String key) {
        map.put(key, config.getValue(key, String.class));
    }
}
