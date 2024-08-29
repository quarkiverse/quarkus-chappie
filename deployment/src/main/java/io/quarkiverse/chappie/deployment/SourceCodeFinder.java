package io.quarkiverse.chappie.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceCodeFinder {

    public static String getSourceCode(Path srcMainJava, StackTraceElement stackTraceElement) {

        Path filePath = getSourceCodePath(srcMainJava, stackTraceElement);

        if (filePath != null) {
            try {
                return Files.readString(filePath);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return null;
    }

    public static Path getSourceCodePath(Path srcMainJava, StackTraceElement stackTraceElement) {
        if (stackTraceElement != null) {
            String className = stackTraceElement.getClassName();
            String file = stackTraceElement.getFileName();
            if (className.contains(".")) {
                file = className.substring(0, className.lastIndexOf('.') + 1).replace('.',
                        File.separatorChar)
                        + file;
            }

            return srcMainJava.resolve(file);
        }
        return null;
    }

    public static String getSourceCode(Path filePath) {
        if (filePath != null && Files.exists(filePath)) {
            try {
                return Files.readString(filePath);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return null;
    }

    public static String getSourceCode(Path srcMainJava, String className) {

        Path filePath = getSourceCodePath(srcMainJava, className);

        if (filePath != null && Files.exists(filePath)) {
            try {
                return Files.readString(filePath);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return null;
    }

    public static Path getSourceCodePath(Path srcMainJava, String className) {
        Path filePath = getSourceCodePath(srcMainJava, className, "java");
        if (filePath == null || !Files.exists(filePath)) {
            filePath = getSourceCodePath(srcMainJava, className, "kt");
        }
        return filePath;
    }

    public static Path getSourceCodePath(Path srcMainJava, String className, String type) {
        if (className != null) {
            if (className.contains(".")) {
                String file = className.replace('.',
                        File.separatorChar) + "." + type;
                try {
                    return srcMainJava.resolve(file);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        return null;
    }
}
