package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.LocalId;
import org.kie.kogito.incubation.rules.data.DataId;

public interface DataStore<T> {
    DataId add(T value);
    DataId remove(LocalId localId);
}
