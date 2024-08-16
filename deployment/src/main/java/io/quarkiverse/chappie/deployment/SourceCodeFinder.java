package io.quarkiverse.chappie.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceCodeFinder {

    public static String getSourceCode(Path srcMainJava, StackTraceElement stackTraceElement) {
        if (stackTraceElement != null) {
            String className = stackTraceElement.getClassName();
            String file = stackTraceElement.getFileName();
            if (className.contains(".")) {
                file = className.substring(0, className.lastIndexOf('.') + 1).replace('.',
                        File.separatorChar)
                        + file;
            }

            Path filePath = srcMainJava.resolve(file);

            try {
                return Files.readString(filePath);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return null;
    }

}
