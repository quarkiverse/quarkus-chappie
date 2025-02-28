package io.quarkiverse.chappie.deployment.action;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.workspace.Patterns;
import io.quarkus.deployment.dev.ai.workspace.WorkspaceUpdateBuildItem;

@BuildSteps(onlyIf = IsDevelopment.class)
class JavaDocDevUIProcessor {

    @BuildStep
    void createWorkspaceActions(BuildProducer<WorkspaceUpdateBuildItem> workspaceUpdateProducer) {

        workspaceUpdateProducer
                .produce(WorkspaceUpdateBuildItem.builder()
                        .label("Add JavaDoc")
                        .systemMessage(SYSTEM_MESSAGE)
                        .userMessage(USER_MESSAGE)
                        .filter(Patterns.JAVA_ANY)
                        .build());

    }

    private static final String SYSTEM_MESSAGE = "Your job is to add JavaDoc on Class and Method level for the provided code.";
    private static final String USER_MESSAGE = "Please add or modify the JavaDoc to reflect the code. If JavaDoc exist, take that into account when modifying the content";
}
