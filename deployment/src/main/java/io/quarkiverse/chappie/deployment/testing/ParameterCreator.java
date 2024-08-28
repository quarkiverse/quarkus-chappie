package io.quarkiverse.chappie.deployment.testing;

import java.util.LinkedList;
import java.util.List;

import io.quarkus.builder.Version;

public class ParameterCreator {

    public static Object[] forTestCreation(String source) {
        List<Object> params = new LinkedList<>();
        params.add("Java / Kotlin");
        params.add("Quarkus");
        params.add(Version.getVersion());
        params.add(source);
        return params.toArray();
    }
}
