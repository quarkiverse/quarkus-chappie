package io.quarkiverse.chappie.deployment;

import java.util.LinkedList;
import java.util.List;

import io.quarkus.builder.Version;

public class ParameterCreator {

    public static Object[] getParameters(String... param) {
        List<Object> params = new LinkedList<>();
        params.add("Java / Kotlin");
        params.add("Quarkus");
        params.add(Version.getVersion());
        for (String s : param) {
            params.add(s);
        }
        return params.toArray();
    }

}
