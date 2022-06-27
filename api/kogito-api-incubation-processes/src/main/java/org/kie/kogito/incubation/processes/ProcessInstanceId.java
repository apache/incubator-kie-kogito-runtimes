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
package org.kie.kogito.incubation.processes;

import org.kie.kogito.incubation.application.LocalId;
import org.kie.kogito.incubation.application.LocalUriId;

public class ProcessInstanceId extends LocalUriId implements LocalId {

    public static final String PREFIX = "instances";

    private final LocalProcessId processId;
    private final String processInstanceId;

    public ProcessInstanceId(LocalProcessId processId, String processInstanceId) {
        super(processId.asLocalUri().append(PREFIX).append(processInstanceId));
        LocalId localDecisionId = processId.toLocalId();
        if (!localDecisionId.asLocalUri().startsWith(LocalProcessId.PREFIX)) {
            throw new IllegalArgumentException("Not a valid process path"); // fixme use typed exception
        }

        this.processId = processId;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public LocalId toLocalId() {
        return this;
    }

    public LocalProcessId processId() {
        return processId;
    }

    public String processInstanceId() {
        return processInstanceId;
    }

    public TaskIds tasks() {
        return new TaskIds(this);
    }

    public SignalIds signals() {
        return new SignalIds(this);
    }

}
