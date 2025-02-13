package io.quarkiverse.chappie.deployment.devservice;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.quarkiverse.chappie.deployment.JsonObjectCreator;
import io.quarkus.deployment.dev.ai.AIClient;
import io.quarkus.deployment.dev.ai.ExceptionOutput;
import io.quarkus.deployment.dev.ai.GenerationOutput;
import io.quarkus.deployment.dev.ai.InterpretationOutput;
import io.quarkus.deployment.dev.ai.ManipulationOutput;

public class ChappieRESTClient implements AIClient {

    private final String baseUrl;

    public ChappieRESTClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public CompletableFuture<String> request(String method, Optional<String> extraContext, Map<String, String> params) {
        try {
            String jsonPayload = JsonObjectCreator.getInput(extraContext, params);
            return send(method, jsonPayload);
        } catch (Exception ex) {
            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    @Override
    public CompletableFuture<ManipulationOutput> manipulate(Optional<String> systemMessage, String userMessage,
            String path, String content) {
        try {
            String jsonPayload = JsonObjectCreator.getInput(systemMessage.orElse(""), userMessage,
                    Map.of("path", path, "content", content));
            return send("manipulate", jsonPayload, ManipulationOutput.class);
        } catch (Exception ex) {
            CompletableFuture<ManipulationOutput> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    @Override
    public CompletableFuture<GenerationOutput> generate(Optional<String> systemMessage, String userMessage,
            String path, String content) {
        try {
            String jsonPayload = JsonObjectCreator.getInput(systemMessage.orElse(""), userMessage,
                    Map.of("path", path, "content", content));
            return send("generate", jsonPayload, GenerationOutput.class);
        } catch (Exception ex) {
            CompletableFuture<GenerationOutput> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    @Override
    public CompletableFuture<InterpretationOutput> interpret(Optional<String> systemMessage, String userMessage,
            String path, String content) {

        try {
            String jsonPayload = JsonObjectCreator.getInput(systemMessage.orElse(""), userMessage,
                    Map.of("path", path, "content", content));
            return send("interpret", jsonPayload, InterpretationOutput.class);
        } catch (Exception ex) {
            CompletableFuture<InterpretationOutput> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }

    }

    @Override
    public CompletableFuture<ExceptionOutput> exception(Optional<String> systemMessage, String userMessage,
            String stacktrace, String path, String content) {

        try {
            String jsonPayload = JsonObjectCreator.getInput(systemMessage.orElse(""), userMessage,
                    Map.of("stacktrace", stacktrace, "path", path, "content", content));
            return send("exception", jsonPayload, ExceptionOutput.class);
        } catch (Exception ex) {
            CompletableFuture<ExceptionOutput> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }

    private CompletableFuture<String> send(String method, String jsonPayload) {
        return send(createHttpRequest(method, jsonPayload), String.class);
    }

    private <T> CompletableFuture<T> send(String method, String jsonPayload, Class<T> responseType) {
        return send(createHttpRequest(method, jsonPayload), responseType);
    }

    private <T> CompletableFuture<T> send(HttpRequest request, Class<T> responseType) {
        HttpClient client = HttpClient.newHttpClient();
        return (CompletableFuture<T>) client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int status = response.statusCode();
                    if (status == 200) {
                        if (responseType.isInstance(String.class)) { // Handle other Java types
                            return response.body();
                        } else {
                            return JsonObjectCreator.getOutput(response.body(), responseType);
                        }
                    } else {
                        throw new RuntimeException("Failed: HTTP error code : " + status);
                    }
                });
    }

    private HttpRequest createHttpRequest(String method, String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/" + method))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
    }
}
