package io.quarkiverse.chappie.deployment.devservice;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import io.quarkiverse.chappie.deployment.ParameterCreator;
import io.quarkus.deployment.dev.ai.AIClient;

public class ChappieRESTClient implements AIClient {

    private final String baseUrl;

    public ChappieRESTClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public CompletableFuture<String> request(String method, Optional<String> extraContext, Map<String, String> params) {
        try {
            String jsonPayload = ParameterCreator.getInput(extraContext, params);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/" + method))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        int status = response.statusCode();
                        if (status == 200) {
                            return response.body();
                        } else {
                            throw new RuntimeException("Failed: HTTP error code : " + status);
                        }
                    });
        } catch (Exception ex) {
            CompletableFuture<String> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }
    }
}
