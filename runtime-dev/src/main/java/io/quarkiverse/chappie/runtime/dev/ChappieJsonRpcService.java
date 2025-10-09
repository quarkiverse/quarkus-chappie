package io.quarkiverse.chappie.runtime.dev;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.inject.Inject;

/**
 * Dev UI JsonRPC Service
 */
public class ChappieJsonRpcService {

    @Inject
    ChappieServerManager chappieServerManager;

    // For the config screen
    public Properties loadConfigurationFor(String name) {
        return chappieServerManager.loadConfiguration(name);
    }

    public Properties loadConfiguration() {
        return chappieServerManager.loadConfiguration(null);
    }

    public boolean storeConfiguration(Map<String, String> configuration) {
        return chappieServerManager.storeConfiguration(configuration);
    }

    public boolean clearConfiguration() {
        return chappieServerManager.clearConfiguration();
    }

    // For the Chat screen
    public CompletionStage<List<Map>> getChats() {
        ChappieAssistant a = chappieServerManager.getChappieAssistant();
        if (a != null) {
            return a.getChats();
        }
        return CompletableFuture.failedFuture(new RuntimeException("Assistant not available"));
    }

    public CompletionStage<Map> getChatMessages(String memoryId) {
        ChappieAssistant a = chappieServerManager.getChappieAssistant();
        if (a != null) {
            return a.getChatMessages(memoryId);
        }
        return CompletableFuture.failedFuture(new RuntimeException("Assistant not available"));
    }

    public CompletionStage<Map> getMostRecentChatMessages() {
        ChappieAssistant a = chappieServerManager.getChappieAssistant();
        if (a != null) {
            return a.getMostRecentChatMessages();
        }
        return CompletableFuture.failedFuture(new RuntimeException("Assistant not available"));
    }

    public boolean deleteChat(String memoryId) {
        ChappieAssistant a = chappieServerManager.getChappieAssistant();
        if (a != null) {
            a.deleteChat(memoryId);
            return true;
        }
        return false;
    }

    public String getMemoryId() {
        ChappieAssistant a = chappieServerManager.getChappieAssistant();
        if (a != null) {
            return a.getMemoryId();
        }
        return null;
    }

    public String getTitle() {
        ChappieAssistant a = chappieServerManager.getChappieAssistant();
        if (a != null) {
            return a.getTitle();
        }
        return null;
    }

    public boolean clearMemory() {
        ChappieAssistant a = chappieServerManager.getChappieAssistant();
        if (a != null) {
            a.clearMemory();
            return true;
        }
        return false;
    }

    public CompletionStage<Map> chat(String message) {
        ChappieAssistant a = chappieServerManager.getChappieAssistant();
        if (a != null) {
            Map<String, String> vars = new HashMap<>();
            vars.put("message", message);
            vars.put("extension", "any");

            if (chappieServerManager.isMcpEnabled()) {
                return a.assist(
                        Optional.of(ChatPrompts.SYSTEM_MESSAGE + "\n\n" + ChatPrompts.SYSTEM_MESSAGE_MCP),
                        ChatPrompts.USER_MESSAGE,
                        vars,
                        List.of(),
                        ChatPrompts.ChatResponseWithMCP.class,
                        false,
                        false);
            } else {
                return a.assist(
                        Optional.of(ChatPrompts.SYSTEM_MESSAGE),
                        ChatPrompts.USER_MESSAGE,
                        vars,
                        List.of(),
                        ChatPrompts.ChatResponse.class,
                        false,
                        false);
            }
        }
        return CompletableFuture.failedFuture(new RuntimeException("Assistant not available"));
    }

}
