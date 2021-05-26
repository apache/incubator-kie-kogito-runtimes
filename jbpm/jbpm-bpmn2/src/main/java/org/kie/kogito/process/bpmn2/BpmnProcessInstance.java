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

import java.util.Map;

import org.jbpm.process.instance.InternalProcessRuntime;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.Model;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.AbstractProcessInstance;

public class BpmnProcessInstance extends AbstractProcessInstance {

    public BpmnProcessInstance(AbstractProcess process, BpmnVariables variables, InternalProcessRuntime rt) {
        super(process, variables, rt);
    }

    public BpmnProcessInstance(AbstractProcess process, BpmnVariables variables, WorkflowProcessInstance wpi) {
        super(process, variables, wpi);
    }

    public BpmnProcessInstance(AbstractProcess process, BpmnVariables variables, InternalProcessRuntime rt, WorkflowProcessInstance wpi) {
        super(process, variables, rt, wpi);
    }

    public BpmnProcessInstance(AbstractProcess process, BpmnVariables variables, String businessKey, InternalProcessRuntime rt) {
        super(process, variables, businessKey, rt);
    }

    @Override
    public BpmnVariables variables() {
        return (BpmnVariables) super.variables();
    }

    @Override
    protected Map<String, Object> bind(Model variables) {
        if (variables == null) {
            return null;
        }
        return variables.toMap();
    }

    @Override
    protected void unbind(Model variables, Map<String, Object> vmap) {
        if (variables == null || vmap == null) {
            return;
        }
        variables.fromMap(vmap);
    }
}
