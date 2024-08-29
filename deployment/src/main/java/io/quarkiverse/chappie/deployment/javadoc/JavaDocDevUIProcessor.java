package io.quarkiverse.chappie.deployment.javadoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkiverse.chappie.deployment.ChappieEnabled;
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

@BuildSteps(onlyIf = { IsDevelopment.class, ChappieEnabled.class })
class JavaDocDevUIProcessor {
    private static final String JAVADOC_TITLE = "Create javadoc for your source";

    static volatile Path srcMainJava;
    static volatile List<String> knownClasses;

    @BuildStep
    LastJavaDocBuildItem createLastJavaDocReference() {
        final AtomicReference<Object> lastResponse = new AtomicReference<>();
        final AtomicReference<Path> path = new AtomicReference<>();
        return new LastJavaDocBuildItem(lastResponse, path);
    }

    @BuildStep
    void javaDocPage(BuildProducer<ChappiePageBuildItem> chappiePageBuildItem) {
        chappiePageBuildItem.produce(new ChappiePageBuildItem(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:book")
                .title(JAVADOC_TITLE)
                .componentLink("qwc-chappie-javadoc.js")));
    }

    @BuildStep
    void createBuildTimeActions(BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            LoggingDecorateBuildItem loggingDecorateBuildItem,
            LastJavaDocBuildItem lastJavaDocBuildItem,
            ChappieClientBuildItem chappieClientBuildItem) {

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

        buildItemActions.addAction("addJavaDoc", (Map<String, String> param) -> {
            if (param.containsKey("className")) {
                String className = param.get("className");

                Path sourcePath = SourceCodeFinder.getSourceCodePath(srcMainJava, className);
                String sourceCode = SourceCodeFinder.getSourceCode(sourcePath);

                if (sourceCode != null) {

                    ChappieClient chappieClient = chappieClientBuildItem.getChappieClient();
                    Object[] params = ParameterCreator.getParameters("", "JavaDoc", sourceCode);
                    CompletableFuture<Object> result = chappieClient.executeRPC("doc#addDoc", params);

                    result.thenApply(classWithJavaDoc -> {
                        lastJavaDocBuildItem.getLastResponse().set(classWithJavaDoc);
                        lastJavaDocBuildItem.getPath().set(sourcePath);
                        return classWithJavaDoc;
                    });
                    return result;
                }
            }
            return null;
        });

        buildItemActions.addAction("save", ignored -> {
            String sourceCode = (String) lastJavaDocBuildItem.getLastResponse().get();
            Path srcPath = lastJavaDocBuildItem.getPath().get();

            try {
                Files.createDirectories(srcPath.getParent());
                if (!Files.exists(srcPath))
                    Files.createFile(srcPath);
                Files.writeString(srcPath, sourceCode, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return srcPath;
        });

        buildTimeActionProducer.produce(buildItemActions);
    }

}
