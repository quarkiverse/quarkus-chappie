package io.quarkiverse.chappie.runtime;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;

/**
 * Chat to AI sending all info on the exception
 * TODO: Add more details about the project, like extensions used and other libraries.
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@ApplicationScoped
public class AIAssistant {
    private static final Logger log = Logger.getLogger(AIAssistant.class.getName());

    @Inject
    ObjectMapper objectMapper;

    private Prompt systemMessagePrompt;
    private String apiKey;
    private String modelName;

    public void setVersion(String version) {
        this.systemMessagePrompt = systemMessageTemplate.apply(Map.of("version", version));
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setModelName(String modelName) {
        if (modelName == null || modelName.isBlank())
            throw new NullPointerException(
                    "Please supply an OpenAI Model Name as a config property [quarkus.chappie.model-name]");
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

    private static final String USER_MESSAGE = """
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

    private final PromptTemplate userMessageTemplate = PromptTemplate
            .from(USER_MESSAGE);

    public SuggestedFix helpFix(
            String stacktrace,
            String source) {

        if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("apiKey")) {
            Prompt userMessagePrompt = userMessageTemplate.apply(Map.of("stacktrace", stacktrace, "source", source));

            List<ChatMessage> messages = asList(systemMessage(systemMessagePrompt.text()),
                    userMessage(userMessagePrompt.text()));

            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(0.3)
                    .responseFormat("json_object")
                    .build();

            Response<AiMessage> response = model.generate(messages);
            System.out.println("FINISH REASON = " + response.finishReason());
            System.out.println("TOTAL TOKENS = " + response.tokenUsage().totalTokenCount());
            System.out.println("RESPONSE TYPE = " + response.content().type());
            System.out.println("RESPONSE = " + response.content().text());

            try {
                return objectMapper.readValue(response.content().text(), SuggestedFix.class);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException("Error while parsing the response from AI", ex);
            }
        }
        return new SuggestedFix("The quarkus-chappie extension could not assist with this exception",
                "You need to provice a `quarkus.chappie.api-key` config property that is set to your OpenAI api key", null,
                null);
    }

}
