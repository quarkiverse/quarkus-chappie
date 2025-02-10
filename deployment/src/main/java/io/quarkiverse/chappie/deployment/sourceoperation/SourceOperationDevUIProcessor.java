package io.quarkiverse.chappie.deployment.sourceoperation;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
import io.quarkus.deployment.logging.LoggingDecorateBuildItem;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.Page;

@BuildSteps(onlyIf = IsDevelopment.class)
class SourceOperationDevUIProcessor {

    static volatile Path srcMainJava;
    static volatile List<KnownClass> knownClasses;

    @BuildStep
    void sourceOperationPage(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<ChappiePageBuildItem> chappiePageBuildItem) {

        if (chappieAvailable.isPresent()) {
            chappiePageBuildItem.produce(new ChappiePageBuildItem(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:code")
                    .title("Source code")
                    .componentLink("qwc-chappie-source-code.js")));
        }
    }

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            List<SourceManipulationBuildItem> sourceManipulationBuildItems,
            List<SourceGenerationBuildItem> sourceGenerationBuildItems,
            BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            LoggingDecorateBuildItem loggingDecorateBuildItem,
            AIBuildItem aiBuildItem,
            ChappieConfig chappieConfig) {

        if (chappieAvailable.isPresent()) {
            if (srcMainJava == null) {
                srcMainJava = loggingDecorateBuildItem.getSrcMainJava();
            }
            if (knownClasses == null) {
                knownClasses = new ArrayList<>();
                for (String knownClass : loggingDecorateBuildItem.getKnowClasses()) {
                    Path sourcePath = SourceCodeFinder.getSourceCodePath(srcMainJava, knownClass);
                    knownClasses.add(new KnownClass(knownClass, sourcePath));
                }
            }

            // Actions
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

            buildItemActions.addAction("save", (Map<String, String> param) -> {
                if (param.containsKey("sourceCode")) {
                    String sourceCode = param.get("sourceCode");
                    String uriString = param.get("path");
                    Path srcPath = Path.of(URI.create(uriString));
                    try {
                        Files.createDirectories(srcPath.getParent());
                        if (!Files.exists(srcPath))
                            Files.createFile(srcPath);
                        Files.writeString(srcPath, sourceCode, StandardOpenOption.TRUNCATE_EXISTING,
                                StandardOpenOption.CREATE);
                    } catch (IOException ex) {
                        return new SavedResult(srcPath, false, ex.getMessage());
                    }
                    return new SavedResult(srcPath, true, null);
                }
                throw new RuntimeException("Invalid input");
            });

            List<SourceAction> sourceActions = new ArrayList<>();
            // Source manipulations
            for (SourceManipulationBuildItem sourceManipulation : sourceManipulationBuildItems) {
                sourceActions
                        .add(new SourceAction(sourceManipulation.getLabel(), sourceManipulation.getMethodName(),
                                Action.Manipulation));
                buildItemActions.addAction(sourceManipulation.getMethodName(), sourceManipulation.getAction());
            }

            // Source generation
            for (SourceGenerationBuildItem sourceGeneration : sourceGenerationBuildItems) {
                sourceActions
                        .add(new SourceAction(sourceGeneration.getLabel(), sourceGeneration.getMethodName(),
                                Action.Generation));
                buildItemActions.addAction(sourceGeneration.getMethodName(), sourceGeneration.getAction());
            }

            buildItemActions.addAction("getSourceActions", (Map<String, String> param) -> {
                return sourceActions;
            });

            buildTimeActionProducer.produce(buildItemActions);
        }
    }

    static record KnownClass(String className, Path path) {
    }

    static record SavedResult(Path path, boolean success, String errorMessage) {
    }

}
