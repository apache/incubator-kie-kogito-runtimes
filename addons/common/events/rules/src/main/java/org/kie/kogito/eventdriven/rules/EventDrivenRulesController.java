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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventEmitter;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.cloudevents.extension.KogitoRulesExtension;
import org.kie.kogito.event.cloudevents.utils.CloudEventUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.ExtensionProvider;

/**
 * This class must always have exact FQCN as <code>org.kie.kogito.eventdriven.rules.EventDrivenRulesController</code>
 * for code generation plugins to correctly detect if this addon is enabled.
 */
public class EventDrivenRulesController {

    private static final String REQUEST_EVENT_TYPE = "RulesRequest";
    private static final String RESPONSE_EVENT_TYPE = "RulesResponse";

    private static final Logger LOG = LoggerFactory.getLogger(EventDrivenRulesController.class);

    private ConfigBean config;
    private EventEmitter eventEmitter;
    private EventReceiver eventReceiver;

    protected EventDrivenRulesController() {
    }

    protected EventDrivenRulesController(ConfigBean config, EventEmitter eventEmitter, EventReceiver eventReceiver) {
        init(config, eventEmitter, eventReceiver);
    }

    protected void init(ConfigBean config, EventEmitter eventEmitter, EventReceiver eventReceiver) {
        this.config = config;
        this.eventEmitter = eventEmitter;
        this.eventReceiver = eventReceiver;
    }

    public <D> void subscribe(EventDrivenQueryExecutor<D> queryExecutor) {
        eventReceiver.subscribe(new RequestHandler<>(queryExecutor), queryExecutor.getObjectClass());
    }

    private class RequestHandler<T> implements Function<DataEvent<T>, CompletionStage<?>> {

        private EventDrivenQueryExecutor<T> queryExecutor;

        public RequestHandler(EventDrivenQueryExecutor<T> queryExecutor) {
            this.queryExecutor = queryExecutor;
        }

        @Override
        public CompletionStage<?> apply(DataEvent<T> event) {
            LOG.debug("Processing event {}", event);
            if (validateRequest(event)) {
                buildResponseCloudEvent(event, queryExecutor.executeQuery(event)).ifPresent(c -> eventEmitter.emit(c, c.getType(), Optional.empty()));
            } else {
                LOG.info("Event {} with extension {} is not recognized by executor {}. Ignored it", event, queryExecutor);
            }
            return CompletableFuture.completedStage(null);
        }

        private boolean validateRequest(DataEvent<T> event) {
            KogitoRulesExtension extension = ExtensionProvider.getInstance().parseExtension(KogitoRulesExtension.class, event);
            return extension != null && Objects.equals(queryExecutor.getRuleUnitId(), extension.getRuleUnitId()) && Objects.equals(queryExecutor.getQueryName(), extension.getRuleUnitQuery())
                    && REQUEST_EVENT_TYPE.equals(event.getType());
        }

        private Optional<CloudEvent> buildResponseCloudEvent(DataEvent<T> event, Object payload) {
            KogitoRulesExtension extension = new KogitoRulesExtension();
            extension.setRuleUnitId(queryExecutor.getRuleUnitId());
            extension.setRuleUnitQuery(queryExecutor.getQueryName());
            return CloudEventUtils.build(UUID.randomUUID().toString(),
                    CloudEventUtils.buildDecisionSource(config.getServiceUrl(), toKebabCase(queryExecutor.getQueryName())),
                    RESPONSE_EVENT_TYPE,
                    event.getSubject(),
                    payload,
                    extension);
        }

        private String toKebabCase(String inputString) {
            return inputString == null ? null : inputString.replaceAll("(.)(\\p{Upper})", "$1-$2").toLowerCase();
        }
    }

}
