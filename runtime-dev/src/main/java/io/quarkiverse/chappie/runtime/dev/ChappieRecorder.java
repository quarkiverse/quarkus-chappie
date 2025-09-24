package io.quarkiverse.chappie.runtime.dev;

import java.util.Map;
import java.util.concurrent.SubmissionPublisher;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ChappieRecorder {

    public RuntimeValue<SubmissionPublisher<String>> createChappieServerManager(BeanContainer beanContainer,
            ChappieAssistant assistant,
            String chappieServerVersion,
            Map<String, String> chappieRAGProperties,
            String devMcpPath) {

        ChappieServerManager chappieServerManager = beanContainer.beanInstance(ChappieServerManager.class);
        return new RuntimeValue(
                chappieServerManager.init(chappieServerVersion, assistant, chappieRAGProperties, devMcpPath));
    }
}
