package org.kie.kogito.incubation.rules.services.adapters;

import org.kie.kogito.incubation.rules.data.DataId;

public interface DataStream<T> {
    DataId append(T value); // void?
}
