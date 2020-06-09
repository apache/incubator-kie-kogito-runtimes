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

package org.kie.kogito.tracing.decision.aggregator;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.json.Json;
import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.ast.DecisionNode;
import org.kie.dmn.api.core.ast.InputDataNode;
import org.kie.kogito.tracing.decision.event.common.Message;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateDecisionResult;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateEvent;
import org.kie.kogito.tracing.decision.event.trace.TraceEvent;
import org.kie.kogito.tracing.decision.event.trace.TraceExecutionStep;
import org.kie.kogito.tracing.decision.event.trace.TraceHeader;
import org.kie.kogito.tracing.decision.event.trace.TraceInputValue;
import org.kie.kogito.tracing.decision.event.trace.TraceModel;
import org.kie.kogito.tracing.decision.event.trace.TraceOutputValue;
import org.kie.kogito.tracing.decision.event.trace.TraceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAggregator implements Aggregator<TraceEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAggregator.class);

    @Override
    public CloudEventImpl<TraceEvent> aggregate(DMNModel model, String evaluationId, List<EvaluateEvent> events) {
        if (events == null) {
            throw new AggregatorException("Event list is null");
        }
        if (events.size() < 2) {
            throw new AggregatorException("Event list contains less than 2 elements");
        }

        EvaluateEvent firstEvent = events.get(0);
        EvaluateEvent lastEvent = events.get(events.size() - 1);

        // header
        TraceHeader header = new TraceHeader(
                TraceEvent.Type.DMN,
                evaluationId,
                lastEvent.getNanoTime() - firstEvent.getNanoTime(),
                TraceModel.from(model),
                lastEvent.getResult().getMessages().stream()
                        .filter(m -> m.getSourceId() == null || m.getSourceId().isBlank())
                        .collect(Collectors.toList())
        );

        // inputs
        List<TraceInputValue> inputs = model.getInputs().stream()
                .map(i -> traceInputFrom(i, firstEvent.getContext()))
                .collect(Collectors.toList());

        // outputs
        List<TraceOutputValue> outputs = lastEvent.getResult().getDecisionResults().stream()
                .map(dr -> traceOutputFrom(dr, model))
                .collect(Collectors.toList());

        // execution steps
        List<TraceExecutionStep> executionSteps = new ArrayList<>(events.size() / 2);
        Deque<DefaultAggregatorStackEntry> stack = new ArrayDeque<>(events.size() / 2);
        for (int i = 1; i < events.size() - 1; i++) {
            EvaluateEvent event = events.get(i);
            LOG.trace("Started aggregating event {} (execution steps: {}, stack size: {})", event.getType(), executionSteps.size(), stack.size());
            if (event.getType().isBefore()) {
                stack.push(new DefaultAggregatorStackEntry(event));
            } else {
                if (stack.isEmpty() || !stack.peek().isValidAfterEvent(event)) {
                    throw new AggregatorException(String.format("Can't match %s \"after\" event with corresponding \"before\" event", event.getType()));
                }
                DefaultAggregatorStackEntry stackEntry = stack.pop();
                TraceExecutionStep step = buildTraceExecutionStep(model, stackEntry, event);
                if (stack.isEmpty()) {
                    executionSteps.add(step);
                } else {
                    stack.peek().addChild(step);
                }
            }
            LOG.trace("Finished aggregating event {} (execution steps: {}, stack size: {})", event.getType(), executionSteps.size(), stack.size());
        }

        // complete event
        TraceEvent event = new TraceEvent(header, inputs, outputs, executionSteps);

        return CloudEventBuilder.<TraceEvent>builder()
                .withType(TraceEvent.class.getName())
                .withId(evaluationId)
                .withSource(URI.create(URLEncoder.encode(lastEvent.getModelName(), StandardCharsets.UTF_8)))
                .withData(event)
                .build();
    }

    private static TraceExecutionStep buildTraceExecutionStep(DMNModel model, DefaultAggregatorStackEntry stackEntry, EvaluateEvent afterEvent) {
        switch (stackEntry.getType()) {
            case DMN_BKM_EVALUATION:
                return buildDmnBkmEvaluationTraceExecutionStep(model, stackEntry, afterEvent);
            case DMN_BKM_INVOCATION:
                return buildDmnBkmInvocationTraceExecutionStep(model, stackEntry, afterEvent);
            case DMN_CONTEXT_ENTRY:
                return buildDmnContextEntryTraceExecutionStep(model, stackEntry, afterEvent);
            case DMN_DECISION:
                return buildDmnDecisionTraceExecutionStep(model, stackEntry, afterEvent);
            case DMN_DECISION_SERVICE:
                return buildDmnDecisionServiceTraceExecutionStep(model, stackEntry, afterEvent);
            case DMN_DECISION_TABLE:
                return buildDmnDecisionTableTraceExecutionStep(model, stackEntry, afterEvent);
        }
        // this should never happen
        throw new AggregatorException("Can't build trace execution step");
    }

    private static TraceExecutionStep buildDmnBkmEvaluationTraceExecutionStep(DMNModel model, DefaultAggregatorStackEntry stackEntry, EvaluateEvent afterEvent) {
        // TODO: implement complete construction of TraceExecutionStep
        long duration = afterEvent.getNanoTime() - stackEntry.getBeforeEvent().getNanoTime();

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("nodeId", afterEvent.getNodeId());

        return new TraceExecutionStep(stackEntry.getType(), duration, afterEvent.getNodeName(), null, Collections.emptyList(), additionalData, stackEntry.getChildren());
    }

    private static TraceExecutionStep buildDmnBkmInvocationTraceExecutionStep(DMNModel model, DefaultAggregatorStackEntry stackEntry, EvaluateEvent afterEvent) {
        // TODO: implement complete construction of TraceExecutionStep
        long duration = afterEvent.getNanoTime() - stackEntry.getBeforeEvent().getNanoTime();

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("nodeId", afterEvent.getNodeId());

        return new TraceExecutionStep(stackEntry.getType(), duration, afterEvent.getNodeName(), null, Collections.emptyList(), additionalData, stackEntry.getChildren());
    }

    private static TraceExecutionStep buildDmnContextEntryTraceExecutionStep(DMNModel model, DefaultAggregatorStackEntry stackEntry, EvaluateEvent afterEvent) {
        // TODO: implement complete construction of TraceExecutionStep
        long duration = afterEvent.getNanoTime() - stackEntry.getBeforeEvent().getNanoTime();

        Object result = afterEvent.getContextEntryResult().getExpressionResult();

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("expressionId", afterEvent.getContextEntryResult().getExpressionId());
        additionalData.put("nodeId", model.getDecisionByName(afterEvent.getNodeName()).getId());
        additionalData.put("variableId", afterEvent.getContextEntryResult().getVariableId());

        return new TraceExecutionStep(stackEntry.getType(), duration, afterEvent.getContextEntryResult().getVariableName(), result, Collections.emptyList(), additionalData, stackEntry.getChildren());
    }

    private static TraceExecutionStep buildDmnDecisionTraceExecutionStep(DMNModel model, DefaultAggregatorStackEntry stackEntry, EvaluateEvent afterEvent) {
        // TODO: implement complete construction of TraceExecutionStep
        long duration = afterEvent.getNanoTime() - stackEntry.getBeforeEvent().getNanoTime();

        List<Message> messages = afterEvent.getResult().getMessages().stream()
                .filter(m -> afterEvent.getNodeId().equals(m.getSourceId()))
                .collect(Collectors.toList());

        Object result = afterEvent.getResult().getDecisionResults().stream()
                .filter(dr -> dr.getDecisionId().equals(afterEvent.getNodeId()))
                .findFirst()
                .map(EvaluateDecisionResult::getResult)
                .orElse(null);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("nodeId", afterEvent.getNodeId());

        return new TraceExecutionStep(stackEntry.getType(), duration, afterEvent.getNodeName(), result, messages, additionalData, stackEntry.getChildren());
    }

    private static TraceExecutionStep buildDmnDecisionServiceTraceExecutionStep(DMNModel model, DefaultAggregatorStackEntry stackEntry, EvaluateEvent afterEvent) {
        // TODO: implement complete construction of TraceExecutionStep
        long duration = afterEvent.getNanoTime() - stackEntry.getBeforeEvent().getNanoTime();

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("nodeId", afterEvent.getNodeId());

        return new TraceExecutionStep(stackEntry.getType(), duration, afterEvent.getNodeName(), null, Collections.emptyList(), additionalData, stackEntry.getChildren());
    }

    private static TraceExecutionStep buildDmnDecisionTableTraceExecutionStep(DMNModel model, DefaultAggregatorStackEntry stackEntry, EvaluateEvent afterEvent) {
        // TODO: implement complete construction of TraceExecutionStep
        long duration = afterEvent.getNanoTime() - stackEntry.getBeforeEvent().getNanoTime();

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("matches", afterEvent.getDecisionTableResult().getMatches());
        additionalData.put("nodeId", model.getDecisionByName(afterEvent.getNodeName()).getId());
        additionalData.put("selected", afterEvent.getDecisionTableResult().getSelected());

        return new TraceExecutionStep(stackEntry.getType(), duration, afterEvent.getDecisionTableResult().getDecisionTableName(), null, Collections.emptyList(), additionalData, stackEntry.getChildren());
    }

    private static TraceInputValue traceInputFrom(InputDataNode node, Map<String, Object> context) {
        JsonNode value = Optional.ofNullable(context.get(node.getName()))
                .<JsonNode>map(Json.MAPPER::valueToTree)
                .orElse(null);

        return new TraceInputValue(
                node.getId(),
                node.getName(),
                TraceType.from(node.getType()),
                value,
                Collections.emptyList()
        );
    }

    private static TraceOutputValue traceOutputFrom(EvaluateDecisionResult decisionResult, DMNModel model) {
        TraceType type = Optional.ofNullable(model)
                .map(m -> m.getDecisionById(decisionResult.getDecisionId()))
                .map(DecisionNode::getResultType)
                .map(TraceType::from)
                .orElse(null);

        JsonNode value = Optional.ofNullable(decisionResult.getResult())
                .<JsonNode>map(Json.MAPPER::valueToTree)
                .orElse(null);

        return new TraceOutputValue(
                decisionResult.getDecisionId(),
                decisionResult.getDecisionName(),
                decisionResult.getEvaluationStatus().name(),
                type,
                value,
                decisionResult.getMessages()
        );
    }

}
