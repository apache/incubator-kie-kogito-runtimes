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

import io.cloudevents.json.Json;
import org.kie.kogito.tracing.decision.aggregator.Aggregator;
import org.kie.kogito.tracing.decision.aggregator.DefaultAggregator;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDecisionTracingCollector {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDecisionTracingCollector.class);

    private final Map<String, List<EvaluateEvent>> cacheMap;
    private final Aggregator<?> aggregator;

    public AbstractDecisionTracingCollector() {
        this(new DefaultAggregator());
    }

    public AbstractDecisionTracingCollector(Aggregator<?> aggregator) {
        this.cacheMap = new HashMap<>();
        this.aggregator = aggregator;
    }

    protected void handleEvaluateEvent(EvaluateEvent event) {
        LOG.trace(
                "Received {}(evaluationId: {}, modelName: {}, modelNamespace: {})",
                event.getClass().getSimpleName(),
                event.getExecutionId(),
                event.getModelName(),
                event.getModelNamespace()
        );

        String evaluationId = event.getExecutionId();
        if (cacheMap.containsKey(evaluationId)) {
            cacheMap.get(evaluationId).add(event);
        } else {
            List<EvaluateEvent> list = new LinkedList<>();
            list.add(event);
            cacheMap.put(evaluationId, list);
            LOG.trace("Added evaluation {} to cache (current size: {})", evaluationId, cacheMap.size());
        }

        if (event instanceof AfterEvaluateAllEvent) {
            String payload = aggregate(evaluationId, cacheMap.get(evaluationId));
            handlePayload(payload);
            LOG.debug("Generated aggregated event for evaluation {} (length {})", evaluationId, payload.length());
            cacheMap.remove(evaluationId);
            LOG.trace("Removed evaluation {} from cache (current size: {})", evaluationId, cacheMap.size());
        }
    }

    protected abstract void handlePayload(String payload);

    private String aggregate(String evaluationId, List<EvaluateEvent> events) {
        return Json.encode(aggregator.aggregate(evaluationId, events));
    }

}
