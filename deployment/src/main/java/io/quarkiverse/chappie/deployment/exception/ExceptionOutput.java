package io.quarkiverse.chappie.deployment.exception;

public record ExceptionOutput(String response, String explanation, String diff, String manipulatedContent) {

}
