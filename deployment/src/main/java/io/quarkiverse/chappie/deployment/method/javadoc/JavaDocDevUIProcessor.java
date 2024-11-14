package io.quarkiverse.chappie.deployment.method.javadoc;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.chappie.deployment.ChappieAvailableBuildItem;
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
class JavaDocDevUIProcessor {
    private static final String JAVADOC_TITLE = "Create javadoc for your source";

    static volatile Path srcMainJava;
    static volatile List<String> knownClasses;

    @BuildStep
    void createLastJavaDocReference(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<LastJavaDocBuildItem> lastJavaDocProducer) {
        if (chappieAvailable.isPresent()) {
            final AtomicReference<Object> lastResponse = new AtomicReference<>();
            final AtomicReference<Path> path = new AtomicReference<>();
            lastJavaDocProducer.produce(new LastJavaDocBuildItem(lastResponse, path));
        }
    }

    @BuildStep
    void javaDocPage(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<ChappiePageBuildItem> chappiePageBuildItem) {
        if (chappieAvailable.isPresent()) {
            chappiePageBuildItem.produce(new ChappiePageBuildItem(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:book")
                    .title(JAVADOC_TITLE)
                    .componentLink("qwc-chappie-javadoc.js")));
        }
    }

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            LoggingDecorateBuildItem loggingDecorateBuildItem,
            LastJavaDocBuildItem lastJavaDocBuildItem,
            AIBuildItem aiBuildItem) {
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

            buildItemActions.addAction("addJavaDoc", (Map<String, String> param) -> {
                if (param.containsKey("className")) {
                    String className = param.get("className");

                    Path sourcePath = SourceCodeFinder.getSourceCodePath(srcMainJava, className);
                    String sourceCode = SourceCodeFinder.getSourceCode(sourcePath);

                    if (sourceCode != null) {

                        AIClient aiClient = aiBuildItem.getAIClient();

                        CompletableFuture<String> response = aiClient
                                .request("doc", Map.of("doc", "JavaDoc", "source", sourceCode));

                        response.thenAccept((classWithJavaDoc) -> {
                            lastJavaDocBuildItem.getLastResponse().set(classWithJavaDoc);
                            lastJavaDocBuildItem.getPath().set(sourcePath);
                        });

                        return response;
                    }
                }
                return null;
            });

            buildItemActions.addAction("save", ignored -> {
                String json = (String) lastJavaDocBuildItem.getLastResponse().get();
                Path srcPath = lastJavaDocBuildItem.getPath().get();
                try (StringReader r = new StringReader(json)) {

                    ObjectMapper mapper = new ObjectMapper();
                    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
                    };

                    try {
                        HashMap<String, Object> object = mapper.readValue(r, typeRef);

                        if (object.containsKey("sourceWithDoc")) {
                            String sourceWithDoc = (String) object.get("sourceWithDoc");

                            Files.createDirectories(srcPath.getParent());
                            if (!Files.exists(srcPath))
                                Files.createFile(srcPath);
                            Files.writeString(srcPath, sourceWithDoc, StandardOpenOption.TRUNCATE_EXISTING,
                                    StandardOpenOption.CREATE);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
                return srcPath;
            });

            buildTimeActionProducer.produce(buildItemActions);
        }
    }

}
