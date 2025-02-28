package io.quarkiverse.chappie.deployment.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public record LastException(StackTraceElement stackTraceElement, Throwable throwable, String decorateString) {

    public String getStackTraceString() {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }

}
