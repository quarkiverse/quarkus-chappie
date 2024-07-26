package io.quarkiverse.chappie.deployment;

import java.io.PrintWriter;
import java.io.StringWriter;

public record LastException(StackTraceElement stackTraceElement, Throwable throwable) {

    public String getStackTraceString() {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }

}
