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
package org.jbpm.process.instance.impl.actions;

import java.io.Serializable;

import org.jbpm.process.core.event.EventTransformerImpl;
import org.jbpm.process.instance.impl.Action;
import org.jbpm.process.instance.impl.util.VariableUtil;
import org.jbpm.workflow.core.node.Transformation;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.kogito.process.workitems.KogitoWorkItemManager;
import org.kie.kogito.process.workitems.impl.KogitoWorkItemImpl;

public class HandleMessageAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    private final String messageType;
    private String variableName;

    private Transformation transformation;

    public HandleMessageAction(String messageType, String variableName) {
        this.messageType = messageType;
        this.variableName = variableName;
    }

    public HandleMessageAction(String messageType, String variableName, Transformation transformation) {
        this.messageType = messageType;
        this.variableName = variableName;
        this.transformation = transformation;
    }

    public void execute(ProcessContext context) throws Exception {
        Object variable = VariableUtil.resolveVariable(variableName, context.getNodeInstance());

        if (transformation != null) {
            variable = new EventTransformerImpl(transformation).transformEvent(variable);
        }

        KogitoWorkItemImpl workItem = new KogitoWorkItemImpl();
        workItem.setName("Send Task");
        workItem.setNodeInstanceId(context.getNodeInstance().getId());
        workItem.setProcessInstanceId(context.getProcessInstance().getId());
        workItem.setNodeId(context.getNodeInstance().getNodeId());
        workItem.setParameter("MessageType", messageType);
        if (variable != null) {
            workItem.setParameter("Message", variable);
        }

        (( KogitoWorkItemManager ) context.getKieRuntime().getWorkItemManager()).internalExecuteWorkItem(workItem);
    }

}
