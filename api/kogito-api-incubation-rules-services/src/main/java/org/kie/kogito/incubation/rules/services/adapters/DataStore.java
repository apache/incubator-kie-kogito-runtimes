package org.kie.kogito.incubation.rules.services.adapters;

public interface DataStore<T> {
    DataHandle add(T value);
}
