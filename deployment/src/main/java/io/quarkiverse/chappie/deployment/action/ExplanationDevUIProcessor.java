package io.quarkiverse.chappie.deployment.action;

import java.util.Optional;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.workspace.InterpretationWorkspaceActionBuildItem;

@BuildSteps(onlyIf = IsDevelopment.class)
class ExplanationDevUIProcessor {

    @BuildStep
    void createBuildTimeActions(BuildProducer<InterpretationWorkspaceActionBuildItem> interpretationActionProducer) {
        interpretationActionProducer
                .produce(new InterpretationWorkspaceActionBuildItem("Explain", Optional.empty(), USER_MESSAGE,
                        Optional.empty()));
    }

    private static final String USER_MESSAGE = """
             Please explain the provided content. Don't just return the original content, narate an explanation with words.
            """;
}
