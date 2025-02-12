package io.quarkiverse.chappie.deployment.workspace;

import java.util.Optional;
import java.util.regex.Pattern;

public record Action(String label, String methodName, ActionType actionType, Optional<Pattern> pattern) {

}
