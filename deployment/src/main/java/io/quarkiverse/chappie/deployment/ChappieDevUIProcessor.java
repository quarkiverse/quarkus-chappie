package io.quarkiverse.chappie.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import io.quarkus.builder.Version;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.dev.ExceptionNotificationBuildItem;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.deployment.pkg.builditem.OutputTargetBuildItem;
import io.quarkus.devui.spi.buildtime.BuildTimeActionBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

class ChappieDevUIProcessor {

    static volatile AtomicReference<LastException> lastExceptionReference;

    @BuildStep(onlyIf = IsDevelopment.class)
    LastExceptionBuildItem createBroadcasters() {
        BroadcastProcessor<LastException> leb = BroadcastProcessor.create();
        return new LastExceptionBuildItem(leb);
    }

    @Consume(ConsoleInstalledBuildItem.class)
    @BuildStep(onlyIf = IsDevelopment.class)
    void setupExceptionHandler(BuildProducer<ExceptionNotificationBuildItem> exceptionNotificationProducer,
            LastExceptionBuildItem lastExceptionBuildItem) {

        lastExceptionReference = ChappieProcessorHelper
                .getLastException(exceptionNotificationProducer);

        exceptionNotificationProducer
                .produce(new ExceptionNotificationBuildItem(new BiConsumer<Throwable, StackTraceElement>() {
                    @Override
                    public void accept(Throwable throwable, StackTraceElement stackTraceElement) {
                        LastException lastException = new LastException(stackTraceElement, throwable);
                        lastExceptionReference.set(lastException);
                        lastExceptionBuildItem.getLastExceptionBroadcastProcessor().onNext(lastException);
                    }
                }));
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    public CardPageBuildItem pages() {
        CardPageBuildItem chappiePage = new CardPageBuildItem();

        chappiePage.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:circle-question")
                .title("Help with the latest exception")
                .componentLink("qwc-chappie-exception.js"));

        return chappiePage;
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    BuildTimeActionBuildItem createBuildTimeActions(CurateOutcomeBuildItem curateOutcomeBuildItem,
            OutputTargetBuildItem outputTargetBuildItem,
            LastExceptionBuildItem lastExceptionBuildItem,
            ChappieConfig chappieConfig) {

        Path srcMainJava = ChappieProcessorHelper.getSourceRoot(curateOutcomeBuildItem.getApplicationModel(),
                outputTargetBuildItem.getOutputDirectory());

        BuildTimeActionBuildItem buildItemActions = new BuildTimeActionBuildItem();

        // This gets the last know exception. For initial load.
        buildItemActions.addAction("getLastException", ignored -> {
            LastException lastException = lastExceptionReference.get();
            if (lastException != null) {
                return lastException;
            }
            return null;
        });

        // This streams exceptions as they happen
        buildItemActions.addSubscription("streamException", ignored -> {
            return lastExceptionBuildItem.getLastExceptionPublisher();
        });

        // TODO: Get last suggested Fix

        buildItemActions.addAction("helpFix", ignored -> {
            LastException lastException = lastExceptionReference.get();
            if (lastException != null) {
                StackTraceElement stackTraceElement = lastException.stackTraceElement();
                String className = stackTraceElement.getClassName();
                String file = stackTraceElement.getFileName();
                if (className.contains(".")) {
                    file = className.substring(0, className.lastIndexOf('.') + 1).replace('.',
                            File.separatorChar)
                            + file;
                }

                AIAssistant aiAssistant = new AIAssistant(Version.getVersion(), chappieConfig.apiKey, chappieConfig.modelName);

                try {
                    Path filePath = srcMainJava.resolve(file);
                    String sourceString = Files.readString(filePath);
                    String stacktraceString = lastException.getStackTraceString();

                    return aiAssistant.helpFix(stacktraceString, sourceString);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            return null;
        });

        return buildItemActions;
    }

}
