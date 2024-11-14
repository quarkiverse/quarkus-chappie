package io.quarkiverse.chappie.deployment.method.exception;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;

/**
 * Main Chappie Processor for exceptions.
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@BuildSteps(onlyIf = IsDevelopment.class)
class ExceptionProcessor {

    @BuildStep
    LastExceptionBuildItem createLastExceptionReference() {
        final AtomicReference<LastException> lastException = new AtomicReference<>();
        return new LastExceptionBuildItem(lastException);
    }

    @BuildStep
    LastSolutionBuildItem createLastSolutionReference() {
        final AtomicReference<Object> lastSuggestedFix = new AtomicReference<>();
        final AtomicReference<Path> path = new AtomicReference<>();
        return new LastSolutionBuildItem(lastSuggestedFix, path);
    }
}
