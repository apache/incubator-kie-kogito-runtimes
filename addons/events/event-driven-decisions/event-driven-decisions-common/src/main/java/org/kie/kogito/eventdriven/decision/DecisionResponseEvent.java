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

package org.kie.kogito.eventdriven.decision;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.kie.kogito.dmn.rest.DMNResult;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

public class DecisionResponseEvent {

    private DecisionResponseStatus status;

    @JsonInclude(NON_EMPTY)
    private String errorMessage;

    @JsonInclude(NON_EMPTY)
    private String executionId;

    @JsonProperty(access = READ_ONLY) // temporary fix of deserialization issues in tests
    @JsonInclude(NON_NULL)
    private DMNResult result;

    private DecisionResponseEvent() {
    }

    public DecisionResponseEvent(String executionId, DMNResult result) {
        this.status = DecisionResponseStatus.OK;
        this.executionId = executionId;
        this.result = result;
    }

    public DecisionResponseEvent(DecisionResponseStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public DecisionResponseStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getExecutionId() {
        return executionId;
    }

    public DMNResult getResult() {
        return result;
    }
}
