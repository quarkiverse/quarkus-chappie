package io.quarkiverse.chappie.deployment.exception;

public interface ExceptionPrompts {

    final record ExceptionResponse(String response, String explanation, String diff, String manipulatedContent) {
    }

    static final String SYSTEM_MESSAGE = """
            You will receive an exception that happened in an application. You will receive the stacktrace and the source code where this exception happened.

            Your job is to fix the code and explain why this exception happened.

            The response field should contain your reply to what caused the exception.
            The explanation field should contain details of the exception.
            The diff field should contain the difference between the source and suggested fixed source code, to show the changes and must be in propper diff file format.
            The manipulatedContent field should contain the source code including the fixed code. It must not contain any formatting errors. It must contain the full content as received, only changed to fix the issue.

             """;

    static final String USER_MESSAGE = """
            I have the following exception:

            ```
            {{stacktrace}}
            ```

            That comes from this content:

            ```
            {{content}}
            ```

            Please help me fix it.

            """;
}