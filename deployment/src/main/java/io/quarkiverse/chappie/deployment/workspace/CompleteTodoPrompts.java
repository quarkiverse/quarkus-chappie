package io.quarkiverse.chappie.deployment.workspace;

public interface CompleteTodoPrompts {

    final record CompleteTodoResponse(String path, String content) {
    }

    static final String SYSTEM_MESSAGE = """
            You will receive content that needs to be manipulated. Use the content received as input when considering the response.
            Also consider the path of the content to determine the file type of the provided content.

            Approach this task step-by-step, take your time and do not skip steps.

            Respond with the manipulated content in the content field. This response must be valid. In the content field, include only the manipulated content, no explanation or other text.

            You must not wrap manipulated content in backticks, markdown, or in any other way, but return it as plain text.

            Your job is to complete all TODO comments (eg. //TODO:Here something that needs to be done) in the code as best you can. If needed add comment to explain yourself.
             """;

    static final String USER_MESSAGE = """
            I have the following content in this Quarkus project:
            ```
            {{content}}
            ```

            Please replace any TODO: comments with whatever the comment specifies.
            """;
}
