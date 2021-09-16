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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.ExtensionProvider;
import org.kie.kogito.cloudevents.CloudEventUtils;
import org.kie.kogito.cloudevents.extension.KogitoExtension;
import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.event.EventEmitter;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.SubscriptionInfo;
import org.kie.kogito.services.event.impl.JsonStringToObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class must always have exact FQCN as <code>org.kie.kogito.eventdriven.rules.EventDrivenRulesController</code>
 * for code generation plugins to correctly detect if this addon is enabled.
 */
public class EventDrivenRulesController {

    public static final String REQUEST_EVENT_TYPE = "RulesRequest";
    public static final String RESPONSE_EVENT_TYPE = "RulesResponse";
    public static final String RESPONSE_FULL_EVENT_TYPE = "RulesResponseFull";
    public static final String RESPONSE_ERROR_EVENT_TYPE = "RulesResponseError";

    private static final Logger LOG = LoggerFactory.getLogger(EventDrivenRulesController.class);

    private ConfigBean config;
    private EventEmitter eventEmitter;
    private EventReceiver eventReceiver;

    protected EventDrivenRulesController() {
    }

    protected EventDrivenRulesController(ConfigBean config, EventEmitter eventEmitter, EventReceiver eventReceiver) {
        this.config = config;
        this.eventEmitter = eventEmitter;
        this.eventReceiver = eventReceiver;
    }

    protected void setup(ConfigBean config, EventEmitter eventEmitter, EventReceiver eventReceiver) {
        this.config = config;
        this.eventEmitter = eventEmitter;
        this.eventReceiver = eventReceiver;
        setup();
    }

    protected void setup() {
        eventReceiver.subscribe(this::handleRequest, new SubscriptionInfo<>(new JsonStringToObject(CloudEventUtils.Mapper.mapper()),
                CloudEvent.class));
    }

    private CompletionStage<Void> handleRequest(CloudEvent event) {
        validateRequest(event)
                .flatMap(this::buildEvaluationContext)
                .map(this::processRequest)
                .flatMap(this::buildResponseCloudEvent)
                .flatMap(CloudEventUtils::toDataEvent)
                .ifPresent(e -> eventEmitter.emit(e, (String) e.get("type"), Optional.empty()));
        return CompletableFuture.completedFuture(null);
    }

    private Optional<CloudEvent> validateRequest(CloudEvent event) {
        return Optional.ofNullable(event).filter(e -> REQUEST_EVENT_TYPE.equals(e.getType()));
    }

    private Optional<EvaluationContext> buildEvaluationContext(CloudEvent event) {
        KogitoExtension kogitoExtension = ExtensionProvider.getInstance().parseExtension(KogitoExtension.class, event);
        Map<String, Object> data = CloudEventUtils.decodeMapData(event, String.class, Object.class).orElse(null);

        if (kogitoExtension == null) {
            LOG.warn("Received CloudEvent(id={} source={} type={}) with null Kogito extension", event.getId(), event.getSource(), event.getType());
        }

        if (data == null) {
            LOG.warn("Received CloudEvent(id={} source={} type={}) with null data", event.getId(), event.getSource(), event.getType());
        }

        return Optional.of(new EvaluationContext(event, kogitoExtension, data));
    }

    private EvaluationContext processRequest(EvaluationContext ctx) {
        return ctx;
    }

    private Optional<CloudEvent> buildResponseCloudEvent(EvaluationContext ctx) {
        return Optional.empty();
    }

    private static class EvaluationContext {

        private final CloudEvent requestCloudEvent;

        public EvaluationContext(CloudEvent requestCloudEvent, KogitoExtension requestKogitoExtension, Map<String, Object> requestData) {
            this.requestCloudEvent = requestCloudEvent;
        }

        public CloudEvent getRequestCloudEvent() {
            return requestCloudEvent;
        }
    }

}
