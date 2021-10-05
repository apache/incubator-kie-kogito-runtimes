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

import org.kie.api.event.process.ProcessEvent;

public class ProcessTrackedEvent {
    private final ProcessEvent event;
    private final String method;

    public ProcessTrackedEvent(ProcessEvent event, String method) {
        this.event = event;
        this.method = method;
    }

    @SuppressWarnings("unchecked")
    public <T extends ProcessEvent> T getEvent() {
        return (T) event;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return method + "(" + event.toString() + ")";
    }
}