package io.quarkiverse.chappie.deployment;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.builder.Version;

public class JsonObjectCreator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ObjectNode commonInputNode;

    static {
        commonInputNode = objectMapper.createObjectNode();
        commonInputNode.put("programmingLanguage", "Java / Kotlin"); // TODO: Find a clean way to decide if this is Kotlin or Java
        commonInputNode.put("programmingLanguageVersion", System.getProperty("java.version"));
        commonInputNode.put("product", "Quarkus");
        commonInputNode.put("productVersion", Version.getVersion());
    }

    public static String getInput(Optional<String> extraContent, Map<String, String> params) {
        try {
            ObjectNode inputNode = objectMapper.createObjectNode();
            inputNode.set("commonInput", commonInputNode);
            if (extraContent.isPresent()) {
                inputNode.put("extraContent", extraContent.get());
            }

            for (Map.Entry<String, String> kv : params.entrySet()) {
                inputNode.put(kv.getKey(), kv.getValue());
            }
            return objectMapper.writeValueAsString(inputNode);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getInput(String systemMessage, String userMessage, Map<String, String> params) {
        try {

            ObjectNode genericInputNode = commonInputNode.deepCopy();

            genericInputNode.put("systemmessage", systemMessage);
            genericInputNode.put("usermessage", userMessage);

            ObjectNode inputNode = objectMapper.createObjectNode();
            inputNode.set("genericInput", genericInputNode);

            for (Map.Entry<String, String> kv : params.entrySet()) {
                inputNode.put(kv.getKey(), kv.getValue());
            }
            return objectMapper.writeValueAsString(inputNode);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> T getOutput(String json, Class<T> responseType) {
        try {
            return objectMapper.readValue(json, responseType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

}
