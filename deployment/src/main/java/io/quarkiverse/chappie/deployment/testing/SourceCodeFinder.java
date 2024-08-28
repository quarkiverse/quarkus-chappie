package io.quarkiverse.chappie.deployment.testing;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceCodeFinder {

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
