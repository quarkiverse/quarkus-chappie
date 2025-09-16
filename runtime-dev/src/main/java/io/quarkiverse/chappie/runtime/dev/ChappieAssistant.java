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

public class ChappieAssistant implements Assistant {

    private String baseUrl = null;
    private String memoryId = null;

    @Override
    public <T> CompletionStage<T> assist(Optional<String> systemMessageTemplate,
            String userMessageTemplate,
            Map<String, String> variables, List<Path> paths) {

        Map<String, String> enhancedVariables = new HashMap<>(variables);
        String extension = getExtension();
        if (extension != null && !variables.containsKey("extension")) {
            enhancedVariables.put("extension", extension);
        }

        try {
            String jsonPayload = JsonObjectCreator.getWorkspaceInput(systemMessageTemplate.orElse(""), userMessageTemplate,
                    enhancedVariables, paths);

            return (CompletionStage<T>) send("assist", jsonPayload, Map.class);
        } catch (Exception ex) {
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    @Override
    public <T> CompletionStage<T> exception(Optional<String> systemMessage, String userMessage,
            String stacktrace, Path path) {
        try {
            String jsonPayload = JsonObjectCreator.getInput(systemMessage.orElse(""), userMessage, Map.of(),
                    Map.of("stacktrace", stacktrace, "path", path.toString()));
            return (CompletionStage<T>) send("exception", jsonPayload, ExceptionOutput.class);
        } catch (Exception ex) {
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    @Override
    public boolean isAvailable() {
        return this.baseUrl != null;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void clearMemoryId() {
        this.memoryId = null;
    }

    private <T> CompletionStage<T> send(String method, String jsonPayload, Class<T> responseType) {
        return send(createHttpRequest(method, jsonPayload), responseType);
    }

    private <T> CompletionStage<T> send(HttpRequest request, Class<T> responseType) {
        HttpClient client = HttpClient.newHttpClient();

        return (CompletableFuture<T>) client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int status = response.statusCode();
                    if (status == 200) {
                        Optional<String> posibleMemoryId = response.headers().firstValue(HEADER_MEMORY_ID);
                        if (posibleMemoryId.isPresent())
                            this.memoryId = posibleMemoryId.get();
                        if (responseType.isInstance(String.class)) { // Handle other Java types
                            return response.body();
                        } else {
                            return JsonObjectCreator.getOutput(response.body(), responseType);
                        }
                    } else {
                        CompletableFuture<T> failedFuture = new CompletableFuture<>();
                        failedFuture.completeExceptionally(new RuntimeException("Failed: HTTP error code : " + status));
                        return failedFuture;
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
