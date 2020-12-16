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

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import io.cloudevents.CloudEvent;
import org.kie.dmn.api.core.DMNContext;
import org.kie.kogito.Application;
import org.kie.kogito.cloudevents.CloudEventUtils;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.dmn.rest.DMNJSONUtils;
import org.kie.kogito.dmn.rest.DMNResult;
import org.kie.kogito.event.CloudEventEmitter;
import org.kie.kogito.event.CloudEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EventDrivenDecisionController {

    private static final Logger LOG = LoggerFactory.getLogger(EventDrivenDecisionController.class);
    private static final String VALID_REQUEST_EVENT_TYPE = DecisionRequestEvent.class.getName();

    private Application application;
    private CloudEventEmitter eventEmitter;
    private CloudEventReceiver eventReceiver;

    protected EventDrivenDecisionController() {
    }

    protected EventDrivenDecisionController(Application application, CloudEventEmitter eventEmitter, CloudEventReceiver eventReceiver) {
        this.application = application;
        this.eventEmitter = eventEmitter;
        this.eventReceiver = eventReceiver;
    }

    protected void setup(Application application, CloudEventEmitter eventEmitter, CloudEventReceiver eventReceiver) {
        this.application = application;
        this.eventEmitter = eventEmitter;
        this.eventReceiver = eventReceiver;
        setup();
    }

    protected void setup() {
        eventReceiver.subscribe(this::handleEvent);
    }

    private void handleEvent(String event) {
        LOG.info("Received event: " + event);
        CloudEventUtils.decode(event)
                .filter(e -> VALID_REQUEST_EVENT_TYPE.equals(e.getType()))
                .ifPresent(this::handleRequest);
    }

    private void handleRequest(CloudEvent event) {
        CloudEventUtils.decodeData(event, DecisionRequestEvent.class)
                .map(this::processRequest)
                .flatMap(this::buildResponseCloudEvent)
                .flatMap(CloudEventUtils::encode)
                .ifPresent(eventEmitter::emit);
    }

    private DecisionResponseEvent processRequest(DecisionRequestEvent event) {
        DecisionRequestType type = getRequestType(event);
        if (type == DecisionRequestType.INVALID) {
            return new DecisionResponseEvent(DecisionResponseStatus.BAD_REQUEST, "Malformed request event");
        }

        return getDecisionModel(event)
                .map(model -> evaluateRequest(event, type, model))
                .map(DecisionResponseEvent::new)
                .orElseGet(() -> new DecisionResponseEvent(DecisionResponseStatus.NOT_FOUND, "Model not found"));
    }

    private DecisionRequestType getRequestType(DecisionRequestEvent event) {
        if (event != null && event.getModelName() != null && event.getModelNamespace() != null && event.getInputContext() != null) {
            return event.getDecisionServiceName() == null
                    ? DecisionRequestType.EVALUATE_ALL
                    : DecisionRequestType.EVALUATE_DECISION_SERVICE;
        }
        return DecisionRequestType.INVALID;
    }

    private Optional<DecisionModel> getDecisionModel(DecisionRequestEvent event) {
        try {
            return Optional.ofNullable(application.decisionModels().getDecisionModel(event.getModelNamespace(), event.getModelName()));
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    private DMNResult evaluateRequest(DecisionRequestEvent event, DecisionRequestType type, DecisionModel model) {
        DMNContext context = DMNJSONUtils.ctx(model, event.getInputContext());

        org.kie.dmn.api.core.DMNResult result = type == DecisionRequestType.EVALUATE_DECISION_SERVICE
                ? model.evaluateDecisionService(context, event.getDecisionServiceName())
                : model.evaluateAll(context);

        return new DMNResult(event.getModelNamespace(), event.getModelName(), result);
    }

    private Optional<CloudEvent> buildResponseCloudEvent(DecisionResponseEvent response) {
        return CloudEventUtils.build(UUID.randomUUID().toString(), URI.create("https://example.com/"), response, DecisionResponseEvent.class);
    }
}
