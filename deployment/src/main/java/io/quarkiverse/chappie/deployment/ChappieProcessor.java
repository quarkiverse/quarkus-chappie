package io.quarkiverse.chappie.deployment;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.quarkiverse.chappie.runtime.AIAssistant;
import io.quarkiverse.chappie.runtime.ChappieRecorder;
import io.quarkiverse.chappie.runtime.ExceptionHistoryService;
import io.quarkiverse.chappie.runtime.LogMessageReceiver;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.bootstrap.model.ApplicationModel;
import io.quarkus.bootstrap.workspace.WorkspaceModule;
import io.quarkus.builder.Version;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationArchivesBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.deployment.pkg.builditem.OutputTargetBuildItem;

class ChappieProcessor {

    private static final String FEATURE = "chappie";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClasses(LogMessageReceiver.class, ExceptionHistoryService.class, AIAssistant.class)
                .setDefaultScope(BuiltinScope.APPLICATION.getName())
                .setUnremovable().build());
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(ExecutionTime.STATIC_INIT)
    void setSourcePathAndVersion(ChappieRecorder recorder, ChappieConfig chappieConfig,
            BeanContainerBuildItem beanContainerBuildItem,
            ApplicationArchivesBuildItem applicationArchives, CurateOutcomeBuildItem curateOutcomeBuildItem,
            OutputTargetBuildItem outputTargetBuildItem) {

        Path srcMainJava = getSourceRoot(curateOutcomeBuildItem.getApplicationModel(),
                outputTargetBuildItem.getOutputDirectory());

        if (srcMainJava != null) {
            recorder.setup(beanContainerBuildItem.getValue(),
                    srcMainJava.toAbsolutePath().toString(),
                    Version.getVersion(),
                    chappieConfig.apiKey,
                    chappieConfig.modelName);
        }
    }

    private Path getSourceRoot(ApplicationModel applicationModel, Path target) {
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
