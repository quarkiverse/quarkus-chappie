package io.quarkiverse.chappie.runtime.dev;

import io.quarkus.dev.spi.HotReplacementContext;
import io.quarkus.dev.spi.HotReplacementSetup;

public class ChappieHotReplacementSetup implements HotReplacementSetup {

    private volatile ProcessHandle currentProcess;

    @Override
    public void setupHotDeployment(HotReplacementContext context) {
        ChappieServerManager.registerProcessHandler(this::getProcess, this::setProcess);
    }

    private ProcessHandle getProcess() {
        return currentProcess;
    }

    private void setProcess(ProcessHandle currentProcess) {
        this.currentProcess = currentProcess;
    }

    @Override
    public void handleFailedInitialStart() {

    }

    @Override
    public void close() {
        try {
            currentProcess.destroy();
        } catch (Exception e) {
            boolean destroyed = currentProcess.destroyForcibly();
            if (!destroyed) {
                throw new RuntimeException("Failed to destroy chappie process", e);
            }
        }
    }
}
