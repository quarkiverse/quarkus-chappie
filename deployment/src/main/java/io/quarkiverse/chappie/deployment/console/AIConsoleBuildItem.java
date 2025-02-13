package io.quarkiverse.chappie.deployment.console;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;

public final class AIConsoleBuildItem extends MultiBuildItem {
    private final ConsoleCommand consoleCommand;

    public AIConsoleBuildItem(ConsoleCommand consoleCommand) {
        this.consoleCommand = consoleCommand;
    }

    public ConsoleCommand getConsoleCommand() {
        return consoleCommand;
    }
}
