package io.quarkiverse.chappie.deployment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.bootstrap.workspace.WorkspaceModule;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.dev.ExceptionNotificationBuildItem;

public class ChappieProcessorHelper {

    public static AtomicReference<LastException> getLastException(
            BuildProducer<ExceptionNotificationBuildItem> exceptionNotificationProducer) {
        final AtomicReference<LastException> lastException = new AtomicReference<>();

        exceptionNotificationProducer
                .produce(new ExceptionNotificationBuildItem(new BiConsumer<Throwable, StackTraceElement>() {
                    @Override
                    public void accept(Throwable throwable, StackTraceElement stackTraceElement) {
                        lastException.set(new LastException(stackTraceElement, throwable));
                    }
                }));

        return lastException;
    }

    public static Path getSourceRoot(ApplicationModel applicationModel, Path target) {
        WorkspaceModule workspaceModule = applicationModel.getAppArtifact().getWorkspaceModule();
        if (workspaceModule != null) {
            return workspaceModule.getModuleDir().toPath().resolve(SRC_MAIN_JAVA);
        }

        if (target != null) {
            var baseDir = target.getParent();
            if (baseDir == null) {
                baseDir = target;
            }
            return baseDir.resolve(SRC_MAIN_JAVA);
        }
        return Paths.get(SRC_MAIN_JAVA);
    }

    private static final String SRC_MAIN_JAVA = "src/main/java";
}
