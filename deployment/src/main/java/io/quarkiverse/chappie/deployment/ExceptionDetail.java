package io.quarkiverse.chappie.runtime;

import java.util.Set;

/**
 * Keeps all info needed to to a AI search on an exception
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
public record ExceptionDetail(String sequenceNumber,
        String time,
        String message,
        String stacktrace,
        Set<String> effectedSourceFilesContent) {

}
