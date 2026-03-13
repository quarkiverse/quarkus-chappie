package io.quarkiverse.chappie.runtime.dev;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChappieEnvelope<T>(@JsonProperty(required = true) String niceName, @JsonProperty(required = true) T answer) {
}
