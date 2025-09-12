/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jbpm.process.instance.impl.actions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jbpm.process.instance.impl.Action;
import org.jbpm.process.instance.impl.util.VariableUtil;
import org.kie.api.runtime.KieRuntime;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.process.workitems.InternalKogitoWorkItemManager;
import org.kie.kogito.process.workitems.impl.KogitoWorkItemImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.ruleflow.core.Metadata.DEFAULT_SCOPE;
import static org.jbpm.ruleflow.core.Metadata.EXTERNAL_SCOPE;
import static org.jbpm.ruleflow.core.Metadata.PROCESS_INSTANCE_SCOPE;

public abstract class AbstractEventProcessInstanceAction implements Action {

    public static String WORK_ITEM_HANDLER_EXTERNAL_NAME = "External Send Task";

    private static final Logger LOG = LoggerFactory.getLogger(SignalEventProcessInstanceAction.class);

    public static final String UNSET_SCOPE = System.getProperty("org.jbpm.signals.defaultscope", PROCESS_INSTANCE_SCOPE);

    private String eventTypeTemplate;
    private String variableNameExpression;
    private Function<KogitoProcessContext, Object> eventDataSupplier = (kcontext) -> null;
    private String scope = UNSET_SCOPE;
    private String inputVariable;

    public AbstractEventProcessInstanceAction(String eventTypeExpression, String variableNameExpression) {
        this(eventTypeExpression, variableNameExpression, DEFAULT_SCOPE);
    }

    public AbstractEventProcessInstanceAction(String signalName, String variableNameExpression, String scope) {
        this(signalName, variableNameExpression, null, scope);
    }

    public AbstractEventProcessInstanceAction(String signalName, String variableNameExpression, String inputVariable, String scope) {
        this.eventTypeTemplate = signalName;
        this.variableNameExpression = variableNameExpression;
        this.inputVariable = inputVariable;
        this.scope = (scope != null) ? scope : DEFAULT_SCOPE;
    }

    public AbstractEventProcessInstanceAction(String signalName, Function<KogitoProcessContext, Object> eventDataSupplier, String scope) {
        this.eventTypeTemplate = signalName;
        this.eventDataSupplier = eventDataSupplier;
        this.scope = (scope != null) ? scope : DEFAULT_SCOPE;
    }

    @Override
    public void execute(KogitoProcessContext context) throws Exception {
        String variableName = VariableUtil.resolveVariable(this.variableNameExpression, context.getNodeInstance());

        KogitoProcessInstance processInstance = context.getProcessInstance();
        KogitoNodeInstance nodeInstance = context.getNodeInstance();

        Object eventPayload = null;
        if (inputVariable != null) {
            eventPayload = context.getContextData().get(inputVariable);
        }
        if (eventPayload == null) {
            if (variableName != null) {
                eventPayload = context.getVariable(variableName);
            } else {
                eventPayload = eventDataSupplier.apply(context);
            }
        }
        if (eventPayload == null) {
            eventPayload = variableName;
        }
        // compute inputs for throwing
        Map<String, Object> inputSet = new HashMap<>();
        inputSet.put("Data", transform(context, eventPayload));

        String eventType = VariableUtil.resolveVariable(this.eventTypeTemplate, context.getNodeInstance());

        this.notifyEvent(context, processInstance, nodeInstance, context.getKieRuntime(), eventType, eventPayload);
        LOG.debug("about to signal {} process {} with scope {}", eventType, processInstance.getId(), scope);
        switch (scope) {
            case DEFAULT_SCOPE:
                context.getKogitoProcessRuntime().signalEvent(eventType, eventPayload);
                break;
            case PROCESS_INSTANCE_SCOPE:
                context.getProcessInstance().signalEvent(eventType, eventPayload);
                break;
            case EXTERNAL_SCOPE:
                KogitoWorkItemImpl workItem = new KogitoWorkItemImpl();
                workItem.setId(UUID.randomUUID().toString());
                workItem.setName(WORK_ITEM_HANDLER_EXTERNAL_NAME);
                workItem.setNodeInstanceId(context.getNodeInstance().getStringId());
                workItem.setProcessInstanceId(context.getProcessInstance().getStringId());
                workItem.setProcessInstance(processInstance);
                workItem.setNodeId(context.getNodeInstance().getNodeId());

                workItem.getParameters().putAll(inputSet);

                workItem.setParameter("Signal", eventType);
                workItem.setParameter("SignalProcessInstanceId", context.getVariable("SignalProcessInstanceId"));
                workItem.setParameter("SignalWorkItemId", context.getVariable("SignalWorkItemId"));
                workItem.setParameter("SignalDeploymentId", context.getVariable("SignalDeploymentId"));

                ((InternalKogitoWorkItemManager) context.getKogitoProcessRuntime().getKogitoWorkItemManager()).internalExecuteWorkItem(workItem);
                break;
        }

    }

    protected Object transform(KogitoProcessContext context, Object payload) {
        return payload;
    }

    protected abstract void notifyEvent(KogitoProcessContext context, KogitoProcessInstance processInstance, KogitoNodeInstance nodeInstance, KieRuntime kieRuntime, String eventType, Object event);

    public String getEventTypeExpression() {
        return eventTypeTemplate;
    }
}
