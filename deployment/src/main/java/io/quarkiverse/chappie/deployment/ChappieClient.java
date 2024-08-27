package io.quarkiverse.chappie.deployment;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketClient;

public class ChappieClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Integer, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();
    private WebSocket ws;
    private final AtomicInteger count = new AtomicInteger(0);
    private final Vertx vertx = Vertx.vertx();
    private final String jsonRpcBase;

    public ChappieClient(String jsonRpcBase) {
        this.jsonRpcBase = jsonRpcBase;
    }

    public Vertx getVertx() {
        return this.vertx;
    }

    public boolean isConnected() {
        return this.ws != null;
    }

    public void connect() {
        if (this.ws == null) {
            URI jsonRpcUri = URI.create(jsonRpcBase + "/quarkus/json-rpc");
            WebSocketClient webSocketClient = vertx.createWebSocketClient();
            webSocketClient.connect(jsonRpcUri.getPort(), jsonRpcUri.getHost(), jsonRpcUri.getPath())
                    .onComplete(r -> {
                        if (r.succeeded()) {
                            this.ws = r.result();
                            this.ws.textMessageHandler(msg -> {
                                Map jsonResponse = jsonToMap(msg);
                                int id = (Integer) jsonResponse.get("id");
                                CompletableFuture<Object> future = pendingRequests.remove(id);
                                if (future != null) {
                                    Object error = jsonResponse.get("error");
                                    if (error != null) {
                                        future.completeExceptionally(new RuntimeException("Error: " + error));
                                    } else {
                                        Object result = jsonResponse.get("result");
                                        future.complete(result);
                                    }
                                }
                            });
                            this.ws.closeHandler((e) -> {
                                System.out.println(">>>>>>>>>>>>> CLOSING !");
                            });
                        } else {
                            // Just try again.
                            connect();
                        }
                    });
        } else {
            disconnect();
            connect();
        }
    }

    public void disconnect() {
        if (this.ws != null) {
            try {
                this.ws.close().toCompletionStage().toCompletableFuture().get();
                this.ws = null;
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public CompletableFuture<Object> executeRPC(String method, Object[] params) {
        int id = count.incrementAndGet();
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(id, future);

        String requestJson = createJson(id, method, params);

        this.ws.writeTextMessage(requestJson);
        return future;
    }

    private Map jsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String createJson(int id, String method, Object[] params) {
        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("jsonrpc", "2.0");
            rootNode.put("id", id);
            rootNode.put("method", method);
            if (params != null && params.length > 0) {
                ArrayNode paramsNode = rootNode.putArray("params");
                for (Object p : params) {
                    paramsNode.addPOJO(p);
                }
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
