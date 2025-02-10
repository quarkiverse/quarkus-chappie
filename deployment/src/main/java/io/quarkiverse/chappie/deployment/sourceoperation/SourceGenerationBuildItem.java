package io.quarkiverse.chappie.deployment.sourceoperation;

import java.util.Map;
import java.util.function.Function;

/**
 * This generates new source using existing source as input. The generated source can be save at a new location.
 */
public final class SourceGenerationBuildItem extends AbstractSourceOperationBuildItem {

    public SourceGenerationBuildItem(String label, Function<Map<String, String>, ?> action) {
        super(label, action);
    }

}
