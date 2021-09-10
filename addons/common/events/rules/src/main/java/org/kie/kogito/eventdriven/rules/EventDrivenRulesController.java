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

package org.kie.kogito.eventdriven.rules;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.cloudevents.CloudEvent;
import org.kie.kogito.cloudevents.CloudEventUtils;

public class EventDrivenRulesController {

    public static final String REQUEST_EVENT_TYPE = "RulesRequest";
    public static final String RESPONSE_EVENT_TYPE = "RulesResponse";
    public static final String RESPONSE_FULL_EVENT_TYPE = "RulesResponseFull";
    public static final String RESPONSE_ERROR_EVENT_TYPE = "RulesResponseError";

    void handleEvent(String event) {
        CloudEventUtils.decode(event)
                .filter(e -> REQUEST_EVENT_TYPE.equals(e.getType()))
                .ifPresent(this::handleRequest);
    }

    private CompletionStage<Void> handleRequest(CloudEvent event) {
//        buildEvaluationContext(event)
//                .map(this::processRequest)
//                .flatMap(this::buildResponseCloudEvent)
//                .flatMap(CloudEventUtils::toDataEvent)
//                .ifPresent(e -> eventEmitter.emit(e, (String) e.get("type"), Optional.empty()));
        return CompletableFuture.completedFuture(null);
    }

}
