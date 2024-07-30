package io.quarkiverse.chappie.deployment;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import io.quarkus.builder.item.SimpleBuildItem;

/**
 * Chat to AI sending all info on the exception
 * TODO: Add more details about the project, like extensions used and other libraries.
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
final class AIAssistantBuildItem extends SimpleBuildItem {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Prompt systemMessagePrompt;
    private final String apiKey;
    private final String modelName;

    public AIAssistantBuildItem(String quarkusVersion, String apiKey, String modelName) {
        this.systemMessagePrompt = systemMessageTemplate.apply(Map.of("version", quarkusVersion));
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    private static final String SYSTEM_MESSAGE = """
                You are an AI assistant helping to debug Java / Kotlin exceptions in a Quarkus {{version}} application.
                You will receive the exception stacktrace and the relevant source that caused the exception.

                Approach this task step-by-step, take your time and do not skip steps.

                Respond with a json file, and only a json file, that is valid, and can ne parsed in Java. Make sure to not include the ``` as the start of the response. The json must contain the following fields from the ingested document:
                    - response (your reply)
                    - explanation (about the diff)
                    - diff (between source and suggested source, to show the changes)
                    - suggestedSource
            """;

    private final PromptTemplate systemMessageTemplate = PromptTemplate
            .from(SYSTEM_MESSAGE);

    private static final String USER_MESSAGE_WITH_SOURCE = """
                I have the following java exception:
                ```
                {{stacktrace}}
                ```

                That comes from this code:
                ```
                {{source}}
                ```

                Please help me fix it.
            """;

    private static final String USER_MESSAGE_WITHOUT_SOURCE = """
                I have the following java exception in my Quarkus app:
                ```
                {{stacktrace}}
                ```

                Please help me fix it.
            """;

    private final PromptTemplate userMessageWithSourceTemplate = PromptTemplate
            .from(USER_MESSAGE_WITH_SOURCE);

    private final PromptTemplate userMessageWithoutSourceTemplate = PromptTemplate
            .from(USER_MESSAGE_WITHOUT_SOURCE);

    public CompletableFuture<SuggestedFix> helpFix(String stacktrace, String source) {
        if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("apiKey")) {
            Prompt userMessagePrompt;
            if (source != null) {
                userMessagePrompt = userMessageWithSourceTemplate.apply(Map.of("stacktrace", stacktrace, "source", source));
            } else {
                userMessagePrompt = userMessageWithoutSourceTemplate.apply(Map.of("stacktrace", stacktrace));
            }

            List<ChatMessage> messages = List.of(systemMessage(systemMessagePrompt.text()),
                    userMessage(userMessagePrompt.text()));

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(0.3)
                    .responseFormat("json_object")
                    .build();

            return CompletableFuture.supplyAsync(() -> {
                Response<AiMessage> response = model.generate(messages);
                try {
                    return objectMapper.readValue(response.content().text(), SuggestedFix.class);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException("Error while parsing the response from AI", ex);
                }
            });
        } else {
            return CompletableFuture.completedFuture(new SuggestedFix(
                    "The quarkus-chappie extension could not assist with this exception",
                    "You need to provide a `quarkus.chappie.api-key` config property that is set to your OpenAI API key",
                    null,
                    null));
        }
    }
}
