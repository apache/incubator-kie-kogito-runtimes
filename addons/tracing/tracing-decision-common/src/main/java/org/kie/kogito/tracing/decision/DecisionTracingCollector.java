/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tracing.decision;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import io.cloudevents.json.Json;
import org.kie.dmn.api.core.DMNModel;
import org.kie.kogito.tracing.decision.aggregator.Aggregator;
import org.kie.kogito.tracing.decision.aggregator.AggregatorException;
import org.kie.kogito.tracing.decision.aggregator.DefaultAggregator;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateEvent;
import org.kie.kogito.tracing.decision.modelsupplier.ModelSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionTracingCollector {

    private static final Logger LOG = LoggerFactory.getLogger(DecisionTracingCollector.class);

    private final Map<String, List<EvaluateEvent>> cacheMap;
    private final Map<String, Counter> openEventsCounterMap;
    private final Aggregator<?> aggregator;
    private final Consumer<String> payloadConsumer;
    private final ModelSupplier modelSupplier;

    public DecisionTracingCollector(Consumer<String> payloadConsumer, ModelSupplier modelSupplier) {
        this(new DefaultAggregator(), payloadConsumer, modelSupplier);
    }

    public DecisionTracingCollector(Aggregator<?> aggregator, Consumer<String> payloadConsumer, ModelSupplier modelSupplier) {
        this.cacheMap = new HashMap<>();
        this.openEventsCounterMap = new HashMap<>();
        this.aggregator = aggregator;
        this.payloadConsumer = payloadConsumer;
        this.modelSupplier = modelSupplier;
    }

    public void addEvent(EvaluateEvent event) {
        LOG.trace("Received {}(evaluationId: {}, modelName: {}, modelNamespace: {})", event.getType(), event.getExecutionId(), event.getModelName(), event.getModelNamespace());

        String evaluationId = event.getExecutionId();
        if (cacheMap.containsKey(evaluationId)) {
            cacheMap.get(evaluationId).add(event);
            if (event.getType().isBefore()) {
                openEventsCounterMap.get(evaluationId).increment();
            } else {
                openEventsCounterMap.get(evaluationId).decrement();
            }
        } else {
            List<EvaluateEvent> list = new LinkedList<>();
            list.add(event);
            cacheMap.put(evaluationId, list);
            openEventsCounterMap.put(evaluationId, new Counter(1));
            LOG.trace("Added evaluation {} to cache (current size: {})", evaluationId, cacheMap.size());
        }

        if (openEventsCounterMap.get(evaluationId).getValue() == 0) {
            Optional<DMNModel> optModel = modelSupplier.get(event.getModelNamespace(), event.getModelName());

            if (optModel.isPresent()) {
                try {
                    String payload = aggregate(optModel.get(), evaluationId, cacheMap.get(evaluationId));
                    payloadConsumer.accept(payload);
                    LOG.debug("Generated aggregated event for evaluation {} (length {})", evaluationId, payload.length());
                } catch (AggregatorException e) {
                    LOG.error("Aggregator exception. Evaluation with id " + evaluationId + " will be discarded.", e);
                }
            } else {
                LOG.error("Can't find model (namespace={}, name={}). Evaluation with id {} will be discarded.", event.getModelNamespace(), event.getModelName(), evaluationId);
            }

            cacheMap.remove(evaluationId);
            LOG.trace("Removed evaluation {} from cache (current size: {})", evaluationId, cacheMap.size());
        }
    }

    private String aggregate(DMNModel model, String evaluationId, List<EvaluateEvent> events) {
        return Json.encode(aggregator.aggregate(model, evaluationId, events));
    }

    private static class Counter {

        private int value;

        public Counter(int initialValue) {
            this.value = initialValue;
        }

        public int getValue() {
            return value;
        }

        public void increment() {
            value++;
        }

        public void decrement() {
            value--;
        }
    }
}
