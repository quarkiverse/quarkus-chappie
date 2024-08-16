package io.quarkiverse.chappie.deployment;

import java.util.LinkedList;
import java.util.List;

import io.quarkus.builder.Version;

public class ParameterCreator {

    public static Object[] forExceptionHelp(String stacktraceString, String sourceString) {
        List<Object> params = new LinkedList<>();
        params.add("Java / Kotlin");
        params.add("Quarkus");
        params.add(Version.getVersion());
        params.add(stacktraceString);
        params.add(sourceString);
        return params.toArray();
    }
}
