package io.quarkiverse.chappie.runtime;

import java.util.concurrent.Flow;

import io.quarkus.dev.console.DevConsoleManager;
import io.smallrye.mutiny.Multi;

public class ChappieJsonRPCService {

    public Multi<LastException> streamLastException() {
        Flow.Publisher<LastException> publisher = DevConsoleManager
                .<Multi<LastException>> invoke("chappie-exception-notification");
        return Multi.createFrom().publisher(publisher);
    }

}
