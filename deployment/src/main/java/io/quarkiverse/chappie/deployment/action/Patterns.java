package io.quarkiverse.chappie.deployment.action;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Common patterns for filters
 */
public interface Patterns {
    public static Optional<Pattern> JAVA_ANY = Optional.of(Pattern.compile(".*\\.java$"));
    public static Optional<Pattern> JAVA_SRC = Optional.of(Pattern.compile("^((?!test).)*\\.java$"));
    public static Optional<Pattern> JAVA_TEST = Optional.of(Pattern.compile("^.*test.*\\.java$"));

    public static Optional<Pattern> README_MD = Optional.of(Pattern.compile("^README\\.md$"));

    public static Optional<Pattern> POM_XML = Optional.of(Pattern.compile("^pom\\.xml$"));
    public static Optional<Pattern> APPLICATION_PROPERTIES = Optional.of(Pattern.compile("^.*application\\.properties$"));

    public static Optional<Pattern> DOCKER_FILE = Optional.of(Pattern.compile("^.*Dockerfile(\\..*)?$"));

    public static Optional<Pattern> SHELL_SCRIPT = Optional.of(Pattern.compile(".*\\.(sh|bash|zsh|ksh)$"));

    public static Optional<Pattern> HTML = Optional.of(Pattern.compile(".*\\.(html|htm)$"));
    public static Optional<Pattern> CSS = Optional.of(Pattern.compile(".*\\.css$"));
    public static Optional<Pattern> JS = Optional.of(Pattern.compile(".*\\.js$"));
    public static Optional<Pattern> XML = Optional.of(Pattern.compile(".*\\.xml$"));
    public static Optional<Pattern> PROPERTIES = Optional.of(Pattern.compile(".*\\.properties$"));
    public static Optional<Pattern> YAML = Optional.of(Pattern.compile(".*\\.(yaml|yml)$"));
}
