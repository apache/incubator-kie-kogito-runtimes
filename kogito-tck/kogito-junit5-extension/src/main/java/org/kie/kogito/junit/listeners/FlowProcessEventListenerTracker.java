/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.junit.listeners;

import java.util.HashMap;
import java.util.Map;

import org.drools.core.event.KogitoProcessVariableChangedEventImpl;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;

public class FlowProcessEventListenerTracker extends DefaultKogitoProcessEventListener {

    public static final String BEFORE_STARTED = "beforeProcessStarted";
    public static final String AFTER_STARTED = "afterProcessStarted";
    public static final String BEFORE_COMPLETED = "beforeProcessCompleted";
    public static final String AFTER_COMPLETED = "afterProcessCompleted";
    public static final String BEFORE_TRIGGERED = "beforeNodeTriggered";
    public static final String AFTER_TRIGGERED = "afterNodeTriggered";
    public static final String BEFORE_LEFT = "beforeNodeLeft";
    public static final String AFTER_LEFT = "afterNodeLeft";
    public static final String BEFORE_VARIABLE = "beforeVariableChanged";
    public static final String AFTER_VARIABLE = "afterVariableChanged";

    private final Map<String, ProcessEvents> eventsByProcess;

    public FlowProcessEventListenerTracker() {
        eventsByProcess = new HashMap<>();
    }

    public ProcessEvents eventsForProcess(String id) {
        return eventsByProcess.getOrDefault(id, new ProcessEvents(id));
    }

    private ProcessEvents getEventsForProcess(ProcessEvent event) {
        return eventsByProcess.computeIfAbsent(getStringId(event), (key) -> new ProcessEvents(key));
    }

    private String getStringId(ProcessEvent event) {
        if (event instanceof KogitoProcessVariableChangedEventImpl) {
            return ((WorkflowProcessInstance) ((KogitoProcessVariableChangedEventImpl) event).getSource()).getStringId();
        } else {
            return ((WorkflowProcessInstance) event.getProcessInstance()).getStringId();
        }
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        getEventsForProcess(event).push(new ProcessTrackedEvent(event, BEFORE_STARTED));
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        getEventsForProcess(event).push(new ProcessTrackedEvent(event, BEFORE_COMPLETED));
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        getEventsForProcess(event).push(new ProcessTrackedEvent(event, BEFORE_TRIGGERED));
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        getEventsForProcess(event).push(new ProcessTrackedEvent(event, BEFORE_LEFT));
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        getEventsForProcess(event).push(new ProcessTrackedEvent(event, AFTER_VARIABLE));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        eventsByProcess.values().forEach(events -> builder.append(events).append("\n"));
        return builder.toString();
    }
}
