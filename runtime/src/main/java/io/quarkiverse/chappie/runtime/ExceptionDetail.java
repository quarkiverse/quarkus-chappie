package io.quarkiverse.chappie.runtime;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Keeps all info needed to to a AI search on an exception
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
public record ExceptionDetail(LocalDateTime time,
        String stacktrace,
        Set<String> effectedSourceFilesContent) {

}
