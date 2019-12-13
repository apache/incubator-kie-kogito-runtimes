package org.kie.kogito.rules.listeners;

public interface DataSourceListener {
    void objectAdded();

    void objectUpdated();

    void objectRemoved();
}
