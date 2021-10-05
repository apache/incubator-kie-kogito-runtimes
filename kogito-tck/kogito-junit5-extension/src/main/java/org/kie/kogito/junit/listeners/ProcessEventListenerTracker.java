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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;

/**
 * Simple listener for watching process flow
 */
public class ProcessEventListenerTracker extends DefaultKogitoProcessEventListener {

    private Map<String, Integer> counterStarted;
    private Map<String, Integer> counterCompleted;

    public ProcessEventListenerTracker() {
        counterStarted = new ConcurrentHashMap<>();
        counterCompleted = new ConcurrentHashMap<>();
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        counterStarted.computeIfAbsent(event.getProcessInstance().getProcessId(), (key) -> 1);
        counterStarted.computeIfPresent(event.getProcessInstance().getProcessId(), (key, counter) -> counter + 1);
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        counterCompleted.computeIfAbsent(event.getProcessInstance().getProcessId(), (key) -> 1);
        counterCompleted.computeIfPresent(event.getProcessInstance().getProcessId(), (key, counter) -> counter + 1);
    }

    public int countForProcessIdStarted(String processId) {
        return counterStarted.getOrDefault(processId, 0);
    }

    public int countForProcessIdCompleted(String processId) {
        return counterStarted.getOrDefault(processId, 0);
    }
}