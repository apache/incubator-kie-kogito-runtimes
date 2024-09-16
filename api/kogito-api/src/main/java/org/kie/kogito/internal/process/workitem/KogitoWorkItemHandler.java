/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.internal.process.workitem;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.kie.kogito.Application;

public interface KogitoWorkItemHandler {

    /**
     * This will allow access other part of the system.
     * 
     * @return
     */
    Application getApplication();

    void setApplication(Application app);

    /**
     * Returns name that it should be registered with, by default simple class name;
     *
     * @return name that should this handler be registered with
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /*
     * Transition to another phase from initial
     */
    Optional<WorkItemTransition> transitionToPhase(KogitoWorkItemManager manager, KogitoWorkItem workItem, WorkItemTransition transition);

    Set<String> allowedTransitions(String phaseStatus);

    WorkItemTransition newTransition(String phaseId, String phaseStatus, Map<String, Object> map, Policy... policy);

    /**
     * The given work item should be activate.
     *
     * @param workItem the work item that should be executed
     * @param manager the manager that requested the work item to be executed
     */
    WorkItemTransition startingTransition(Map<String, Object> data, Policy... policies);

    WorkItemTransition completeTransition(String phaseStatus, Map<String, Object> data, Policy... policies);

    WorkItemTransition abortTransition(String phaseStatus, Policy... policies);

}
