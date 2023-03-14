/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.workflow.core.impl.WorkflowProcessImpl;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.kie.kogito.Addons;
import org.kie.kogito.KogitoEngine;
import org.kie.kogito.Model;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.StaticConfig;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.impl.StaticProcessConfig;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;
import org.kie.kogito.serverless.workflow.parser.FunctionTypeHandlerFactory;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;
import org.kie.kogito.serverless.workflow.parser.types.RestTypeHandler;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.functions.FunctionDefinition.Type;
import io.vertx.mutiny.core.Vertx;

public class StaticWorkflowApplication extends StaticApplication implements AutoCloseable {

    private final StaticWorkflowProcesses processes = new StaticWorkflowProcesses();
    private Collection<KogitoWorkItemHandler> handlers = new ArrayList<>();
    private StaticRestWorkItemHandler restWorkItemHandler;
    private Vertx vertx;

    public static StaticWorkflowApplication create() {
        return new StaticWorkflowApplication();
    }

    private StaticWorkflowApplication() {
        super(new StaticConfig(new Addons(Collections.emptySet()), new StaticProcessConfig()));

    }

    public JsonNodeModel execute(Workflow workflow, Map<String, Object> data) {
        return execute(process(workflow), data);
    }

    public JsonNodeModel execute(Process<JsonNodeModel> process, Map<String, Object> data) {
        ProcessInstance<JsonNodeModel> processInstance = process.createInstance(new JsonNodeModel(data));
        processInstance.start();
        return processInstance.variables();
    }

    public Process<JsonNodeModel> process(Workflow workflow) {
        return processes.map.computeIfAbsent(workflow.getId(), k -> createProcess(workflow));
    }

    private Process<JsonNodeModel> createProcess(Workflow workflow) {
        registerNeededHandlers(workflow);
        StaticWorkflowProcess process = new StaticWorkflowProcess(this, handlers, ServerlessWorkflowParser
                .of(workflow, JavaKogitoBuildContext.builder().withApplicationProperties(System.getProperties()).build()).getProcessInfo().info());
        WorkflowProcessImpl workflowProcess = (WorkflowProcessImpl) process.get();
        workflowProcess.getNodesRecursively().forEach(node -> {
            if (node instanceof SubProcessNode) {
                SubProcessNode subProcess = (SubProcessNode) node;
                subProcess.setSubProcessFactory(new StaticSubprocessFactory(processes.map.get(subProcess.getProcessId())));
            }
        });
        return process;
    }

    @Override
    public <T extends KogitoEngine> T get(Class<T> clazz) {
        if (Processes.class.isAssignableFrom(clazz)) {
            return clazz.cast(processes);
        }
        return super.get(clazz);
    }

    private class StaticWorkflowProcesses implements Processes {
        private Map<String, Process<JsonNodeModel>> map = new ConcurrentHashMap<>();

        @Override
        public Process<? extends Model> processById(String processId) {
            return map.get(processId);
        }

        @Override
        public Collection<String> processIds() {
            return map.keySet();
        }
    }

    @Override
    public void close() {
        if (restWorkItemHandler != null) {
            restWorkItemHandler.close();
        }
        if (vertx != null) {
            vertx.closeAndAwait();
        }
    }

    private void registerNeededHandlers(Workflow flow) {
        if (flow.getFunctions() != null && flow.getFunctions().getFunctionDefs() != null) {
            for (FunctionDefinition functionDef : flow.getFunctions().getFunctionDefs()) {
                if (functionDef.getType() == Type.CUSTOM) {
                    String type = FunctionTypeHandlerFactory.getTypeFromOperation(functionDef);
                    if (RestTypeHandler.TYPE.equals(type)) {
                        initRestWorkItemHandler();
                    }
                }
            }
        }
    }

    private Vertx initVertx() {
        if (vertx == null) {
            vertx = Vertx.vertx();
        }
        return vertx;
    }

    private void initRestWorkItemHandler() {
        if (restWorkItemHandler == null) {
            restWorkItemHandler = new StaticRestWorkItemHandler(initVertx());
            handlers.add(restWorkItemHandler);
        }
    }
}
