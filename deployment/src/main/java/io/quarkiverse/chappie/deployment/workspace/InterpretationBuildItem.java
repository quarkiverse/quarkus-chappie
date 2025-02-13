package io.quarkiverse.chappie.deployment.workspace;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * This interprets content. The output will be in markdown
 */
public final class InterpretationBuildItem extends AbstractWorkspaceBuildItem {

    public InterpretationBuildItem(String label, Function<Map<String, String>, ?> action, Optional<Pattern> filter) {
        super(label, action, filter);
    }

}
