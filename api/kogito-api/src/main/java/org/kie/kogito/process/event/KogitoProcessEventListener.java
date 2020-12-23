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

public interface KogitoProcessEventListener {
    /**
     * This listener method is invoked right before a process instance is being started.
     * @param event
     */
    default void beforeProcessStarted(KogitoProcessEvent event) {}

    /**
     * This listener method is invoked right after a process instance has been started.
     * @param event
     */
    default void afterProcessStarted(KogitoProcessEvent event) {}

    /**
     * This listener method is invoked right before a process instance is being completed (or aborted).
     * @param event
     */
    default void beforeProcessCompleted(KogitoProcessEvent event) {}

    /**
     * This listener method is invoked right after a process instance has been completed (or aborted).
     * @param event
     */
    default void afterProcessCompleted(KogitoProcessEvent event) {}

    /**
     * This listener method is invoked right before a node in a process instance is being triggered
     * (which is when the node is being entered, for example when an incoming connection triggers it).
     * @param event
     */
    default void beforeNodeTriggered(KogitoNodeEvent event) {}

    /**
     * This listener method is invoked right after a node in a process instance has been triggered
     * (which is when the node was entered, for example when an incoming connection triggered it).
     * @param event
     */
    default void afterNodeTriggered(KogitoNodeEvent event) {}

    /**
     * This listener method is invoked right before a node in a process instance is being left
     * (which is when the node is completed, for example when it has performed the task it was
     * designed for).
     * @param event
     */
    default void beforeNodeLeft(KogitoNodeEvent event) {}

    /**
     * This listener method is invoked right after a node in a process instance has been left
     * (which is when the node was completed, for example when it performed the task it was
     * designed for).
     * @param event
     */
    default void afterNodeLeft(KogitoNodeEvent event) {}

    /**
     * This listener method is invoked right before the value of a process variable is being changed.
     * @param event
     */
    default void beforeVariableChanged(KogitoVariableChangedEvent event) {}

    /**
     * This listener method is invoked right after the value of a process variable has been changed.
     * @param event
     */
    default void  afterVariableChanged(KogitoVariableChangedEvent event) {}
    
    /**
     * This listener method is invoked right before a process/node instance's SLA has been violated.
     * @param event
     */
    default void beforeSLAViolated(KogitoNodeEvent event){}

    /**
     * This listener method is invoked right after a process/node instance's SLA has been violated.
     * @param event
     */
    default void afterSLAViolated(KogitoNodeEvent event){}
    
    /**
     * This listener method is invoked right before a work item transition.
     * @param event
     */
    default void beforeWorkItemTransition(KogitoWorkItemTransitionEvent event) {}
    
    /**
     * This listener method is invoked right after a work item transition.
     * @param event
     */
    default void afterWorkItemTransition(KogitoWorkItemTransitionEvent event) {}
    
    
    /**
     * This listener method is invoked when a signal is sent
     * @param event
     */
    default void onSignalSent (KogitoSignalEvent signalEvent) {}
    
    /**
     * This listener method is invoked when a message is sent
     * @param event
     */
    default void onMessageSent (KogitoMessageEvent messageEvent) {}
    
}
