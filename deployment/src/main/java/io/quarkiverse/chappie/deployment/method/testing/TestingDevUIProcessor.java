package io.quarkiverse.chappie.deployment.method.testing;

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
class TestingDevUIProcessor {
    private static final String TESTING_TITLE = "Create tests for your source";

    static volatile Path srcMainJava;
    static volatile List<String> knownClasses;

    @BuildStep
    void createLastTestClassReference(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<LastTestClassBuildItem> lastTestClassProducer) {
        if (chappieAvailable.isPresent()) {
            final AtomicReference<Object> lastResponse = new AtomicReference<>();
            final AtomicReference<Path> path = new AtomicReference<>();
            lastTestClassProducer.produce(new LastTestClassBuildItem(lastResponse, path));
        }
    }

    @BuildStep
    void testingPage(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<ChappiePageBuildItem> chappiePageBuildItem) {
        if (chappieAvailable.isPresent()) {
            chappiePageBuildItem.produce(new ChappiePageBuildItem(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:flask-vial")
                    .title(TESTING_TITLE)
                    .componentLink("qwc-chappie-testing.js")));
        }
    }

    @BuildStep
    void createBuildTimeActions(Optional<ChappieAvailableBuildItem> chappieAvailable,
            BuildProducer<BuildTimeActionBuildItem> buildTimeActionProducer,
            LoggingDecorateBuildItem loggingDecorateBuildItem,
            LastTestClassBuildItem lastTestClassBuildItem,
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

            buildItemActions.addAction("suggestTestClass", (Map<String, String> param) -> {
                if (param.containsKey("className")) {
                    String className = param.get("className");

                    Path sourcePath = SourceCodeFinder.getSourceCodePath(srcMainJava, className);
                    String sourceCode = SourceCodeFinder.getSourceCode(sourcePath);

                    if (sourceCode != null) {
                        AIClient aiClient = aiBuildItem.getAIClient();

                        CompletableFuture<String> response = aiClient
                                .request("testing",
                                        Optional.of("Make sure to NOT create a NativeTest, but a normal Quarkus Unit test"),
                                        Map.of("source", sourceCode));

                        response.thenAccept((classWithJavaDoc) -> {
                            lastTestClassBuildItem.getLastResponse().set(classWithJavaDoc);
                            lastTestClassBuildItem.getPath().set(sourcePath);
                        });

                        return response;
                    }
                }
                return null;
            });

            buildItemActions.addAction("saveSuggestion", ignored -> {
                String json = (String) lastTestClassBuildItem.getLastResponse().get();
                Path testPath = createTestPath(lastTestClassBuildItem.getPath().get());
                try (StringReader r = new StringReader(json)) {

                    ObjectMapper mapper = new ObjectMapper();
                    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
                    };

                    try {
                        HashMap<String, Object> object = mapper.readValue(r, typeRef);

                        if (object.containsKey("suggestedTestSource")) {
                            String suggestedTestSource = (String) object.get("suggestedTestSource");

                            Files.createDirectories(testPath.getParent());
                            if (!Files.exists(testPath))
                                Files.createFile(testPath);
                            Files.writeString(testPath, suggestedTestSource, StandardOpenOption.TRUNCATE_EXISTING,
                                    StandardOpenOption.CREATE);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                return testPath;
            });

            buildTimeActionProducer.produce(buildItemActions);
        }
    }

    // TODO: Will this work in Gradle ?
    private Path createTestPath(Path srcPath) {
        String s = srcPath.toString();
        s = s.replace("/main/", "/test/");
        s = s.substring(0, s.length() - 5);
        s = s + "Test.java";
        return Path.of(s);
    }
}
