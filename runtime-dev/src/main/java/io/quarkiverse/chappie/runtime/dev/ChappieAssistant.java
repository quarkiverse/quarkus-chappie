package io.quarkiverse.chappie.runtime.dev;

import java.lang.invoke.MethodHandle;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import io.quarkus.assistant.runtime.dev.Assistant;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.logging.Log;

public class ChappieAssistant implements Assistant {

    private String baseUrl = null;
    private String memoryId = null;
    private String title = null;

    @Override
    public boolean isAvailable() {
        return this.baseUrl != null;
    }

    @Override
    public <T> CompletionStage<T> assist(Optional<String> systemMessageTemplate,
            String userMessageTemplate,
            Map<String, String> variables,
            List<Path> paths) {//,
        //Class<?> responseType) {
        return assist(systemMessageTemplate, userMessageTemplate, variables, paths, Map.class, true, true);
    }

    public <T> CompletionStage<T> assist(Optional<String> systemMessageTemplate,
            String userMessageTemplate,
            Map<String, String> variables,
            List<Path> paths,
            Class<?> responseType,
            boolean unwrap,
            boolean forceNewSession) {

        Map<String, String> enhancedVariables = new HashMap<>(variables);
        String extension = getExtension();
        if (extension != null && !variables.containsKey("extension")) {
            enhancedVariables.put("extension", extension);
        }

        if (forceNewSession) {
            this.clearMemory();
        }

        try {
            String jsonPayload = JsonObjectCreator.getWorkspaceInput(systemMessageTemplate.orElse(""), userMessageTemplate,
                    enhancedVariables, paths, responseType);

            return (CompletionStage<T>) sendToChappieServer("assist", jsonPayload, responseType, unwrap);
        } catch (Exception ex) {
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    @Deprecated
    @Override
    public <T> CompletionStage<T> exception(Optional<String> systemMessage, String userMessage,
            String stacktrace, Path path) {
        try {
            String jsonPayload = JsonObjectCreator.getInput(systemMessage.orElse(""), userMessage, Map.of(),
                    Map.of("stacktrace", stacktrace, "path", path.toString()), Map.class);

            return (CompletionStage<T>) sendToChappieServer("exception", jsonPayload, ExceptionOutput.class, true);
        } catch (Exception ex) {
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    public CompletionStage<List<Map>> getChats() {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Chappie server is not configured"));
        }
        HttpRequest r = HttpRequest.newBuilder().GET()
                .uri(URI.create(baseUrl + "/api/store/chats"))
                .header("Accept", "application/json")
                .build();

        return getArray(r);
    }

    public CompletionStage<Map> getMostRecentChatMessages() {
        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Chappie server is not configured"));
        }
        if (this.memoryId != null && !this.memoryId.isBlank()) {
            return getChatMessages(this.memoryId);
        } else {
            HttpRequest r = HttpRequest.newBuilder().GET()
                    .uri(URI.create(baseUrl + "/api/store/most-recent"))
                    .header("Accept", "application/json")
                    .build();

            return getObject(r);
        }
    }

    public CompletionStage<Map> getChatMessages(String memoryId) {
        this.memoryId = memoryId;

        if (!isAvailable()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Chappie server is not configured"));
        }
        HttpRequest r = HttpRequest.newBuilder().GET()
                .uri(URI.create(baseUrl + "/api/store/messages/" + memoryId))
                .header("Accept", "application/json")
                .build();

        return getObject(r);
    }

    public void deleteChat(String memoryId) {
        if (isAvailable()) {
            HttpRequest r = HttpRequest.newBuilder().DELETE()
                    .uri(URI.create(baseUrl + "/api/store/messages/" + memoryId))
                    .header("Accept", "application/json")
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            client.sendAsync(r, HttpResponse.BodyHandlers.ofString());
        }
    }

    public CompletionStage<Map> searchDocs(String queryMessage, Integer maxResults, String extension) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("queryMessage", queryMessage);
            if (maxResults != null) {
                params.put("maxResults", maxResults);
            }
            if (extension == null) {
                params.put("extension", extension);
            }
            String jsonPayload = JsonObjectCreator.toJsonString(params);
            Log.info("Search payload: " + jsonPayload);

            HttpRequest searchRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/search"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            return getObject(searchRequest);
        } catch (Exception ex) {
            CompletableFuture<Map> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            Log.info("Search Failed ", ex);
            return failedFuture;
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void clearMemory() {
        this.memoryId = null;
        this.title = null;
    }

    public String getMemoryId() {
        return this.memoryId;
    }

    public String getTitle() {
        return this.title;
    }

    private <T> CompletionStage<T> sendToChappieServer(String method, String jsonPayload, Class<T> responseType,
            boolean unwrap) {
        return sendToChappieServer(createHttpRequest(method, jsonPayload), responseType, unwrap);
    }

    private <T> CompletionStage<T> sendToChappieServer(HttpRequest request, Class<T> responseType, boolean unwrap) {
        HttpClient client = HttpClient.newHttpClient();

        return (CompletableFuture<T>) client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply((response) -> {
                    int status = response.statusCode();
                    if (status == 200) {
                        Optional<String> posibleMemoryId = response.headers().firstValue(HEADER_MEMORY_ID);
                        if (posibleMemoryId.isPresent()) {
                            this.memoryId = posibleMemoryId.get();
                        }

                        String body = response.body();
                        ChappieEnvelope envelope = JsonObjectCreator.getEnvelopeOutput(body, responseType);
                        this.title = envelope.niceName();

                        if (unwrap) {
                            if (responseType.isInstance(String.class)) {
                                return body;
                            } else {
                                return envelope.answer();
                            }
                        } else {
                            return Map.of(this.memoryId, envelope);
                        }
                    } else {
                        // TODO: Can we get more details ?
                        throw new RuntimeException("Failed with HTTP error code : " + status);
                    }
                });
    }

    private CompletionStage<Map> getObject(HttpRequest request) {
        HttpClient client = HttpClient.newHttpClient();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int status = response.statusCode();
                    if (status == 200) {
                        String body = response.body();
                        try {
                            return JsonObjectCreator.getMap(response.body());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse messages JSON", e);
                        }
                    } else if (status == 204) {
                        return Map.of();
                    } else {
                        throw new RuntimeException("Failed: HTTP error code : " + status);
                    }
                });
    }

    private CompletionStage<List<Map>> getArray(HttpRequest request) {
        HttpClient client = HttpClient.newHttpClient();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int status = response.statusCode();
                    switch (status) {
                        case 200:
                            try {
                                return JsonObjectCreator.getList(response.body());
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to parse messages JSON", e);
                            }
                        case 204:
                            return List.of();
                        default:
                            throw new RuntimeException("Failed: HTTP error code : " + status);
                    }
                });
    }

    private HttpRequest createHttpRequest(String method, String jsonPayload) {
        if (isAvailable()) {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/" + method))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            if (this.memoryId != null && !this.memoryId.isBlank()) {
                builder = builder.header(HEADER_MEMORY_ID, this.memoryId);
            }

            return builder.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
        } else {
            throw new IllegalStateException("Chappie server is not configured");
        }
    }

    private String getExtension() {
        Class<?> caller = getCallerClass();
        return getArtifactFromCallerClass(caller);
    }

    private Class<?> getCallerClass() {
        // Get the caller class that will be used get the gav
        StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        stackWalker.walk(frames -> frames.collect(Collectors.toList()));

        Optional<StackWalker.StackFrame> stackFrame = stackWalker.walk(frames -> frames
                .filter(frame -> (!frame.getDeclaringClass().getPackageName().startsWith("io.quarkiverse.chappie")
                        && !frame.getDeclaringClass().getPackageName().startsWith("io.quarkus.assistant")
                        && !frame.getDeclaringClass().equals(MethodHandle.class)))
                .findFirst());

        if (stackFrame.isPresent()) {
            return stackFrame.get().getDeclaringClass();
        } else {
            return null;
        }
    }

    private String getArtifactFromCallerClass(Class<?> caller) {
        return DevConsoleManager.invoke("chappie.getArtifact", Map.of("caller", caller.getName()));
    }

    private static final String HEADER_MEMORY_ID = "X-Chappie-MemoryId";

}
