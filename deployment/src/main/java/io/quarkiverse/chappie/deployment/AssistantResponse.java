package io.quarkiverse.chappie.deployment;

import java.util.Map;

import io.quarkiverse.chappie.runtime.dev.JsonObjectCreator;

public record AssistantResponse(Map<String, Object> json) {

    @Override
    public String toString() {
        return JsonObjectCreator.toJsonString(json);
    }
}
