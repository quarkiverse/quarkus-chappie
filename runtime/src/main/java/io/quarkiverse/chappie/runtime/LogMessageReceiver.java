package io.quarkiverse.chappie.runtime;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.devui.runtime.logstream.LogStreamBroadcaster;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Receive Log messages, and check if there is an exception
 */
@ApplicationScoped
public class LogMessageReceiver {
    private static final Logger LOGGER = Logger.getLogger(LogMessageReceiver.class);

    @Inject
    LogStreamBroadcaster logStreamBroadcaster;

    @Inject
    ExceptionHistoryService exceptionHistoryService;

    private Map<String, Path> sourceFiles;

    public void setSourcePath(String sourcePath) {
        this.sourceFiles = sourceFiles(sourcePath);
    }

    void onStart(@Observes StartupEvent ev) {
        BroadcastProcessor<JsonObject> logStream = logStreamBroadcaster.getLogStream();

        logStream.subscribe().with((JsonObject item) -> {
            if (item.containsKey(STACKTRACE)) {
                JsonArray stacktrace = item.getJsonArray(STACKTRACE);
                StringBuffer stackTraceBuffer = new StringBuffer();
                if (!stacktrace.isEmpty()) {

                    Set<String> effectedSourceFilesContent = new HashSet<>();

                    for (Iterator iterator = stacktrace.iterator(); iterator.hasNext();) {
                        String exception = (String) iterator.next();
                        stackTraceBuffer.append(exception);
                        String[] lines = exception.split("\n");
                        for (String line : lines) {
                            Optional<Path> relevantSourceFile = getRelevantSourceFile(line);
                            if (relevantSourceFile.isPresent()) {
                                try {
                                    String content = Files.readString(relevantSourceFile.get());
                                    effectedSourceFilesContent.add(content);
                                } catch (IOException ex) {
                                    throw new UncheckedIOException(ex);
                                }
                            }
                        }
                    }

                    // Now we have the stacktrace and the effected files
                    ExceptionDetail exceptionDetail = new ExceptionDetail(LocalDateTime.now(), stackTraceBuffer.toString(),
                            effectedSourceFilesContent);

                    exceptionHistoryService.addExceptionDetail(exceptionDetail);
                }
            }
        });

    }

    private Optional<Path> getRelevantSourceFile(String exceptionline) {
        for (Map.Entry<String, Path> kv : sourceFiles.entrySet()) {
            if (exceptionline.contains(kv.getKey())) {
                return Optional.of(kv.getValue());
            }
        }
        return Optional.empty();
    }

    public Map<String, Path> sourceFiles(String sourcePath) {
        Path root = Paths.get(sourcePath);
        Map<String, Path> javaFilesMap = new HashMap<>();

        try (Stream<Path> stream = Files.walk(root)) {
            javaFilesMap = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toMap(
                            path -> getClassName(root, path),
                            path -> path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return javaFilesMap;
    }

    private static String getClassName(Path startPath, Path filePath) {
        // Remove the start path and the file extension
        Path relativePath = startPath.relativize(filePath);
        String className = relativePath.toString()
                .replace(File.separatorChar, '.')
                .replace(".java", "");
        return className;
    }

    private static final String STACKTRACE = "stacktrace";

}
