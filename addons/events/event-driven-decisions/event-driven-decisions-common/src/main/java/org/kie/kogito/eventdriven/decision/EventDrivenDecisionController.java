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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.ExtensionProvider;
import org.kie.dmn.api.core.DMNContext;
import org.kie.kogito.cloudevents.CloudEventUtils;
import org.kie.kogito.cloudevents.extension.KogitoExtension;
import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.decision.DecisionExecutionIdUtils;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.dmn.rest.DMNJSONUtils;
import org.kie.kogito.dmn.rest.DMNResult;
import org.kie.kogito.event.CloudEventEmitter;
import org.kie.kogito.event.CloudEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventDrivenDecisionController {

    public static final String REQUEST_EVENT_TYPE = "DecisionRequest";
    public static final String RESPONSE_EVENT_TYPE = "DecisionResponse";
    public static final String RESPONSE_ERROR_EVENT_TYPE = "DecisionResponseError";

    private static final Logger LOG = LoggerFactory.getLogger(EventDrivenDecisionController.class);

    private DecisionModels decisionModels;
    private ConfigBean config;
    private CloudEventEmitter eventEmitter;
    private CloudEventReceiver eventReceiver;

    protected EventDrivenDecisionController() {
    }

    protected EventDrivenDecisionController(DecisionModels decisionModels, ConfigBean config, CloudEventEmitter eventEmitter, CloudEventReceiver eventReceiver) {
        this.decisionModels = decisionModels;
        this.config = config;
        this.eventEmitter = eventEmitter;
        this.eventReceiver = eventReceiver;
    }

    protected void setup(DecisionModels decisionModels, ConfigBean config, CloudEventEmitter eventEmitter, CloudEventReceiver eventReceiver) {
        this.decisionModels = decisionModels;
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
                .filter(e -> REQUEST_EVENT_TYPE.equals(e.getType()))
                .ifPresent(this::handleRequest);
    }

    private void handleRequest(CloudEvent event) {
        buildEvaluationContext(event)
                .map(this::processRequest)
                .flatMap(this::buildResponseCloudEvent)
                .flatMap(CloudEventUtils::encode)
                .ifPresent(eventEmitter::emit);
    }

    private Optional<EvaluationContext> buildEvaluationContext(CloudEvent event) {
        if (event == null) {
            LOG.error("Received null CloudEvent");
            return Optional.empty();
        }

        KogitoExtension kogitoExtension = ExtensionProvider.getInstance().parseExtension(KogitoExtension.class, event);
        Map<String, Object> data = CloudEventUtils.decodeMapData(event, String.class, Object.class).orElse(null);

        if (kogitoExtension == null) {
            LOG.warn("Received CloudEvent(id={} source={} type={}) with null Kogito extension", event.getId(), event.getSource().toString(), event.getType());
        }

        if (data == null) {
            LOG.warn("Received CloudEvent(id={} source={} type={}) with null data", event.getId(), event.getSource().toString(), event.getType());
        }

        return Optional.of(new EvaluationContext(event, kogitoExtension, data));
    }

    private EvaluationContext processRequest(EvaluationContext ctx) {
        if (!ctx.isValidRequest()) {
            ctx.setResponseError(DecisionResponseError.BAD_REQUEST);
            return ctx;
        }

        Optional<DecisionModel> optDecisionModel = getDecisionModel(ctx.getModelNamespace(), ctx.getModelName());
        if (!optDecisionModel.isPresent()) {
            ctx.setResponseError(DecisionResponseError.MODEL_NOT_FOUND);
            return ctx;
        }

        DecisionModel model = optDecisionModel.get();
        DMNContext context = DMNJSONUtils.ctx(model, ctx.getRequestData());

        org.kie.dmn.api.core.DMNResult apiResult = ctx.isEvaluateDecisionServiceRequest()
                ? model.evaluateDecisionService(context, ctx.getDecisionServiceToBeEvaluated())
                : model.evaluateAll(context);

        String executionId = DecisionExecutionIdUtils.get(apiResult.getContext());
        ctx.setExecutionId(executionId);

        DMNResult result = new DMNResult(ctx.getModelNamespace(), ctx.getModelName(), apiResult);
        ctx.setResponseDmnResult(result);

        return ctx;
    }

    private Optional<DecisionModel> getDecisionModel(String modelNamespace, String modelName) {
        try {
            return Optional.ofNullable(decisionModels.getDecisionModel(modelNamespace, modelName));
        } catch (IllegalStateException e) {
            LOG.warn("Model not found with name=\"{}\" namespace=\"{}\"", modelName, modelNamespace);
            return Optional.empty();
        }
    }

    private Optional<CloudEvent> buildResponseCloudEvent(EvaluationContext ctx) {
        String id = UUID.randomUUID().toString();
        URI source = buildResponseCloudEventSource(ctx);
        String subject = ctx.getRequestCloudEvent().getSubject();

        KogitoExtension kogitoExtension = new KogitoExtension();
        kogitoExtension.setDmnModelName(ctx.getModelName());
        kogitoExtension.setDmnModelNamespace(ctx.getModelNamespace());
        kogitoExtension.setDmnEvaluateDecision(ctx.getDecisionServiceToBeEvaluated());

        if (ctx.isResponseError()) {
            String data = Optional.ofNullable(ctx.getResponseError()).map(DecisionResponseError::name).orElse(null);
            return CloudEventUtils.build(id, source, RESPONSE_ERROR_EVENT_TYPE, subject, data, kogitoExtension);
        }

        kogitoExtension.setExecutionId(ctx.getExecutionId());
        return CloudEventUtils.build(id, source, RESPONSE_EVENT_TYPE, subject, ctx.getResponseDmnResult().getDmnContext(), kogitoExtension);
    }

    private URI buildResponseCloudEventSource(EvaluationContext ctx) {
        return ctx.isEvaluateDecisionServiceRequest()
                ? CloudEventUtils.buildDecisionSource(config.getServiceUrl(), ctx.getModelName(), ctx.getDecisionServiceToBeEvaluated())
                : CloudEventUtils.buildDecisionSource(config.getServiceUrl(), ctx.getModelName());
    }

    private static class EvaluationContext {

        private final CloudEvent requestCloudEvent;
        private final Map<String, Object> requestData;

        private final String modelName;
        private final String modelNamespace;
        private final String decisionServiceToBeEvaluated;
        private final boolean validRequest;
        private final boolean evaluateDecisionServiceRequest;

        private DecisionResponseError responseError;
        private DMNResult responseDmnResult;
        private String executionId;

        public EvaluationContext(CloudEvent requestCloudEvent, KogitoExtension requestKogitoExtension, Map<String, Object> requestData) {
            this.requestCloudEvent = requestCloudEvent;
            this.requestData = requestData;

            this.modelName = Optional.ofNullable(requestKogitoExtension)
                    .map(KogitoExtension::getDmnModelName)
                    .orElse(null);
            this.modelNamespace = Optional.ofNullable(requestKogitoExtension)
                    .map(KogitoExtension::getDmnModelNamespace)
                    .orElse(null);
            this.decisionServiceToBeEvaluated = Optional.ofNullable(requestKogitoExtension)
                    .map(KogitoExtension::getDmnEvaluateDecision)
                    .orElse(null);

            this.validRequest = requestCloudEvent != null
                    && requestKogitoExtension != null
                    && modelName != null && !modelName.isEmpty()
                    && modelNamespace != null && !modelNamespace.isEmpty()
                    && requestData != null;

            this.evaluateDecisionServiceRequest = validRequest
                    && decisionServiceToBeEvaluated != null
                    && !decisionServiceToBeEvaluated.isEmpty();
        }

        public boolean isValidRequest() {
            return validRequest;
        }

        public boolean isEvaluateDecisionServiceRequest() {
            return evaluateDecisionServiceRequest;
        }

        boolean isResponseError() {
            return responseDmnResult == null;
        }

        public CloudEvent getRequestCloudEvent() {
            return requestCloudEvent;
        }

        public Map<String, Object> getRequestData() {
            return requestData;
        }

        String getModelName() {
            return modelName;
        }

        String getModelNamespace() {
            return modelNamespace;
        }

        String getDecisionServiceToBeEvaluated() {
            return decisionServiceToBeEvaluated;
        }

        public DecisionResponseError getResponseError() {
            return responseError;
        }

        public void setResponseError(DecisionResponseError responseError) {
            this.responseError = responseError;
        }

        public DMNResult getResponseDmnResult() {
            return responseDmnResult;
        }

        public void setResponseDmnResult(DMNResult responseDmnResult) {
            this.responseDmnResult = responseDmnResult;
        }

        public String getExecutionId() {
            return executionId;
        }

        public void setExecutionId(String executionId) {
            this.executionId = executionId;
        }
    }
}
