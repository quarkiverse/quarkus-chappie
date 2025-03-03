package io.quarkiverse.chappie.deployment.action;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.assistant.workspace.WorkspaceReadBuildItem;

@BuildSteps(onlyIf = IsDevelopment.class)
class ExplanationDevUIProcessor {

    @BuildStep
    void createWorkspaceActions(BuildProducer<WorkspaceReadBuildItem> workspaceReadProducer) {
        workspaceReadProducer
                .produce(WorkspaceReadBuildItem.builder()
                        .label("Explain")
                        .userMessage(USER_MESSAGE)
                        .build());
    }

    private static final String USER_MESSAGE = """
             Please explain the provided content. Talk thought what type of content it is and what it does. If possible talk about how it might be used.
            """;
}
