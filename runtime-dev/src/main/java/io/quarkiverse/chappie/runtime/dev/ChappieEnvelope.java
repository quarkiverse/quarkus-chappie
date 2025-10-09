package io.quarkiverse.chappie.runtime.dev;

public record ChappieEnvelope<T>(String niceName, T answer) {
}