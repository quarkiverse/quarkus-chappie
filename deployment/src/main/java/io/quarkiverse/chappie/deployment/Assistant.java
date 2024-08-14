package io.quarkiverse.chappie.deployment;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

public interface Assistant {

    final String SYSTEM_MESSAGE = """
                You are an AI assistant helping to debug Java / Kotlin exceptions in a Quarkus {{version}} application.
                You will receive the exception stacktrace and the relevant source that caused the exception.

                Approach this task step-by-step, take your time and do not skip steps.

                Respond with a json file, and only a json file, that is valid, and can ne parsed in Java. Make sure to not include the ``` as the start of the response. The json must contain the following fields from the ingested document:
                    - response (your reply)
                    - explanation (about the diff)
                    - diff (between source and suggested source, to show the changes)
                    - suggestedSource
            """;

    final PromptTemplate systemMessageTemplate = PromptTemplate
            .from(SYSTEM_MESSAGE);

    static final String USER_MESSAGE_WITH_SOURCE = """
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

    static final String USER_MESSAGE_WITHOUT_SOURCE = """
                I have the following java exception in my Quarkus app:
                ```
                {{stacktrace}}
                ```

                Please help me fix it.
            """;

    final PromptTemplate userMessageWithSourceTemplate = PromptTemplate
            .from(USER_MESSAGE_WITH_SOURCE);

    final PromptTemplate userMessageWithoutSourceTemplate = PromptTemplate
            .from(USER_MESSAGE_WITHOUT_SOURCE);

    public CompletableFuture<SuggestedFix> suggestFix(String stacktrace, String source);

    default Prompt getSystemMessagePrompt(String quarkusVersion) {
        return systemMessageTemplate.apply(Map.of("version", quarkusVersion));
    }

    default Prompt getUserMessagePrompt(String stacktrace, String source) {
        Prompt userMessagePrompt;
        if (source != null) {
            return userMessageWithSourceTemplate.apply(Map.of("stacktrace", stacktrace, "source", source));
        } else {
            return userMessageWithoutSourceTemplate.apply(Map.of("stacktrace", stacktrace));
        }
    }

    default List<ChatMessage> getChatMessages(Prompt systemMessagePrompt, String stacktrace, String source) {
        return List.of(systemMessage(systemMessagePrompt.text()),
                userMessage(getUserMessagePrompt(stacktrace, source).text()));
    }

}
