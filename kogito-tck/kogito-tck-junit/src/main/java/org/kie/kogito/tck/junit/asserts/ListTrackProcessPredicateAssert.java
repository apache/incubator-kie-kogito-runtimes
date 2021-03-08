/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tck.junit.asserts;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.kogito.tck.junit.listeners.ProcessEvents;
import org.kie.kogito.tck.junit.listeners.ProcessTrackedEvent;

public class ListTrackProcessPredicateAssert {

    private ProcessEvents events;

    public ListTrackProcessPredicateAssert(ProcessEvents eventsForProcess) {
        events = eventsForProcess;
    }

    private Optional<ProcessTrackedEvent> findNodeEvent(Class<?> type) {
        for(ProcessTrackedEvent trackedEvent : events.getEvents()) {
            if(!type.isAssignableFrom(trackedEvent.getEvent().getClass())) {
                continue;
            }
            return Optional.of(trackedEvent);
        }
        return Optional.empty();
    }
    
    private Optional<ProcessTrackedEvent> findNodeEvent(Class<?> type, String nodeName) {
        for(ProcessTrackedEvent trackedEvent : events.getEvents()) {
            if(!type.isAssignableFrom(trackedEvent.getEvent().getClass())) {
                continue;
            }
            ProcessNodeEvent event = trackedEvent.getEvent();
            if(event.getNodeInstance().getNodeName().equals(nodeName)) {
                return Optional.of(trackedEvent);
            }
        }
        return Optional.empty();
    }
    
    
    private Optional<ProcessTrackedEvent> findVarEvent(Class<?> type, String varName) {
        for(ProcessTrackedEvent trackedEvent : events.getEvents()) {
            if(!type.isAssignableFrom(trackedEvent.getEvent().getClass())) {
                continue;
            }
            ProcessVariableChangedEvent event = trackedEvent.getEvent();
            if(event.getVariableId().equals(varName)) {
                return Optional.of(trackedEvent);
            }
        }
        return Optional.empty();
    }

    public ListTrackProcessPredicateAssert started() {
        if(!findNodeEvent(ProcessStartedEvent.class).isPresent()) {
            Assertions.fail("Process has not started");
        }
        return this;
    }

    public ListTrackProcessPredicateAssert step(String nodeName) {
        entered(nodeName);
        exited(nodeName);
        return this;
    }

    public ListTrackProcessPredicateAssert entered(String nodeName) {
        if(!findNodeEvent(ProcessNodeTriggeredEvent.class, nodeName).isPresent()) {
            Assertions.fail("Node " + nodeName + " has no entered");
        }
        return this;
    }

    public ListTrackProcessPredicateAssert varChanged(String varName) {
        Optional<ProcessTrackedEvent> event = findVarEvent(ProcessVariableChangedEvent.class, varName);
        if(!event.isPresent()) {
            Assertions.fail("Expecting variable change in " + varName + " but not found ");
        }
        return this;
    }

    public ListTrackProcessPredicateAssert notStep(String nodeName) {
        notEntered(nodeName);
        notExited(nodeName);
        return this;
    }

    public ListTrackProcessPredicateAssert notEntered(String nodeName) {
        if(findNodeEvent(ProcessNodeTriggeredEvent.class, nodeName).isPresent()) {
            Assertions.fail("Node " + nodeName + " has entered");
        }
        return this;
    }

    public ListTrackProcessPredicateAssert notExited(String nodeName) {
        if(findNodeEvent(ProcessNodeLeftEvent.class, nodeName).isPresent()) {
            Assertions.fail("Node " + nodeName + "has exited");
        }
        return this;
    }


    public ListTrackProcessPredicateAssert varNotChanged(String varName) {
        Optional<ProcessTrackedEvent> event = findVarEvent(ProcessVariableChangedEvent.class, varName);
        if(event.isPresent()) {
            Assertions.fail("Expecting variable not to change in " + varName + " but found ");
        }
        return this;
    }


    public ListTrackProcessPredicateAssert varAssert(String varName, Function<Object,Boolean> assertion) {
        Optional<ProcessTrackedEvent> event = findVarEvent(ProcessVariableChangedEvent.class, varName);
        if(!event.isPresent()) {
            Assertions.fail("Expecting variable change in " + varName + " but not found ");
        }
        
        ProcessVariableChangedEvent varChangedEvent = event.get().getEvent();
        if(assertion.apply(varChangedEvent.getNewValue())) {
            Assertions.fail("Variable assertion failed for " + varName);
        }

        return this;
    }

    public ListTrackProcessPredicateAssert exited(String nodeName) {
        if(!findNodeEvent(ProcessNodeLeftEvent.class, nodeName).isPresent()) {
            Assertions.fail("Node " + nodeName + " has no entered");
        }
        return this;
    }

    public ListTrackProcessPredicateAssert completed() {
        if(!findNodeEvent(ProcessCompletedEvent.class).isPresent()) {
            Assertions.fail("Process has not ended");
        }
        return this;
    }




}
