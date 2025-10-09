package io.quarkiverse.chappie.runtime.dev;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;

import io.quarkus.builder.Version;

public class JsonObjectCreator {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final ObjectNode commonInputNode;

    static {
        commonInputNode = objectMapper.createObjectNode();
        commonInputNode.put("programmingLanguage", "Java"); // TODO: Find a clean way to decide if this is Kotlin or Java
        commonInputNode.put("programmingLanguageVersion", System.getProperty("java.version"));
        commonInputNode.put("quarkusVersion", Version.getVersion());
    }

    public static String getWorkspaceInput(String systemmessageTemplate, String usermessageTemplate,
            Map<String, String> variables, List<Path> paths, Class<?> responseType) {
        return getInput(systemmessageTemplate, usermessageTemplate, variables, Map.of("paths", paths), responseType);
    }

    public static String getInput(String systemmessageTemplate, String usermessageTemplate, Map<String, String> variables,
            Map<String, Object> params, Class<?> responseType) {
        try {

            ObjectNode genericInputNode = commonInputNode.deepCopy();

            genericInputNode.put("systemmessageTemplate", systemmessageTemplate);
            genericInputNode.put("usermessageTemplate", usermessageTemplate);
            ObjectNode variablesAsNode = objectMapper.valueToTree(variables);
            genericInputNode.set("variables", variablesAsNode);

            ObjectNode inputNode = objectMapper.createObjectNode();
            inputNode.set("genericInput", genericInputNode);
            ObjectNode paramsAsNode = objectMapper.valueToTree(params);
            inputNode.setAll(paramsAsNode);

            if (responseType != null) {
                inputNode.put("responseSchemaPrompt", buildResponsePrompt(responseType));
            }
            return objectMapper.writeValueAsString(inputNode);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> ChappieEnvelope<T> getEnvelopeOutput(String json, Class<T> answerClass) {
        try {
            TypeFactory tf = objectMapper.getTypeFactory();
            JavaType inner = tf.constructType(answerClass);
            JavaType envelope = tf.constructParametricType(ChappieEnvelope.class, inner);
            return objectMapper.readValue(json, envelope);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response \n" + json, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ChappieEnvelope<T> getEnvelopeOutput(String json, Type answerType) {
        try {
            TypeFactory tf = objectMapper.getTypeFactory();
            JavaType inner = tf.constructType(answerType);
            JavaType envelope = tf.constructParametricType(ChappieEnvelope.class, inner);
            return (ChappieEnvelope<T>) objectMapper.readValue(json, envelope);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response \n" + json, e);
        }
    }

    public static List<Map> getList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response \n" + json, e);
        }
    }

    public static Map getMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response \n" + json, e);
        }
    }

    public static String toJsonString(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Small helper to produce a ready-to-send prompt block
     * TODO: We can cache this
     */
    private static String buildResponsePrompt(Type answerType) {
        JsonNode jsonSchema = SCHEMA_GENERATOR.generateSchema(answerType);

        return """
                RESPONSE FORMAT (strict):
                - "niceName": string label you provide
                    - Derive it from the user’s current request and/or early conversation context.
                    - Only use the contents in the [USER PROMPT]
                    - Constraints: ≤ 60 chars, 4–9 words, Title Case, no quotes/backticks, no trailing punctuation.
                    - Make it specific (action + topic), e.g., "Fix WebSocket Reconnect in Quarkus".
                - "answer": Must match and validates against the following JSON Schema (Draft 2020-12). Do not include extra fields.
                    - JSON Scheme:
                        %s
                """
                .formatted(jsonSchema.toString());
    }

    private static final SchemaGenerator SCHEMA_GENERATOR = new SchemaGenerator(
            new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12)
                    .with(new JacksonModule())
                    .without(Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS)
                    .without(Option.GETTER_METHODS)
                    .without(Option.NONSTATIC_NONVOID_NONGETTER_METHODS)
                    .without(Option.STATIC_METHODS)
                    .without(Option.VOID_METHODS).build());

}
