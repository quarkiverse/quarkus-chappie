package io.quarkiverse.chappie.runtime;

/**
 * Contains the suggested fix from AI
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
public record SuggestedFix(String response, String explanation, String diff, String suggestedSource) {
}
