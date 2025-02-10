package io.quarkiverse.chappie.deployment.sourceoperation;

import java.util.Map;
import java.util.function.Function;

/**
 * This Build item is used to manipulate existing source in some way using AI. The manipulated source can be used to override
 * the provided source.
 */
public final class SourceManipulationBuildItem extends AbstractSourceOperationBuildItem {

    public SourceManipulationBuildItem(String label, Function<Map<String, String>, ?> action) {
        super(label, action);
    }

}
