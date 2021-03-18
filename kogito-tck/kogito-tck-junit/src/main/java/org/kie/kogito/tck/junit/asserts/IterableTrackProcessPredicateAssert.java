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

import java.util.Iterator;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.kogito.tck.junit.listeners.ProcessTrackedEvent;

public class IterableTrackProcessPredicateAssert {

    private Iterator<ProcessTrackedEvent> events;

    public IterableTrackProcessPredicateAssert(Iterable<ProcessTrackedEvent> eventsForProcess) {
        events = eventsForProcess.iterator();
    }

    public IterableTrackProcessPredicateAssert started() {
        ProcessTrackedEvent event = nextEvent();
        if(!(event.getEvent() instanceof ProcessStartedEvent)) {
            Assertions.fail("Process has not started");
        }
        return this;
    }

    public IterableTrackProcessPredicateAssert step(String nodeName) {
        entered(nodeName);
        exited(nodeName);
        return this;
    }

    public IterableTrackProcessPredicateAssert entered(String nodeName) {
        ProcessTrackedEvent event = nextEvent();
        if(!(event.getEvent() instanceof ProcessNodeTriggeredEvent)) {
            Assertions.fail("Node " + nodeName + " has no entered");
        }
        ProcessNodeTriggeredEvent nodeTriggeredEvent = event.getEvent();
        String nodeNameEntered = nodeTriggeredEvent.getNodeInstance().getNodeName();
        if(!nodeName.equals(nodeNameEntered)) {
            Assertions.fail("expected entering node name " + nodeName + " but it was " + nodeNameEntered);
        }
        return this;
    }

    public IterableTrackProcessPredicateAssert varAssert(String varName, Function<Object,Boolean> assertion) {
        ProcessTrackedEvent event = nextEvent();
        while(event != null) {
            if (!(event.getEvent() instanceof ProcessVariableChangedEvent)) {
                event = nextEvent();
                continue;
            }
            ProcessVariableChangedEvent varChanged = event.getEvent();
            String nodeVarName = varChanged.getVariableId();
            if(!varName.equals(nodeVarName)) {
                Assertions.fail("Expecting variable change in " + varName + " but found " + nodeVarName);
            } else {
                assertion.apply(varChanged.getNewValue());
                break;
            }
        }
        return this;
    }

    public IterableTrackProcessPredicateAssert exited(String nodeName) {
        ProcessTrackedEvent event = nextEvent();
        while(event != null) {
            if(event.getEvent() instanceof ProcessNodeTriggeredEvent) {
                ProcessNodeTriggeredEvent nodeTriggeredEvent = event.getEvent();
                Assertions.fail("Expected leaving node name " + nodeName + " but entering node " + nodeTriggeredEvent.getNodeInstance().getNodeName());
            }
            if (!(event.getEvent() instanceof ProcessNodeLeftEvent)) {
                event = nextEvent();
                continue;
            }
            ProcessNodeLeftEvent nodeLeftEvent = event.getEvent();
            String nodeNameLeft = nodeLeftEvent.getNodeInstance().getNodeName();
            if(!nodeName.equals(nodeNameLeft)) {
                Assertions.fail("expected leaving node name " + nodeName + " but it was " + nodeNameLeft);
            } else {
                break;
            }
        }
        return this;
    }

    public IterableTrackProcessPredicateAssert completed() {
        ProcessTrackedEvent event = nextEvent();
        if(!(event.getEvent() instanceof ProcessCompletedEvent)) {
            Assertions.fail("Process has not ended");
        }
        return this;
    }

    private ProcessTrackedEvent nextEvent() {
        ProcessTrackedEvent event = events.next();
        if(event == null) {
            Assertions.fail("there are no more events to process");
        }
        return event;
    }

    public IterableTrackProcessPredicateAssert varAssert(String varName, Object equal) {
        // TODO Auto-generated method stub
        return null;
    }




}
