/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.ruleflow.core;

public interface Metadata {

    String ACTION = "Action";
    String TRIGGER_REF = "TriggerRef";
    String MESSAGE_TYPE = "MessageType";
    String TRIGGER_TYPE = "TriggerType";
    String TRIGGER_MAPPING = "TriggerMapping";
    String MAPPING_VARIABLE = "MappingVariable";
    String EVENT_TYPE = "EventType";
    String EVENT_TYPE_TIMER = "Timer";
    String ATTACHED_TO = "AttachedTo";
    String TIME_CYCLE = "TimeCycle";
    String TIME_DURATION = "TimeDuration";
    String CANCEL_ACTIVITY = "CancelActivity";
    String HIDDEN = "hidden";
    String UNIQUE_ID = "UniqueId";
    String LINK_NODE_HIDDEN = "linkNodeHidden";

}
