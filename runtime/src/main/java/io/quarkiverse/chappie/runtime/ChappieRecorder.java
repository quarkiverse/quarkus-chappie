package io.quarkiverse.chappie.runtime;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ChappieRecorder {

    public void setup(BeanContainer beanContainer,
            String sourcePath, String version, String apiKey, String modelName) {
        LogMessageReceiver logMessageReceiver = beanContainer.beanInstance(LogMessageReceiver.class);
        logMessageReceiver.setSourcePath(sourcePath);

        AIAssistant aiAssistant = beanContainer.beanInstance(AIAssistant.class);
        aiAssistant.setVersion(version);
        aiAssistant.setApiKey(apiKey);
        aiAssistant.setModelName(modelName);
    }
}
