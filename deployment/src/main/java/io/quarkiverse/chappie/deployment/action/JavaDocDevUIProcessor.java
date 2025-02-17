package io.quarkiverse.chappie.deployment.action;

import java.util.Optional;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.workspace.ManipulationWorkspaceActionBuildItem;

@BuildSteps(onlyIf = IsDevelopment.class)
class JavaDocDevUIProcessor {

    @BuildStep
    void createBuildTimeActions(BuildProducer<ManipulationWorkspaceActionBuildItem> manipulationProducer) {

        manipulationProducer
                .produce(new ManipulationWorkspaceActionBuildItem("Add JavaDoc", Optional.of(SYSTEM_MESSAGE), USER_MESSAGE,
                        Patterns.JAVA_ANY));

    }

    private static final String SYSTEM_MESSAGE = "Your job is to add JavaDoc on Class and Method level for the provided code.";
    private static final String USER_MESSAGE = "Please add or modify the JavaDoc to reflect the code. If JavaDoc exist, take that into account when modifying the content";
}
