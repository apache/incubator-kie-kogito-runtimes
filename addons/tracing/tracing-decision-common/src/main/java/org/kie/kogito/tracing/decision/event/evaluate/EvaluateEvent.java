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

package org.kie.kogito.tracing.decision.event.evaluate;

import java.util.HashMap;
import java.util.Map;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.ast.DMNNode;
import org.kie.dmn.api.core.event.AfterEvaluateAllEvent;
import org.kie.dmn.api.core.event.AfterEvaluateBKMEvent;
import org.kie.dmn.api.core.event.AfterEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.AfterInvokeBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateAllEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateBKMEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateContextEntryEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionServiceEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateDecisionTableEvent;
import org.kie.dmn.api.core.event.BeforeInvokeBKMEvent;
import org.kie.dmn.feel.runtime.FEELFunction;
import org.kie.kogito.decision.DecisionExecutionIdUtils;

public class EvaluateEvent {

    private EvaluateEventType type;
    private long nanoTime;
    private String executionId;
    private String modelNamespace;
    private String modelName;
    private String nodeId;
    private String nodeName;
    private Map<String, Object> context;
    private EvaluateResult result;
    private EvaluateContextEntryResult contextEntryResult;
    private EvaluateDecisionTableResult decisionTableResult;

    public EvaluateEvent(EvaluateEventType type, long nanoTime, DMNResult result, String modelNamespace, String modelName) {
        this.type = type;
        this.nanoTime = nanoTime;
        this.executionId = DecisionExecutionIdUtils.get(result.getContext());
        this.modelNamespace = modelNamespace;
        this.modelName = modelName;
        this.nodeId = null;
        this.nodeName = null;
        this.context = extractContext(result.getContext());
        this.result = EvaluateResult.from(result);
        this.contextEntryResult = null;
        this.decisionTableResult = null;
    }

    public EvaluateEvent(EvaluateEventType type, long nanoTime, DMNResult result, DMNNode node) {
        this.type = type;
        this.nanoTime = nanoTime;
        this.executionId = DecisionExecutionIdUtils.get(result.getContext());
        this.modelNamespace = node.getModelNamespace();
        this.modelName = node.getModelName();
        this.nodeId = node.getId();
        this.nodeName = node.getName();
        this.context = extractContext(result.getContext());
        this.result = EvaluateResult.from(result);
        this.contextEntryResult = null;
        this.decisionTableResult = null;
    }

    public EvaluateEvent(EvaluateEventType type, long nanoTime, DMNResult result, String nodeName, EvaluateContextEntryResult contextEntryResult) {
        this.type = type;
        this.nanoTime = nanoTime;
        this.executionId = DecisionExecutionIdUtils.get(result.getContext());
        this.modelNamespace = null;
        this.modelName = null;
        this.nodeId = null;
        this.nodeName = nodeName;
        this.context = extractContext(result.getContext());
        this.result = EvaluateResult.from(result);
        this.contextEntryResult = contextEntryResult;
        this.decisionTableResult = null;
    }

    public EvaluateEvent(EvaluateEventType type, long nanoTime, DMNResult result, String nodeName, EvaluateDecisionTableResult decisionTableResult) {
        this.type = type;
        this.nanoTime = nanoTime;
        this.executionId = DecisionExecutionIdUtils.get(result.getContext());
        this.modelNamespace = null;
        this.modelName = null;
        this.nodeId = null;
        this.nodeName = nodeName;
        this.context = extractContext(result.getContext());
        this.result = EvaluateResult.from(result);
        this.contextEntryResult = null;
        this.decisionTableResult = decisionTableResult;
    }

    private EvaluateEvent() {
    }

    public EvaluateEventType getType() {
        return type;
    }

    public long getNanoTime() {
        return nanoTime;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getModelNamespace() {
        return modelNamespace;
    }

    public String getModelName() {
        return modelName;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public EvaluateResult getResult() {
        return result;
    }

    public EvaluateContextEntryResult getContextEntryResult() {
        return contextEntryResult;
    }

    public EvaluateDecisionTableResult getDecisionTableResult() {
        return decisionTableResult;
    }

    public static EvaluateEvent from(BeforeEvaluateAllEvent event) {
        return new EvaluateEvent(EvaluateEventType.BEFORE_EVALUATE_ALL, System.nanoTime(), event.getResult(), event.getModelNamespace(), event.getModelName());
    }

    public static EvaluateEvent from(AfterEvaluateAllEvent event) {
        return new EvaluateEvent(EvaluateEventType.AFTER_EVALUATE_ALL, System.nanoTime(), event.getResult(), event.getModelNamespace(), event.getModelName());
    }

    public static EvaluateEvent from(BeforeEvaluateBKMEvent event) {
        return new EvaluateEvent(EvaluateEventType.BEFORE_EVALUATE_BKM, System.nanoTime(), event.getResult(), event.getBusinessKnowledgeModel());
    }

    public static EvaluateEvent from(AfterEvaluateBKMEvent event) {
        return new EvaluateEvent(EvaluateEventType.AFTER_EVALUATE_BKM, System.nanoTime(), event.getResult(), event.getBusinessKnowledgeModel());
    }

    public static EvaluateEvent from(BeforeEvaluateContextEntryEvent event) {
        return new EvaluateEvent(EvaluateEventType.BEFORE_EVALUATE_CONTEXT_ENTRY, System.nanoTime(), event.getResult(), event.getNodeName(), EvaluateContextEntryResult.from(event));
    }

    public static EvaluateEvent from(AfterEvaluateContextEntryEvent event) {
        return new EvaluateEvent(EvaluateEventType.AFTER_EVALUATE_CONTEXT_ENTRY, System.nanoTime(), event.getResult(), event.getNodeName(), EvaluateContextEntryResult.from(event));
    }

    public static EvaluateEvent from(BeforeEvaluateDecisionEvent event) {
        return new EvaluateEvent(EvaluateEventType.BEFORE_EVALUATE_DECISION, System.nanoTime(), event.getResult(), event.getDecision());
    }

    public static EvaluateEvent from(AfterEvaluateDecisionEvent event) {
        return new EvaluateEvent(EvaluateEventType.AFTER_EVALUATE_DECISION, System.nanoTime(), event.getResult(), event.getDecision());
    }

    public static EvaluateEvent from(BeforeEvaluateDecisionServiceEvent event) {
        return new EvaluateEvent(EvaluateEventType.BEFORE_EVALUATE_DECISION_SERVICE, System.nanoTime(), event.getResult(), event.getDecisionService());
    }

    public static EvaluateEvent from(AfterEvaluateDecisionServiceEvent event) {
        return new EvaluateEvent(EvaluateEventType.AFTER_EVALUATE_DECISION_SERVICE, System.nanoTime(), event.getResult(), event.getDecisionService());
    }

    public static EvaluateEvent from(BeforeEvaluateDecisionTableEvent event) {
        return new EvaluateEvent(EvaluateEventType.BEFORE_EVALUATE_DECISION_TABLE, System.nanoTime(), event.getResult(), event.getNodeName(), EvaluateDecisionTableResult.from(event));
    }

    public static EvaluateEvent from(AfterEvaluateDecisionTableEvent event) {
        return new EvaluateEvent(EvaluateEventType.AFTER_EVALUATE_DECISION_TABLE, System.nanoTime(), event.getResult(), event.getNodeName(), EvaluateDecisionTableResult.from(event));
    }

    public static EvaluateEvent from(BeforeInvokeBKMEvent event) {
        return new EvaluateEvent(EvaluateEventType.BEFORE_INVOKE_BKM, System.nanoTime(), event.getResult(), event.getBusinessKnowledgeModel());
    }

    public static EvaluateEvent from(AfterInvokeBKMEvent event) {
        return new EvaluateEvent(EvaluateEventType.AFTER_INVOKE_BKM, System.nanoTime(), event.getResult(), event.getBusinessKnowledgeModel());
    }

    private static Map<String, Object> extractContext(DMNContext context) {
        return context.getAll().entrySet().stream()
                .filter(e -> !(e.getValue() instanceof FEELFunction))
                // This collect method avoids this bug (https://bugs.openjdk.java.net/browse/JDK-8148463) on variables with null value
                .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }
}
