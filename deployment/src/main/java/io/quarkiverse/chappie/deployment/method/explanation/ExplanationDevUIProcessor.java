package io.quarkiverse.chappie.deployment.method.explanation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.ChappiePageBuildItem;
import io.quarkiverse.chappie.deployment.SourceCodeFinder;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.dev.ai.AIBuildItem;
import io.quarkus.deployment.dev.ai.AIClient;
import io.quarkus.deployment.logging.LoggingDecorateBuildItem;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.Page;

@BuildSteps(onlyIf = IsDevelopment.class)
class ExplanationDevUIProcessor {
    private static final String EXPLANATION_TITLE = "Explain your source";

    static volatile Path srcMainJava;
    static volatile List<String> knownClasses;

    @BuildStep
    void explanationPage(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<ChappiePageBuildItem> chappiePageBuildItem) {

        if (chappieAvailable.isPresent()) {
            chappiePageBuildItem.produce(new ChappiePageBuildItem(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:file-circle-question")
                    .title(EXPLANATION_TITLE)
                    .componentLink("qwc-chappie-explanation.js")));
        }
    }

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            LoggingDecorateBuildItem loggingDecorateBuildItem,
            AIBuildItem aiBuildItem,
            ChappieConfig chappieConfig) {

        if (chappieAvailable.isPresent()) {
            if (srcMainJava == null) {
                srcMainJava = loggingDecorateBuildItem.getSrcMainJava();
            }
            if (knownClasses == null) {
                knownClasses = loggingDecorateBuildItem.getKnowClasses();
            }
            BuildTimeActionBuildItem buildItemActions = new BuildTimeActionBuildItem();

            buildItemActions.addAction("getKnownClasses", ignored -> {
                return knownClasses;
            });

            buildItemActions.addAction("getSourceCode", (Map<String, String> param) -> {
                if (param.containsKey("className")) {
                    String className = param.get("className");
                    Path sourcePath = SourceCodeFinder.getSourceCodePath(srcMainJava, className);
                    return SourceCodeFinder.getSourceCode(sourcePath);
                }
                return null;
            });

            buildItemActions.addAction("explainClass", (Map<String, String> param) -> {
                if (param.containsKey("className")) {
                    String className = param.get("className");

                    Path sourcePath = SourceCodeFinder.getSourceCodePath(srcMainJava, className);
                    String sourceCode = SourceCodeFinder.getSourceCode(sourcePath);

                    if (sourceCode != null) {
                        AIClient aiClient = aiBuildItem.getAIClient();

                        return aiClient.request("explanation", Map.of("source", sourceCode));
                    }
                }
                return null;
            });

            buildTimeActionProducer.produce(buildItemActions);
        }
    }
}
