/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.instance.impl.workitem;

import java.util.Arrays;
import java.util.List;

import org.kie.kogito.process.workitem.LifeCyclePhase;

/**
 * Active life cycle phase that applies to any work item.
 * It will set the status to "Ready"
 * 
 * This is initial state so it can transition even if there is no phase set yet.
 */
public class Active implements LifeCyclePhase {

    public static final String ID = "active";
    public static final String STATUS = "Ready";

    private List<String> allowedTransitions = Arrays.asList();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String status() {
        return STATUS;
    }

    @Override
    public boolean isTerminating() {
        return false;
    }

    @Override
    public boolean canTransition(LifeCyclePhase phase) {
        if (phase == null) {
            return true;
        }

        return allowedTransitions.contains(phase.id());
    }

}
