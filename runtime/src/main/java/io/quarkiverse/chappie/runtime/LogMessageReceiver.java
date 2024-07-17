package io.quarkiverse.chappie.runtime;

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

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Chappie is listening for exceptions....");

        BroadcastProcessor<JsonObject> logStream = logStreamBroadcaster.getLogStream();

        logStream.subscribe().with((JsonObject item) -> {
            if (item.containsKey(STACKTRACE)) {
                JsonArray stacktrace = item.getJsonArray(STACKTRACE);
                if (!stacktrace.isEmpty()) {
                    System.out.println(">>>>>>> CHAPPIE FOUND AN EXCEPTION !");
                    String fileName = getFileName(item);
                    int lineNumber = item.getInteger(SOURCE_LINE_NUMBER);

                    System.out.println("\t\t " + fileName + " | line " + lineNumber);
                }
            }
        });

    }

    private String getFileName(JsonObject item) {
        String classNameFull = item.getString(SOURCE_CLASS_NAME_FULL);
        String fileName = item.getString(SOURCE_FILE_NAME);
        int lastDotIndex = classNameFull.lastIndexOf('.');

        if (lastDotIndex == -1) {
            return fileName; // No package
        }
        return classNameFull.substring(0, lastDotIndex + 1) + fileName;
    }

    private static final String STACKTRACE = "stacktrace";
    private static final String SOURCE_CLASS_NAME_FULL = "sourceClassNameFull";
    private static final String SOURCE_FILE_NAME = "sourceFileName";
    private static final String SOURCE_LINE_NUMBER = "sourceLineNumber";

}
