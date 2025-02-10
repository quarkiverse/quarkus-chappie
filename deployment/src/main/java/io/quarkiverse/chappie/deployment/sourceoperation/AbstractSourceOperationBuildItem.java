package io.quarkiverse.chappie.deployment.sourceoperation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.quarkus.builder.item.MultiBuildItem;

public abstract class AbstractSourceOperationBuildItem extends MultiBuildItem {
    private final String label;
    private final String methodName;
    private final Function<Map<String, String>, ?> action;

    public AbstractSourceOperationBuildItem(String label, Function<Map<String, String>, ?> action) {
        this.label = label;
        this.methodName = generateMethodName(label);
        this.action = action;
    }

    public String getLabel() {
        return label;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public Function<Map<String, String>, ?> getAction() {
        return this.action;
    }

    private String generateMethodName(String label) {
        // Remove non-alphanumeric characters and normalize spaces
        String normalized = label.replaceAll("[^a-zA-Z0-9 ]", "").trim().replaceAll(" +", " ");

        // Convert to CamelCase
        String camelCase = toCamelCase(normalized);

        // Ensure it starts with a lowercase letter
        if (!camelCase.isEmpty()) {
            camelCase = Character.toLowerCase(camelCase.charAt(0)) + camelCase.substring(1);
        }

        // Append a short hash for uniqueness
        String hash = generateShortHash(label);
        return camelCase + "_" + hash;
    }

    private String toCamelCase(String input) {
        Matcher matcher = Pattern.compile(" (\\w)").matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(result);

        return result.toString().replaceAll(" ", ""); // Remove remaining spaces
    }

    private String generateShortHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 4; i++) { // Use first 4 bytes for short hash
                hexString.append(String.format("%02x", hashBytes[i]));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
