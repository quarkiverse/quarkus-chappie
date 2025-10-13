package io.quarkiverse.chappie.deployment.workspace;

public interface ExplainPrompts {

    final record JavaDocResponse(String path, String content) {
    }

    static final String SYSTEM_MESSAGE = """
            You will receive content that needs to be interpreted . You will receive the path and the content, consider that when creating a response.
            Also consider the file type of the provided content as it might not be {{programmingLanguage}}, but just part of a {{programmingLanguage}} project.
            Example: You might receive HTML that is part of a Java project. Then interpret it as HTML (not Java)

            Approach this task step-by-step, take your time and do not skip steps.

            Respond with an interpretation in markdown format, but make sure this markdown in encoded such that it can be added to a json file. This response must be valid markdown. Only include the markdown content, no explanation or other text.

            You must not wrap markdown content in backticks, or in any other way, but return it as plain markdown encoded for json. If the interpretation contains code, make sure to use the markdown format to display the code properly.

            The markdown content must be returned per path (the path will be provided in the provided content).
             """;

    static final String USER_MESSAGE = """
            Please explain the provided content. Talk thought what type of content it is and what it does. If possible talk about how it might be used.

            Here are the content:

            ```
            {{content}}
            ```
            """;
}