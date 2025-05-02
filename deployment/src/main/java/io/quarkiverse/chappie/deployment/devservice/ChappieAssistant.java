package io.quarkiverse.chappie.deployment.devservice;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.quarkiverse.chappie.deployment.JsonObjectCreator;
import io.quarkiverse.chappie.deployment.exception.ExceptionOutput;
import io.quarkus.assistant.deployment.Assistant;

public class ChappieAssistant implements Assistant {

    private final String baseUrl;

    public ChappieAssistant(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public <T> CompletionStage<T> assist(Optional<String> systemMessageTemplate,
            String userMessageTemplate,
            Map<String, String> variables, List<Path> paths) {
        try {
            String jsonPayload = JsonObjectCreator.getWorkspaceInput(systemMessageTemplate.orElse(""), userMessageTemplate,
                    variables, paths);
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

    private <T> CompletionStage<T> send(String method, String jsonPayload, Class<T> responseType) {
        return send(createHttpRequest(method, jsonPayload), responseType);
    }

    private <T> CompletionStage<T> send(HttpRequest request, Class<T> responseType) {
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
                        CompletableFuture<T> failedFuture = new CompletableFuture<>();
                        failedFuture.completeExceptionally(new RuntimeException("Failed: HTTP error code : " + status));
                        return failedFuture;
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
