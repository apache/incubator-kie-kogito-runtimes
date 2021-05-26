/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.process.bpmn2;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.workflow.core.WorkflowProcess;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.process.impl.AbstractProcess;

public class BpmnProcess extends AbstractProcess {

    private static BpmnProcessCompiler COMPILER = new BpmnProcessCompiler();

    private final Process process;

    public BpmnProcess(Process p) {
        process = p;
    }

    public BpmnProcess(Process p, ProcessConfig config) {
        super(config);
        process = p;
    }

    @Override
    public BpmnProcessInstance createInstance(Model m) {
        BpmnVariables variables = createModel();
        variables.fromMap(m.toMap());
        return new BpmnProcessInstance(this, variables, this.createProcessRuntime());
    }

    public BpmnProcessInstance createInstance() {
        return new BpmnProcessInstance(this, createModel(), this.createProcessRuntime());
    }

    @Override
    public BpmnProcessInstance createInstance(String businessKey, Model variables) {
        BpmnVariables variablesModel = createModel();
        variablesModel.fromMap(variables.toMap());
        return new BpmnProcessInstance(this, variablesModel, businessKey, this.createProcessRuntime());
    }

    @Override
    public BpmnProcessInstance createInstance(WorkflowProcessInstance wpi) {
        return new BpmnProcessInstance(this, createModel(), this.createProcessRuntime(), wpi);
    }

    @Override
    public BpmnProcessInstance createReadOnlyInstance(WorkflowProcessInstance wpi) {
        return new BpmnProcessInstance(this, createModel(), wpi);
    }

    @Override
    public Process process() {
        return process;
    }

    @Override
    public BpmnVariables createModel() {
        VariableScope variableScope = (VariableScope) ((WorkflowProcess) process()).getDefaultContext(VariableScope.VARIABLE_SCOPE);
        return new BpmnVariables(variableScope.getVariables(), new HashMap<>());
    }

    /**
     *
     */
    public static void overrideCompiler(BpmnProcessCompiler compiler) {
        COMPILER = Objects.requireNonNull(compiler);
    }

    public static List<BpmnProcess> from(Resource... resource) {
        return from(null, resource);
    }

    public static List<BpmnProcess> from(ProcessConfig config, Resource... resources) {
        return COMPILER.from(config, resources);
    }

}
