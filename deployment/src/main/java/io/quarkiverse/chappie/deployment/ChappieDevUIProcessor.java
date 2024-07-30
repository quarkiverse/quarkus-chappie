package io.quarkiverse.chappie.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.dev.ExceptionNotificationBuildItem;
import io.quarkus.deployment.logging.LoggingDecorateBuildItem;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.runtime.logging.DecorateStackUtil;
import io.quarkus.vertx.http.deployment.ErrorPageActionsBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

class ChappieDevUIProcessor {
    private static final String EXCEPTION_TITLE = "Help with the latest exception";

    static volatile Path srcMainJava;
    static volatile List<String> knownClasses;

    @BuildStep(onlyIf = IsDevelopment.class)
    LastExceptionBroadcastBuildItem createBroadcasters() {
        BroadcastProcessor<LastException> leb = BroadcastProcessor.create();
        return new LastExceptionBroadcastBuildItem(leb);
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep(onlyIf = IsDevelopment.class)
    ExceptionNotificationBuildItem setupBroadcaster(LastExceptionBroadcastBuildItem lastExceptionBroadcastBuildItem,
            LastExceptionBuildItem lastExceptionBuildItem) {
        return new ExceptionNotificationBuildItem(new BiConsumer<Throwable, StackTraceElement>() {
            @Override
            public void accept(Throwable throwable, StackTraceElement stackTraceElement) {
                String decorateString = DecorateStackUtil.getDecoratedString(throwable, srcMainJava, knownClasses);
                LastException lastException = new LastException(stackTraceElement, throwable, decorateString);
                lastExceptionBroadcastBuildItem.getLastExceptionBroadcastProcessor().onNext(lastException);
                lastExceptionBuildItem.getLastException().set(lastException);
            }
        });
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    public ErrorPageActionsBuildItem addActionToErrorPage(NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {
        String url = nonApplicationRootPathBuildItem.resolvePath(
                "dev-ui/io.quarkiverse.chappie.quarkus-chappie/" + EXCEPTION_TITLE.replace(" ", "-").toLowerCase());
        return new ErrorPageActionsBuildItem("Get help with this", url);
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    public CardPageBuildItem pages() {
        CardPageBuildItem chappiePage = new CardPageBuildItem();

        chappiePage.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:circle-question")
                .title(EXCEPTION_TITLE)
                .componentLink("qwc-chappie-exception.js"));

        return chappiePage;
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    BuildTimeActionBuildItem createBuildTimeActions(LoggingDecorateBuildItem loggingDecorateBuildItem,
            LastExceptionBroadcastBuildItem lastExceptionBroadcastBuildItem,
            AIAssistantBuildItem assistantBuildItem,
            LastExceptionBuildItem lastExceptionBuildItem) {

        if (srcMainJava == null) {
            srcMainJava = loggingDecorateBuildItem.getSrcMainJava();
        }
        if (knownClasses == null) {
            knownClasses = loggingDecorateBuildItem.getKnowClasses();
        }
        BuildTimeActionBuildItem buildItemActions = new BuildTimeActionBuildItem();

        // This gets the last know exception. For initial load.
        buildItemActions.addAction("getLastException", ignored -> {
            LastException lastException = lastExceptionBuildItem.getLastException().get();
            if (lastException != null) {
                return lastException;
            }
            return null;
        });

        // This streams exceptions as they happen
        buildItemActions.addSubscription("streamException", ignored -> {
            return lastExceptionBroadcastBuildItem.getLastExceptionPublisher();
        });

        // TODO: Get last suggested Fix

        buildItemActions.addAction("helpFix", ignored -> {
            LastException lastException = lastExceptionBuildItem.getLastException().get();
            if (lastException != null) {
                StackTraceElement stackTraceElement = lastException.stackTraceElement();
                String className = stackTraceElement.getClassName();
                String file = stackTraceElement.getFileName();
                if (className.contains(".")) {
                    file = className.substring(0, className.lastIndexOf('.') + 1).replace('.',
                            File.separatorChar)
                            + file;
                }

                try {
                    Path filePath = srcMainJava.resolve(file);
                    String sourceString = Files.readString(filePath);
                    String stacktraceString = lastException.getStackTraceString();

                    return assistantBuildItem.helpFix(stacktraceString, sourceString);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            return null;
        });

        return buildItemActions;
    }

}
