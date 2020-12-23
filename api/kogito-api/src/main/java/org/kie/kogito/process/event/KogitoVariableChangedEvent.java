/*
 * Copyright 2005 Red Hat, Inc. and/or its affiliates.
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

import java.util.Collection;
import java.util.Collections;

import org.kie.api.runtime.process.NodeInstance;
import org.kie.kogito.process.ProcessInstance;

/**
 * An event when a variable inside a process instance has been changed.
 */
public class KogitoVariableChangedEvent extends KogitoNodeEvent {

    private String variableId;
    private String variableInstanceId;
    private Object oldValue;
    private Object newValue;
    private Collection<String> tags;
    
    public KogitoVariableChangedEvent(ProcessInstance<?> processInstance, NodeInstance nodeInstance, String variableId, String variableInstanceId, Object oldValue, Object newValue) {
        this(processInstance, nodeInstance, variableId, variableInstanceId, oldValue, newValue, Collections.emptyList());
    }

    public KogitoVariableChangedEvent(ProcessInstance<?> processInstance, NodeInstance nodeInstance, String variableId, String variableInstanceId, Object oldValue, Object newValue, Collection<String> tags) {
        super(processInstance, nodeInstance);
        this.variableId  = variableId;
        this.variableInstanceId = variableInstanceId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.tags = tags;
    }

    /**
     * The unique id of the process variable (definition).
     *
     * @return the variable id
     */
    public String getVariableId() {
        return variableId;
    }

    /**
     * The unique id of the process variable instance (as multiple node instances with the
     * same process variable definition exists).  This is an aggregation of the unique id of
     * the instance that contains the variable scope and the variable id.
     *
     * @return the variable instance id
     */
    public String getVariableInstanceId() {
        return variableInstanceId;
    }

    /**
     * The old value of the variable.
     * This may be null.
     *
     * @return the old value
     */
    Object getOldValue() {
        return oldValue;
    }

    /**
     * The new value of the variable.
     * This may be null.
     *
     * @return the new value
     */
    Object getNewValue() {
        return newValue;
    }
    
    /**
     * List of tags associated with variable that is being changed.
     * @return list of tags if there are any otherwise empty list
     */
    Collection<String> getTags() {
        return tags;
    }

    /**
     * Determines if variable that is being changed has given tag associated with it
     * @param tag name of the tag
     * @return returns true if given tag is associated with variable otherwise false
     */
    boolean hasTag(String tag) {
        return tags.contains(tag);
    }
}
