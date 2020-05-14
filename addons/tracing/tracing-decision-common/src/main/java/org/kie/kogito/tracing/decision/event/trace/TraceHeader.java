/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.tracing.decision.event.trace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.kie.kogito.tracing.decision.event.common.Message;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

public class TraceHeader {

    private final TraceEvent.Type type;
    private final String executionId;
    private final long duration;
    private final TraceModel resourceId;
    @JsonInclude(NON_EMPTY)
    private final List<Message> messages;

    public TraceHeader(TraceEvent.Type type, String executionId, long duration, TraceModel resourceId, List<Message> messages) {
        this.type = type;
        this.executionId = executionId;
        this.duration = duration;
        this.resourceId = resourceId;
        this.messages = messages;
    }

    public TraceEvent.Type getType() {
        return type;
    }

    public String getExecutionId() {
        return executionId;
    }

    public long getDuration() {
        return duration;
    }

    public TraceModel getResourceId() {
        return resourceId;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
