package org.kie.kogito.services.event.impl;

import java.util.Optional;
import org.kie.kogito.services.event.ProcessDataEvent;

class DummyCloudEvent extends ProcessDataEvent<DummyEvent> {

    public DummyCloudEvent() {
    }

    public DummyCloudEvent(DummyEvent dummyEvent, String type) {
        this(dummyEvent, type, null);
    }

    public DummyCloudEvent(DummyEvent dummyEvent, String type, String source) {
        this(dummyEvent, type, source, null);
    }

    public DummyCloudEvent(DummyEvent dummyEvent, String type, String source, String referenceId) {
        super(type, Optional.ofNullable(source).orElse(DummyCloudEvent.class.getSimpleName()), dummyEvent, "1", "1", "1", "1", "1", "1", null, referenceId);
    }
}
