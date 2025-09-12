package org.kie.kogito.addon.quarkus.messaging.endpoint;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

public class TestEventProcessor {

    @Inject
    Instance<TestEventEmitter> emitters;

    public void processReceive(TestEvent event) {

    }

    public void processSend(TestEvent event) {

    }

}
