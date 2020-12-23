/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.event;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.runtime.Closeable;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.event.KogitoMessageEvent;
import org.kie.kogito.process.event.KogitoNodeEvent;
import org.kie.kogito.process.event.KogitoProcessEvent;
import org.kie.kogito.process.event.KogitoProcessEventListener;
import org.kie.kogito.process.event.KogitoSignalEvent;
import org.kie.kogito.process.event.KogitoVariableChangedEvent;
import org.kie.kogito.process.event.KogitoWorkItemTransitionEvent;
import org.kie.kogito.process.workitem.Transition;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.kie.kogito.uow.WorkUnit;

public class KogitoProcessEventSupport {

    private UnitOfWorkManager unitOfWorkManager;
    private Collection<KogitoProcessEventListener> listeners = ConcurrentHashMap.newKeySet();

    public KogitoProcessEventSupport(UnitOfWorkManager unitOfWorkManager) {
        this.unitOfWorkManager = unitOfWorkManager;
    }
    
 
    public <E extends KogitoProcessEvent> void notifyAllListeners(E event, BiConsumer<KogitoProcessEventListener, E> consumer) {
        for (KogitoProcessEventListener listener : listeners) {
                consumer.accept( listener, event );
        }
    }

    protected boolean hasListeners() {
        return !listeners.isEmpty();
    }

    /**
     * Adds the specified listener to the list of listeners
     *
     * @param listener to add
     */
    public  void addEventListener(final KogitoProcessEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes all event listeners of the specified class. 
     */
    public  void removeEventListener(Class<?> cls) {
        listeners.removeIf(l -> cls.isAssignableFrom(l.getClass()));
    }

    public final void removeEventListener(final KogitoProcessEventListener listener) {
        this.listeners.remove(listener);
    }

    public Collection<KogitoProcessEventListener> getEventListeners() {
        return Collections.unmodifiableCollection(this.listeners);
    }

    public void clear() {
        for (KogitoProcessEventListener listener : listeners) {
            if (listener instanceof Closeable) {
                ((Closeable) listener).close();
            }
        }
        this.listeners.clear();
    }

  
    public void fireBeforeProcessStarted(final ProcessInstance<?> instance) {
        final KogitoProcessEvent event = new KogitoProcessEvent(instance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.beforeProcessStarted( e1 ))));
        }
    }

    public void fireAfterProcessStarted(final ProcessInstance<?> instance) {
        final KogitoProcessEvent event = new KogitoProcessEvent(instance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.afterProcessStarted( e1 ))));
        }
    }

    public void fireBeforeProcessCompleted(final ProcessInstance<?> instance) {
        final KogitoProcessEvent event = new KogitoProcessEvent(instance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.beforeProcessCompleted( e1 ))));
        }
    }

    public void fireAfterProcessCompleted(final ProcessInstance<?> instance) {
        final KogitoProcessEvent event = new KogitoProcessEvent(instance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.afterProcessCompleted( e1 ))));
        }
    }

    public void fireBeforeNodeTriggered(final ProcessInstance<?> processInstance, final NodeInstance nodeInstance) {
        final KogitoNodeEvent event = new KogitoNodeEvent(processInstance,nodeInstance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.beforeNodeTriggered( e1 ))));
        }
    }

    public void fireAfterNodeTriggered(final ProcessInstance<?> processInstance,final NodeInstance nodeInstance) {
        final KogitoNodeEvent event = new KogitoNodeEvent(processInstance,nodeInstance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.afterNodeTriggered( e1 ))));
        }
    }

    public void fireBeforeNodeLeft(final ProcessInstance<?> processInstance,final NodeInstance nodeInstance) {
        final KogitoNodeEvent event = new KogitoNodeEvent(processInstance,nodeInstance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.beforeNodeLeft( e1 ))));
        }
    }

    public void fireAfterNodeLeft(final ProcessInstance<?> processInstance,final NodeInstance nodeInstance) {
        final KogitoNodeEvent event = new KogitoNodeEvent(processInstance,nodeInstance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.afterNodeLeft( e1 ))));
        }
    }

    public void fireBeforeVariableChanged(final String id, final String instanceId,
                                          final Object oldValue, final Object newValue,
                                          final List<String> tags,
                                          final ProcessInstance<?> processInstance, NodeInstance nodeInstance) {
        final KogitoVariableChangedEvent event = new KogitoVariableChangedEvent(processInstance, nodeInstance, id, instanceId, oldValue, newValue, tags);  
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.beforeVariableChanged( e1 ))));
        }
    }

    public void fireAfterVariableChanged(final String id, final String instanceId,
                                         final Object oldValue, final Object newValue,
                                         final List<String> tags,
                                         final ProcessInstance<?> processInstance, NodeInstance nodeInstance) {
        final KogitoVariableChangedEvent event = new KogitoVariableChangedEvent(processInstance, nodeInstance, id, instanceId, oldValue, newValue, tags);  
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.afterVariableChanged( e1 ))));
        }
    }

    public void fireBeforeSLAViolated(final ProcessInstance<?> processInstance, NodeInstance nodeInstance ) {
        final KogitoNodeEvent event = new KogitoNodeEvent(processInstance,nodeInstance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.beforeSLAViolated( e1 ))));
        }
    }

    public void fireAfterSLAViolated(final ProcessInstance<?> processInstance, NodeInstance nodeInstance) {
        final KogitoNodeEvent event = new KogitoNodeEvent(processInstance,nodeInstance);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.afterSLAViolated( e1 ))));
        }
    }

    public void fireBeforeWorkItemTransition(final ProcessInstance<?> instance, WorkItem workitem, Transition<?> transition) {
        final KogitoWorkItemTransitionEvent event = new KogitoWorkItemTransitionEvent(instance, workitem, transition);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.beforeWorkItemTransition( e1 ))));
        }
    }

    public void fireAfterWorkItemTransition(final ProcessInstance<?> instance, WorkItem workitem, Transition<?> transition) {
        final KogitoWorkItemTransitionEvent event = new KogitoWorkItemTransitionEvent(instance, workitem, transition);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.afterWorkItemTransition( e1 ))));
        }
    }
    
    public void fireOnMessageSent(final ProcessInstance<?> instance, NodeInstance nodeInstance, String messageName, Object messageObject) {
        final KogitoMessageEvent event = new KogitoMessageEvent(instance, nodeInstance, messageName, messageObject);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.onMessageSent( e1 ))));
        }
    }
    
    public void fireOnSignalSent(final ProcessInstance<?> instance, NodeInstance nodeInstance, String signalName, Object signalObject) {
        final KogitoSignalEvent event = new KogitoSignalEvent(instance, nodeInstance, signalName, signalObject);
        if ( hasListeners() ) {
            unitOfWorkManager.currentUnitOfWork().intercept(WorkUnit.create(event, e -> notifyAllListeners( event, ( l, e1 ) -> l.onSignalSent( e1 ))));
        }
    }

    public void reset() {
        this.clear();
    }
}
