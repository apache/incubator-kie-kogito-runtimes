package org.kie.kogito.internal.rules;

import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.kogito.rules.listeners.DataSourceListener;

public final class RuleRuntimeEventListenerAdapter implements RuleRuntimeEventListener {

    private final DataSourceListener dataSourceListener;

    public RuleRuntimeEventListenerAdapter(DataSourceListener dataSourceListener) {
        this.dataSourceListener = dataSourceListener;
    }

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        dataSourceListener.objectAdded();
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        dataSourceListener.objectUpdated();
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        dataSourceListener.objectRemoved();
    }
}
