package io.quarkiverse.chappie.deployment;

import java.util.Map;

public record AssistantResponse(Map<String, Object> json) {

    @Override
    public String toString() {
        return JsonObjectCreator.toJsonString(json);
    }
}