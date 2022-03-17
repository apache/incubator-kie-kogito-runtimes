package org.kie.kogito.services.event.impl;

import java.util.Collections;
import java.util.Map;
import org.kie.kogito.MappableToModel;
import org.kie.kogito.Model;

class DummyModel implements Model,
                            MappableToModel<DummyModel> {

    private Object dummyEvent;

    @Override
    public DummyModel fromMap(Map<String, Object> params) {
        this.dummyEvent = params.get("dummyEvent");
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.singletonMap("dummyEvent", dummyEvent);
    }

    public DummyModel(Object dummyEvent) {
        this.dummyEvent = dummyEvent;
    }

    @Override
    public void update(Map<String, Object> params) {
        fromMap(params);
    }

    @Override
    public DummyModel toModel() {
        return this;
    }
}
