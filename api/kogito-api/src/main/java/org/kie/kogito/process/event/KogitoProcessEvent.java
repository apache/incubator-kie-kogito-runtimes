/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.process.event;

import java.time.ZonedDateTime;
import java.util.EventListener;

import org.kie.kogito.process.ProcessInstance;


/**
 * A event related to the execution of kogito process instances.
 */
public class KogitoProcessEvent implements EventListener {
    
    private ProcessInstance<?> processInstance;
    private ZonedDateTime eventDate;
    
    
    public KogitoProcessEvent (ProcessInstance<?> processInstance) {
        this.processInstance = processInstance;
        this.eventDate = ZonedDateTime.now();
    }
    
    /**
     * The ProcessInstance this event relates to.
     *
     * @return the process instance
     */
    public ProcessInstance<?> getProcessInstance() {
        return processInstance;
    }
    
    /**
     * Returns exact date when the event was created
     * @return time when event was created
     */
    public ZonedDateTime getEventDate() {
        return eventDate;
    }

}
