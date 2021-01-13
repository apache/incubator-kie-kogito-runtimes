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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.kogito.Application;
import org.kie.kogito.cloudevents.CloudEventUtils;
import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.decision.DecisionExecutionIdUtils;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.dmn.rest.DMNJSONUtils;
import org.kie.kogito.event.CloudEventEmitter;
import org.kie.kogito.event.CloudEventReceiver;

import static org.kie.kogito.eventdriven.decision.DecisionRequestType.EVALUATE_ALL;
import static org.kie.kogito.eventdriven.decision.DecisionRequestType.EVALUATE_DECISION_SERVICE;

public class EventDrivenDecisionController {

    private static final String VALID_REQUEST_EVENT_TYPE = DecisionRequestEvent.class.getName();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DecisionModels decisionModels;
    private ConfigBean config;
    private CloudEventEmitter eventEmitter;
    private CloudEventReceiver eventReceiver;

    protected EventDrivenDecisionController() {
    }

    protected EventDrivenDecisionController(Application application, ConfigBean config, CloudEventEmitter eventEmitter, CloudEventReceiver eventReceiver) {
        this.decisionModels = application.get(DecisionModels.class);
        this.config = config;
        this.eventEmitter = eventEmitter;
        this.eventReceiver = eventReceiver;
    }

    protected void setup(Application application, ConfigBean config, CloudEventEmitter eventEmitter, CloudEventReceiver eventReceiver) {
        this.decisionModels = application.get(DecisionModels.class);
        this.config = config;
        this.eventEmitter = eventEmitter;
        this.eventReceiver = eventReceiver;
        setup();
    }

    protected void setup() {
        eventReceiver.subscribe(this::handleEvent);
    }

    void handleEvent(String event) {
        CloudEventUtils.decode(event)
                .filter(e -> VALID_REQUEST_EVENT_TYPE.equals(e.getType()))
                .ifPresent(this::handleRequest);
    }

    private void handleRequest(CloudEvent event) {
        CloudEventUtils.decodeData(event, DecisionRequestEvent.class)
                .map(data -> new EvaluationContext(event, data))
                .map(this::processRequest)
                .flatMap(this::buildResponseCloudEvent)
                .flatMap(CloudEventUtils::encode)
                .ifPresent(eventEmitter::emit);
    }

    private EvaluationContext processRequest(EvaluationContext ctx) {
        DecisionRequestEvent request = ctx.request;

        DecisionRequestType type = getRequestType(request);
        ctx.requestType = type;

        ctx.response = type == DecisionRequestType.INVALID
                ? new DecisionResponseEvent(DecisionResponseStatus.BAD_REQUEST, "Malformed request event")
                : getDecisionModel(request)
                        .map(model -> evaluateRequest(request, type, model))
                        .map(result -> buildDecisionResponseEventFromResult(request, result))
                        .orElseGet(() -> new DecisionResponseEvent(DecisionResponseStatus.NOT_FOUND, "Model not found"));

        return ctx;
    }

    private DecisionRequestType getRequestType(DecisionRequestEvent event) {
        if (event != null && event.isValid()) {
            return event.getDecisionServiceName() == null
                    ? EVALUATE_ALL
                    : EVALUATE_DECISION_SERVICE;
        }
        return DecisionRequestType.INVALID;
    }

    private Optional<DecisionModel> getDecisionModel(DecisionRequestEvent event) {
        try {
            return Optional.ofNullable(decisionModels.getDecisionModel(event.getModelNamespace(), event.getModelName()));
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    private DMNResult evaluateRequest(DecisionRequestEvent event, DecisionRequestType type, DecisionModel model) {
        DMNContext context = DMNJSONUtils.ctx(model, event.getInputContext());
        return type == EVALUATE_DECISION_SERVICE
                ? model.evaluateDecisionService(context, event.getDecisionServiceName())
                : model.evaluateAll(context);
    }

    private DecisionResponseEvent buildDecisionResponseEventFromResult(DecisionRequestEvent event, DMNResult result) {
        String executionId = DecisionExecutionIdUtils.get(result.getContext());
        return new DecisionResponseEvent(
                executionId,
                new org.kie.kogito.dmn.rest.DMNResult(event.getModelNamespace(), event.getModelName(), result)
        );
    }

    private Optional<CloudEvent> buildResponseCloudEvent(EvaluationContext ctx) {
        URI source = buildResponseCloudEventSource(ctx);
        String subject = buildResponseCloudEventSubject(ctx);
        return CloudEventUtils.build(UUID.randomUUID().toString(), source, subject, ctx.response, DecisionResponseEvent.class);
    }

    private URI buildResponseCloudEventSource(EvaluationContext ctx) {
        if (ctx.requestType == EVALUATE_ALL) {
            return CloudEventUtils.buildDecisionSource(config.getServiceUrl(), ctx.request.getModelName());
        }
        if (ctx.requestType == EVALUATE_DECISION_SERVICE) {
            return CloudEventUtils.buildDecisionSource(config.getServiceUrl(), ctx.request.getModelName(), ctx.request.getDecisionServiceName());
        }
        return CloudEventUtils.buildDecisionSource(config.getServiceUrl());
    }

    private String buildResponseCloudEventSubject(EvaluationContext ctx) {
        try {
            return MAPPER.writeValueAsString(
                    new DecisionResponseCloudEventSubject(
                            ctx.requestCloudEvent.getId(),
                            ctx.requestCloudEvent.getSource().toString()
                    )
            );
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static class EvaluationContext {

        final CloudEvent requestCloudEvent;
        final DecisionRequestEvent request;
        DecisionRequestType requestType;
        DecisionResponseEvent response;

        public EvaluationContext(CloudEvent requestCloudEvent, DecisionRequestEvent request) {
            this.requestCloudEvent = requestCloudEvent;
            this.request = request;
        }
    }
}
