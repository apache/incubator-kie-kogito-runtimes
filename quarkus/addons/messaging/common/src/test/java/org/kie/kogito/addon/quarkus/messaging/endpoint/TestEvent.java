package org.kie.kogito.addon.quarkus.messaging.endpoint;

public interface TestEvent {

    Object payload();

    String channel();
}
