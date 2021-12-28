package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.rules.data.DataId;

public interface DataHandle {
    void update();

    void remove();

    DataId id();
}
