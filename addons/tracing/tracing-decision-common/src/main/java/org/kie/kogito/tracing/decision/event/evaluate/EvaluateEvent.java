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

import java.util.Map;

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
import org.kie.kogito.decision.DecisionExecutionIdUtils;

public class EvaluateEvent {

    public enum Type {
        BEFORE_EVALUATE_ALL(true),
        AFTER_EVALUATE_ALL(false),
        BEFORE_EVALUATE_BKM(true),
        AFTER_EVALUATE_BKM(false),
        BEFORE_EVALUATE_CONTEXT_ENTRY(true),
        AFTER_EVALUATE_CONTEXT_ENTRY(false),
        BEFORE_EVALUATE_DECISION(true),
        AFTER_EVALUATE_DECISION(false),
        BEFORE_EVALUATE_DECISION_SERVICE(true),
        AFTER_EVALUATE_DECISION_SERVICE(false),
        BEFORE_EVALUATE_DECISION_TABLE(true),
        AFTER_EVALUATE_DECISION_TABLE(false),
        BEFORE_INVOKE_BKM(true),
        AFTER_INVOKE_BKM(false);

        private final boolean before;

        Type(boolean before) {
            this.before = before;
        }

        public boolean isBefore() {
            return before;
        }

        public boolean isAfter() {
            return !before;
        }
    }

    private final Type type;
    private final long nanoTime;
    private final String executionId;
    private final String modelNamespace;
    private final String modelName;
    private final String nodeId;
    private final String nodeName;
    private final Map<String, Object> context;
    private final EvaluateResult result;
    private final EvaluateContextEntryResult contextEntryResult;
    private final EvaluateDecisionTableResult decisionTableResult;

    public EvaluateEvent(Type type, long nanoTime, DMNResult result, String modelNamespace, String modelName) {
        this.type = type;
        this.nanoTime = nanoTime;
        this.executionId = DecisionExecutionIdUtils.get(result.getContext());
        this.modelNamespace = modelNamespace;
        this.modelName = modelName;
        this.nodeId = null;
        this.nodeName = null;
        this.context = result.getContext().clone().getAll();
        this.result = EvaluateResult.from(result);
        this.contextEntryResult = null;
        this.decisionTableResult = null;
    }

    public EvaluateEvent(Type type, long nanoTime, DMNResult result, DMNNode node) {
        this.type = type;
        this.nanoTime = nanoTime;
        this.executionId = DecisionExecutionIdUtils.get(result.getContext());
        this.modelNamespace = node.getModelNamespace();
        this.modelName = node.getModelName();
        this.nodeId = node.getId();
        this.nodeName = node.getName();
        this.context = result.getContext().clone().getAll();
        this.result = EvaluateResult.from(result);
        this.contextEntryResult = null;
        this.decisionTableResult = null;
    }

    public EvaluateEvent(Type type, long nanoTime, DMNResult result, String nodeName, EvaluateContextEntryResult contextEntryResult) {
        this.type = type;
        this.nanoTime = nanoTime;
        this.executionId = DecisionExecutionIdUtils.get(result.getContext());
        this.modelNamespace = null;
        this.modelName = null;
        this.nodeId = null;
        this.nodeName = nodeName;
        this.context = result.getContext().clone().getAll();
        this.result = EvaluateResult.from(result);
        this.contextEntryResult = contextEntryResult;
        this.decisionTableResult = null;
    }

    public EvaluateEvent(Type type, long nanoTime, DMNResult result, String nodeName, EvaluateDecisionTableResult decisionTableResult) {
        this.type = type;
        this.nanoTime = nanoTime;
        this.executionId = DecisionExecutionIdUtils.get(result.getContext());
        this.modelNamespace = null;
        this.modelName = null;
        this.nodeId = null;
        this.nodeName = nodeName;
        this.context = result.getContext().clone().getAll();
        this.result = EvaluateResult.from(result);
        this.contextEntryResult = null;
        this.decisionTableResult = decisionTableResult;
    }

    public Type getType() {
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
        return new EvaluateEvent(Type.BEFORE_EVALUATE_ALL, System.nanoTime(), event.getResult(), event.getModelNamespace(), event.getModelName());
    }

    public static EvaluateEvent from(AfterEvaluateAllEvent event) {
        return new EvaluateEvent(Type.AFTER_EVALUATE_ALL, System.nanoTime(), event.getResult(), event.getModelNamespace(), event.getModelName());
    }

    public static EvaluateEvent from(BeforeEvaluateBKMEvent event) {
        return new EvaluateEvent(Type.BEFORE_EVALUATE_BKM, System.nanoTime(), event.getResult(), event.getBusinessKnowledgeModel());
    }

    public static EvaluateEvent from(AfterEvaluateBKMEvent event) {
        return new EvaluateEvent(Type.AFTER_EVALUATE_BKM, System.nanoTime(), event.getResult(), event.getBusinessKnowledgeModel());
    }

    public static EvaluateEvent from(BeforeEvaluateContextEntryEvent event) {
        return new EvaluateEvent(Type.BEFORE_EVALUATE_CONTEXT_ENTRY, System.nanoTime(), event.getResult(), event.getNodeName(), EvaluateContextEntryResult.from(event));
    }

    public static EvaluateEvent from(AfterEvaluateContextEntryEvent event) {
        return new EvaluateEvent(Type.AFTER_EVALUATE_CONTEXT_ENTRY, System.nanoTime(), event.getResult(), event.getNodeName(), EvaluateContextEntryResult.from(event));
    }

    public static EvaluateEvent from(BeforeEvaluateDecisionEvent event) {
        return new EvaluateEvent(Type.BEFORE_EVALUATE_DECISION, System.nanoTime(), event.getResult(), event.getDecision());
    }

    public static EvaluateEvent from(AfterEvaluateDecisionEvent event) {
        return new EvaluateEvent(Type.AFTER_EVALUATE_DECISION, System.nanoTime(), event.getResult(), event.getDecision());
    }

    public static EvaluateEvent from(BeforeEvaluateDecisionServiceEvent event) {
        return new EvaluateEvent(Type.BEFORE_EVALUATE_DECISION_SERVICE, System.nanoTime(), event.getResult(), event.getDecisionService());
    }

    public static EvaluateEvent from(AfterEvaluateDecisionServiceEvent event) {
        return new EvaluateEvent(Type.AFTER_EVALUATE_DECISION_SERVICE, System.nanoTime(), event.getResult(), event.getDecisionService());
    }

    public static EvaluateEvent from(BeforeEvaluateDecisionTableEvent event) {
        return new EvaluateEvent(Type.BEFORE_EVALUATE_DECISION_TABLE, System.nanoTime(), event.getResult(), event.getNodeName(), EvaluateDecisionTableResult.from(event));
    }

    public static EvaluateEvent from(AfterEvaluateDecisionTableEvent event) {
        return new EvaluateEvent(Type.AFTER_EVALUATE_DECISION_TABLE, System.nanoTime(), event.getResult(), event.getNodeName(), EvaluateDecisionTableResult.from(event));
    }

    public static EvaluateEvent from(BeforeInvokeBKMEvent event) {
        return new EvaluateEvent(Type.BEFORE_INVOKE_BKM, System.nanoTime(), event.getResult(), event.getBusinessKnowledgeModel());
    }

    public static EvaluateEvent from(AfterInvokeBKMEvent event) {
        return new EvaluateEvent(Type.AFTER_INVOKE_BKM, System.nanoTime(), event.getResult(), event.getBusinessKnowledgeModel());
    }
}
