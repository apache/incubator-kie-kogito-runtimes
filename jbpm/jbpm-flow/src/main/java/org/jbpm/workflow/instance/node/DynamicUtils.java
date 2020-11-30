/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workflow.instance.node;

import java.util.Map;
import java.util.regex.Matcher;

import org.drools.core.process.instance.impl.KogitoWorkItemImpl;
import org.jbpm.util.PatternConstants;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.impl.MVELProcessHelper;
import org.jbpm.workflow.instance.impl.ProcessInstanceResolverFactory;
import org.kie.kogito.internal.runtime.EnvironmentName;
import org.kie.kogito.internal.runtime.KieRuntime;
import org.kie.kogito.internal.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicUtils {

    private static final Logger logger = LoggerFactory.getLogger(DynamicUtils.class);

    public static void addDynamicWorkItem(
            final DynamicNodeInstance dynamicContext,
            KieRuntime ksession,
            String workItemName,
            Map<String, Object> parameters) {
        final WorkflowProcessInstance processInstance = dynamicContext.getProcessInstance();
        internalAddDynamicWorkItem(processInstance,
                                   dynamicContext,
                                   ksession,
                                   workItemName,
                                   parameters);
    }

    public static void addDynamicWorkItem(
            final org.kie.kogito.internal.runtime.process.ProcessInstance dynamicProcessInstance,
            KieRuntime ksession,
            String workItemName,
            Map<String, Object> parameters) {
        internalAddDynamicWorkItem((WorkflowProcessInstance) dynamicProcessInstance,
                                   null,
                                   ksession,
                                   workItemName,
                                   parameters);
    }

    private static void internalAddDynamicWorkItem(
            final WorkflowProcessInstance processInstance,
            final DynamicNodeInstance dynamicContext,
            KieRuntime ksession,
            String workItemName,
            Map<String, Object> parameters) {
        final KogitoWorkItemImpl workItem = new KogitoWorkItemImpl();
        workItem.setState(WorkItem.ACTIVE);
        workItem.setProcessInstanceId(processInstance.getId());
        workItem.setDeploymentId((String) ksession.getEnvironment().get(EnvironmentName.DEPLOYMENT_ID));
        workItem.setName(workItemName);
        workItem.setParameters(parameters);

        for (Map.Entry<String, Object> entry : workItem.getParameters().entrySet()) {
            if (entry.getValue() instanceof String) {
                String s = (String) entry.getValue();
                Object variableValue = null;
                Matcher matcher = PatternConstants.PARAMETER_MATCHER.matcher(s);
                while (matcher.find()) {
                    String paramName = matcher.group(1);
                    variableValue = processInstance.getVariable(paramName);
                    if (variableValue == null) {
                        try {
                            variableValue = MVELProcessHelper.evaluator().eval(paramName,
                                                                                            new ProcessInstanceResolverFactory(processInstance));
                        } catch (Throwable t) {
                            logger.error("Could not find variable scope for variable {}",
                                         paramName);
                            logger.error("when trying to replace variable in string for Dynamic Work Item {}",
                                         workItemName);
                            logger.error("Continuing without setting parameter.");
                        }
                    }
                }
                if (variableValue != null) {
                    workItem.setParameter(entry.getKey(),
                                          variableValue);
                }
            }
        }

        final WorkItemNodeInstance workItemNodeInstance = new WorkItemNodeInstance();
        workItemNodeInstance.internalSetWorkItem(workItem);
        workItemNodeInstance.setMetaData("NodeType",
                                         workItemName);
        workItem.setNodeInstanceId(workItemNodeInstance.getId());
        throw new IllegalArgumentException("Unsupported ksession: " + ksession == null ? "null" : ksession.getClass().getName());
    }


    public static String addDynamicSubProcess(
            final DynamicNodeInstance dynamicContext,
            KieRuntime ksession,
            final String processId,
            final Map<String, Object> parameters) {
        final WorkflowProcessInstance processInstance = dynamicContext.getProcessInstance();
        return internalAddDynamicSubProcess(processInstance,
                                            dynamicContext,
                                            ksession,
                                            processId,
                                            parameters);
    }

    public static String addDynamicSubProcess(
            final org.kie.kogito.internal.runtime.process.ProcessInstance processInstance,
            KieRuntime ksession,
            final String processId,
            final Map<String, Object> parameters) {
        return internalAddDynamicSubProcess((WorkflowProcessInstance) processInstance,
                                            null,
                                            ksession,
                                            processId,
                                            parameters);
    }

    public static String internalAddDynamicSubProcess(
            final WorkflowProcessInstance processInstance,
            final DynamicNodeInstance dynamicContext,
            KieRuntime ksession,
            final String processId,
            final Map<String, Object> parameters) {
        final SubProcessNodeInstance subProcessNodeInstance = new SubProcessNodeInstance();
        subProcessNodeInstance.setNodeInstanceContainer(dynamicContext == null ? processInstance : dynamicContext);
        subProcessNodeInstance.setProcessInstance(processInstance);
        subProcessNodeInstance.setMetaData("NodeType",
                                           "SubProcessNode");
        throw new IllegalArgumentException("Unsupported ksession: " + (ksession == null ? "null" : ksession.getClass().getName()));
    }


    private DynamicUtils() {
        // It is not allowed to create instances of util classes.
    }
}
