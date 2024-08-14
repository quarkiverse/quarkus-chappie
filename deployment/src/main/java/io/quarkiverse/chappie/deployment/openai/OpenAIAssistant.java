package io.quarkiverse.chappie.deployment.openai;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import io.quarkiverse.chappie.deployment.Assistant;
import io.quarkiverse.chappie.deployment.SuggestedFix;

/**
 * Help via OpenAI
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
public class OpenAIAssistant implements Assistant {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Prompt systemMessagePrompt;
    private final String apiKey;
    private final String modelName;

    public OpenAIAssistant(String quarkusVersion, String apiKey, String modelName) {
        this.systemMessagePrompt = getSystemMessagePrompt(quarkusVersion);
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    @Override
    public CompletableFuture<SuggestedFix> suggestFix(String stacktrace, String source) {
        List<ChatMessage> messages = getChatMessages(this.systemMessagePrompt, stacktrace, source);

        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.0)
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
    }
}
