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

import org.kie.kogito.dmn.rest.DMNResult;

public class DecisionResponseEvent {

    private DecisionResponseStatus status;
    private DMNResult result;
    private String errorMessage;

    private DecisionResponseEvent() {
    }

    public DecisionResponseEvent(DMNResult result) {
        this.status = DecisionResponseStatus.OK;
        this.result = result;
    }

    public DecisionResponseEvent(DecisionResponseStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public DecisionResponseStatus getStatus() {
        return status;
    }

    public DMNResult getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
