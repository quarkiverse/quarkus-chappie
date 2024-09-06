package io.quarkiverse.chappie.deployment.explanation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
import io.quarkiverse.chappie.deployment.ChappieConfig;
import io.quarkiverse.chappie.deployment.ChappiePageBuildItem;
import io.quarkiverse.chappie.deployment.ParameterCreator;
import io.quarkiverse.chappie.deployment.SourceCodeFinder;
import io.quarkiverse.chappie.deployment.devservice.ChappieClient;
import io.quarkiverse.chappie.deployment.devservice.ChappieClientBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
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
            ChappieClientBuildItem chappieClientBuildItem,
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

                        ChappieClient chappieClient = chappieClientBuildItem.getChappieClient();
                        Object[] params = ParameterCreator.getParameters("", sourceCode);
                        CompletableFuture<Object> result = chappieClient.executeRPC("explanation#explain", params);
                        return result;
                    }
                }
                return null;
            });

            buildTimeActionProducer.produce(buildItemActions);
        }
    }
}
