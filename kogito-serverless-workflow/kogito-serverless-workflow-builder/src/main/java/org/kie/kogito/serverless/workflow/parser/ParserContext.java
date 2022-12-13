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
package org.kie.kogito.serverless.workflow.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.drools.codegen.common.GeneratedFile;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.serverless.workflow.asyncapi.AsyncInfoConverter;
import org.kie.kogito.serverless.workflow.asyncapi.AsyncInfoResolver;
import org.kie.kogito.serverless.workflow.asyncapi.CachedAsyncInfoResolver;
import org.kie.kogito.serverless.workflow.operationid.WorkflowOperationIdFactory;
import org.kie.kogito.serverless.workflow.parser.handlers.StateHandler;

import io.serverlessworkflow.api.transitions.Transition;

public class ParserContext {

    private final Map<String, StateHandler<?>> stateHandlers = new LinkedHashMap<>();
    private final RuleFlowProcessFactory factory;
    private final NodeIdGenerator idGenerator;
    private final WorkflowOperationIdFactory operationIdFactory;
    private final KogitoBuildContext context;
    private final Collection<GeneratedFile> generatedFiles;
    private final AsyncInfoResolver asyncInfoResolver;

    public static final String ASYNC_CONVERTER_KEY = "asyncInfoConverter";

    private boolean isCompensation;

    public ParserContext(NodeIdGenerator idGenerator, RuleFlowProcessFactory factory, KogitoBuildContext context, WorkflowOperationIdFactory operationIdFactory) {
        this(idGenerator, factory, context, operationIdFactory, new CachedAsyncInfoResolver(context.getContextAttribute(ASYNC_CONVERTER_KEY, AsyncInfoConverter.class)));
    }

    public ParserContext(NodeIdGenerator idGenerator, RuleFlowProcessFactory factory, KogitoBuildContext context, WorkflowOperationIdFactory operationIdFactory, AsyncInfoResolver asyncInfoResolver) {
        this.idGenerator = idGenerator;
        this.factory = factory;
        this.context = context;
        this.operationIdFactory = operationIdFactory;
        this.asyncInfoResolver = asyncInfoResolver;
        this.generatedFiles = new ArrayList<>();
    }

    public void add(StateHandler<?> stateHandler) {
        stateHandlers.put(stateHandler.getState().getName(), stateHandler);
    }

    public StateHandler<?> getStateHandler(StateHandler<?> stateHandler) {
        return getStateHandler(stateHandler.getState().getTransition());
    }

    public void addGeneratedFile(GeneratedFile file) {
        generatedFiles.add(file);
    }

    public Collection<GeneratedFile> generatedFiles() {
        return generatedFiles;
    }

    public AsyncInfoResolver getAsyncInfoResolver() {
        return asyncInfoResolver;
    }

    public StateHandler<?> getStateHandler(Transition transition) {
        return transition != null ? getStateHandler(transition.getNextState()) : null;
    }

    public StateHandler<?> getStateHandler(String name) {
        return name != null ? stateHandlers.get(name) : null;
    }

    public long newId() {
        return idGenerator.getId();
    }

    public RuleFlowProcessFactory factory() {
        return factory;
    }

    public WorkflowOperationIdFactory operationIdFactory() {
        return operationIdFactory;
    }

    public boolean isCompensation() {
        return isCompensation;
    }

    public void setCompensation() {
        isCompensation = true;
    }

    public KogitoBuildContext getContext() {
        return context;
    }
}
