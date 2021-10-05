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

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEvent;
import org.kie.api.event.process.ProcessNodeEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple listener for watching process flow
 */
public class DebugProcessEventListener extends DefaultKogitoProcessEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugProcessEventListener.class);

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        LOGGER.info(formatNodeMessage("afterNodeLeft", event));
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        LOGGER.info(formatNodeMessage("afterNodeTriggered", event));
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        LOGGER.info(formatProcessMessage("afterProcessCompleted", event));
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        LOGGER.info(formatProcessMessage("afterProcessStarted", event));
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        LOGGER.info(formatVariableChangedMessage("afterVariableChanged", event));
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        LOGGER.info(formatNodeMessage("beforeNodeLeft", event));
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        LOGGER.info(formatNodeMessage("beforeNodeTriggered", event));
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        LOGGER.info(formatProcessMessage("beforeProcessCompleted", event));
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        LOGGER.info(formatProcessMessage("beforeProcessStarted", event));
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        LOGGER.info(formatVariableChangedMessage("beforeVariableChanged", event));
    }

    private String formatNodeMessage(String when, ProcessNodeEvent event) {
        NodeInstance ni = event.getNodeInstance();
        return String.format("<%s> name:%s, id:%s", when, ni.getNodeName(), ni.getNodeId());
    }

    private String formatProcessMessage(String when, ProcessEvent event) {
        ProcessInstance pi = event.getProcessInstance();
        return String.format("<%s> name:%s, id:%s, state:%s", when, pi.getProcessName(), pi.getProcessId(),
                pi.getState());
    }

    private String formatVariableChangedMessage(String when, ProcessVariableChangedEvent event) {
        return String.format("<%s> id:%s, old:%s, new:%s", when, event.getVariableId(), event.getOldValue(),
                event.getNewValue());
    }
}