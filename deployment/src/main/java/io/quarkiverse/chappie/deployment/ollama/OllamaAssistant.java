package io.quarkiverse.chappie.deployment.ollama;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import io.quarkiverse.chappie.deployment.Assistant;
import io.quarkiverse.chappie.deployment.SuggestedFix;

/**
 * Help via Ollama
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
public class OllamaAssistant implements Assistant {
    private final static Logger LOGGER = Logger.getLogger(OllamaAssistant.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Prompt systemMessagePrompt;
    private final int port;
    private final String modelName;

    public OllamaAssistant(String quarkusVersion, int port, String modelName) {
        this.systemMessagePrompt = systemMessageTemplate.apply(Map.of("version", quarkusVersion));
        this.port = port;
        this.modelName = modelName;
    }

    public CompletableFuture<SuggestedFix> suggestFix(String stacktrace, String source) {
        List<ChatMessage> messages = getChatMessages(this.systemMessagePrompt, stacktrace, source);

        LOGGER.info("Model created with [" + baseUrl() + "]");
        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(modelName)
                //.timeout(Duration.ofMinutes(5))
                .temperature(0.0)
                .build();

        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Here we go....");
            Response<AiMessage> response = model.generate(messages);
            LOGGER.info("response = " + response);
            try {
                return objectMapper.readValue(response.content().text(), SuggestedFix.class);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException("Error while parsing the response from AI", ex);
            }
        });
    }

    private String baseUrl() {
        return String.format("http://%s:%d", "localhost", port);
    }
}
