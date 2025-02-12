package io.quarkiverse.chappie.deployment.workspace;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * This generates new source using existing source as input. The generated source can be save at a new location.
 */
public final class GenerationBuildItem extends AbstractWorkspaceBuildItem {

    public GenerationBuildItem(String label, Function<Map<String, String>, ?> action, Optional<Pattern> filter) {
        super(label, action, filter);
    }

}
