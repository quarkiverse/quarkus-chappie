package io.quarkiverse.chappie.deployment.workspace;

public interface JavaDocPrompts {

    final record JavaDocResponse(String path, String content) {
    }

    static final String SYSTEM_MESSAGE = """
            You will receive content that needs to be manipulated. Use the content received as input when considering the response.
            Also consider the path of the content to determine the file type of the provided content.

            Approach this task step-by-step, take your time and do not skip steps.

            Respond with the manipulated content in the content field. This response must be valid. In the content field, include only the manipulated content, no explanation or other text.

            You must not wrap manipulated content in backticks, markdown, or in any other way, but return it as plain text.

            Your job is to add JavaDoc on Class and Method level for the provided code.
             """;

    static final String USER_MESSAGE = """
            I have the following content in this Quarkus project:
            ```
            {{content}}
            ```

            Please add or modify the JavaDoc to reflect the code. If JavaDoc exist, take that into account when modifying the content.

            """;
}