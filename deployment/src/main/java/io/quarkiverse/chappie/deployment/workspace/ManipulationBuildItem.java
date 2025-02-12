package io.quarkiverse.chappie.deployment.workspace;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * This Build item is used to manipulate existing source in some way using AI. The manipulated source can be used to override
 * the provided source.
 */
public final class ManipulationBuildItem extends AbstractWorkspaceBuildItem {

    public ManipulationBuildItem(String label, Function<Map<String, String>, ?> action, Optional<Pattern> filter) {
        super(label, action, filter);
    }

}
