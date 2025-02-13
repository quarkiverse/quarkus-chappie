package io.quarkiverse.chappie.deployment;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ContentIO {

    public static String writeContent(String path, String contents) {
        try {
            return ContentIO.writeContent(Paths.get(new URI(path)), contents);
        } catch (URISyntaxException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    public static String writeContent(Path path, String contents) {
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path))
                Files.createFile(path);
            Files.writeString(path, contents, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
            return path.toString();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String readContents(String path) {
        try {
            return ContentIO.readContents(Paths.get(new URI(path)));
        } catch (URISyntaxException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }
    }

    public static String readContents(Path filePath) {
        if (filePath != null && Files.exists(filePath)) {
            try {
                return Files.readString(filePath);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return null;
    }

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
