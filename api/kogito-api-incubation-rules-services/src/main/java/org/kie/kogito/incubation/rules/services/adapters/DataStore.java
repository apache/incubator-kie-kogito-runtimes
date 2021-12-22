package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.common.LocalId;

public interface DataStore<T> {
    MutableDataHandle add(T value);
}
