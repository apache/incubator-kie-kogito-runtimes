package org.kie.kogito.event.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventBatch;
import org.kie.kogito.event.process.ProcessDataEvent;

public class ProcessEventBatch implements EventBatch {

    private List<DataEvent<?>> events = new ArrayList<>();

    @Override
    public void append(Object rawEvent) {
        if (!ProcessDataEvent.class.isInstance(rawEvent)) {
            throw new IllegalArgumentException("The event is not a ProcessDataEvent");
        }
        events.add((ProcessDataEvent<?>) rawEvent);
    }

    @Override
    public Collection<DataEvent<?>> events() {
        return events;
    }
}
