package io.quarkiverse.chappie.deployment.action;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.workspace.WorkspaceReadBuildItem;

@BuildSteps(onlyIf = IsDevelopment.class)
class ExplanationDevUIProcessor {

    @BuildStep
    void createWorkspaceActions(BuildProducer<WorkspaceReadBuildItem> workspaceReadProducer) {
        workspaceReadProducer
                .produce(WorkspaceReadBuildItem.builder()
                        .label("Explain Code")
                        .userMessage(USER_MESSAGE)
                        .build());
    }

    private static final String USER_MESSAGE = """
             Please explain the provided content. Don't just return the original content, narate an explanation with words.
            """;
}
