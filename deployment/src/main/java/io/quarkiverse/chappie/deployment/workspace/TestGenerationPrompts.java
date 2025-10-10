package io.quarkiverse.chappie.deployment.workspace;

public interface TestGenerationPrompts {

    final record TestGenerationResponse(String path, String content) {
    }

    static final String SYSTEM_MESSAGE = """
            You will receive content that needs to used as base for other content generation. Use the content received as input when considering the response.
            Also consider the path of the content to determine the file type of the provided content.

            Approach this task step-by-step, take your time and do not skip steps.

            Respond with the generated content in the content field. This response must be valid content in the content type requested. Only include the generated content in the content field, no explanation or other text.

            You must not wrap generated content response in backticks, markdown, or in any other way, but return it as plain text.

            Your job is to generate a test for the provided Quarkus source code. Make sure the solutions is done in the Quarkus recomended way
             """;

    static final String USER_MESSAGE = """
            I have the following content in this Quarkus project:
            ```
            {{content}}
            ```

            Please generate a Unit test to test all methods in the source code provided.
            Use the same package as the provided source.
            Make the path of the generated test the same as the path of the provided source with Test appended.
            The generated code must be able to compile.
            """;
}