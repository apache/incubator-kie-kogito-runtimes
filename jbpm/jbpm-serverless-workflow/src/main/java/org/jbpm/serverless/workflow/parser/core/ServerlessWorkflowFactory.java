/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.serverless.workflow.parser.core;

import com.fasterxml.jackson.databind.JsonNode;
import org.drools.compiler.rule.builder.dialect.java.JavaDialect;
import org.jbpm.process.core.Work;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.process.core.impl.WorkImpl;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.process.core.validation.ProcessValidationError;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.validation.RuleFlowProcessValidator;
import org.jbpm.serverless.workflow.api.Workflow;
import org.jbpm.serverless.workflow.api.end.End;
import org.jbpm.serverless.workflow.api.events.EventDefinition;
import org.jbpm.serverless.workflow.api.functions.Function;
import org.jbpm.serverless.workflow.parser.util.ServerlessWorkflowUtils;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;
import org.jbpm.workflow.core.node.*;
import org.kie.api.definition.process.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ServerlessWorkflowFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerlessWorkflowFactory.class);

    protected static final String EOL = System.getProperty( "line.separator" );
    private static final String DEFAULT_WORKFLOW_ID = "serverless";
    private static final String DEFAULT_WORKFLOW_NAME = "workflow";
    private static final String DEFAULT_WORKFLOW_VERSION = "1.0";
    private static final String DEFAULT_PACKAGE_NAME = "org.kie.kogito.serverless";
    private static final String DEFAULT_VISIBILITY = "Public";
    private static final String DEFAULT_VAR = "Var";
    private static final String JSON_NODE = "com.fasterxml.jackson.databind.JsonNode";
    private static final String DEFAULT_WORKFLOW_VAR = "workflowdata";
    private static final Set<String> DEFAULT_IMPORTS = new HashSet<String>(Arrays.asList(JSON_NODE));

    public RuleFlowProcess createProcess(Workflow workflow) {
        RuleFlowProcess process = new RuleFlowProcess();

        if(workflow.getId() != null && !workflow.getId().isEmpty()) {
            process.setId(workflow.getId());
        } else {
            LOGGER.info("setting default id {}", DEFAULT_WORKFLOW_ID);
            process.setId(DEFAULT_WORKFLOW_ID);
        }

        if(workflow.getName() != null && !workflow.getName().isEmpty()) {
            process.setName(workflow.getName());
        } else {
            LOGGER.info("setting default name {}", DEFAULT_WORKFLOW_NAME);
            process.setId(DEFAULT_WORKFLOW_NAME);
        }

        if(workflow.getVersion() != null && !workflow.getVersion().isEmpty()) {
            process.setVersion(workflow.getVersion());
        } else {
            LOGGER.info("setting default version {}", DEFAULT_WORKFLOW_VERSION);
            process.setVersion(DEFAULT_WORKFLOW_VERSION);
        }

        if(workflow.getMetadata() != null && workflow.getMetadata().get("package") != null) {
            process.setPackageName(workflow.getMetadata().get("package"));
        } else {
            process.setPackageName(DEFAULT_PACKAGE_NAME);
        }

        process.setImports(DEFAULT_IMPORTS);
        process.setAutoComplete(true);
        process.setVisibility(DEFAULT_VISIBILITY);

        // add workflow data var
        processVar(DEFAULT_WORKFLOW_VAR, process);

        return process;
    }

    public StartNode startNode(long id, String name, NodeContainer nodeContainer) {
        StartNode startNode = new StartNode();
        startNode.setId(id);
        startNode.setName(name);

        nodeContainer.addNode(startNode);

        return startNode;
    }

    public StartNode messageStartNode(long id, EventDefinition eventDefinition, NodeContainer nodeContainer) {

        StartNode startNode = new StartNode();
        startNode.setId(id);
        startNode.setName(eventDefinition.getName());
        startNode.setMetaData("TriggerMapping", eventDefinition.getSource() + DEFAULT_VAR);
        startNode.setMetaData("TriggerType", "ConsumeMessage");
        startNode.setMetaData("TriggerRef", eventDefinition.getSource());
        startNode.setMetaData("MessageType", JSON_NODE);
        addTriggerToStartNode(startNode, JSON_NODE);

        nodeContainer.addNode(startNode);

        return startNode;
    }

    public EndNode endNode(long id, String name, boolean terminate, NodeContainer nodeContainer) {
        EndNode endNode = new EndNode();
        endNode.setId(id);
        endNode.setName(name);
        endNode.setTerminate(terminate);

        nodeContainer.addNode(endNode);

        return endNode;
    }

    public EndNode messageEndNode(long id, Workflow workflow, End stateEnd, NodeContainer nodeContainer) {
        EndNode endNode = new EndNode();
        endNode.setTerminate(false);
        endNode.setId(id);

        EventDefinition eventDef = ServerlessWorkflowUtils.getWorkflowEventFor(workflow, stateEnd.getProduceEvent().getNameRef());

        endNode.setName(eventDef.getName());

        endNode.setMetaData("TriggerRef", eventDef.getSource());
        endNode.setMetaData("TriggerType", "ProduceMessage");
        endNode.setMetaData("MessageType", JSON_NODE);
        endNode.setMetaData("MappingVariable", stateEnd.getProduceEvent().getData());
        addMessageEndNodeAction(endNode, stateEnd.getProduceEvent().getData(), JSON_NODE);

        nodeContainer.addNode(endNode);

        return endNode;
    }

    public TimerNode timerNode(long id, String name, String delay, NodeContainer nodeContainer) {
        TimerNode timerNode = new TimerNode();
        timerNode.setId(id);
        timerNode.setName(name);
        timerNode.setMetaData("EventType", "timer");

        Timer timer = new Timer();
        timer.setTimeType(Timer.TIME_DURATION);
        timer.setDelay(delay);
        timerNode.setTimer(timer);

        nodeContainer.addNode(timerNode);

        return timerNode;
    }

    public SubProcessNode callActivity(long id, String name, String calledId, boolean waitForCompletion, NodeContainer nodeContainer) {
        SubProcessNode subProcessNode = new SubProcessNode();
        subProcessNode.setId(id);
        subProcessNode.setName(name);
        subProcessNode.setProcessId(calledId);
        subProcessNode.setWaitForCompletion(waitForCompletion);

        nodeContainer.addNode(subProcessNode);

        return subProcessNode;
    }

    private void addMessageEndNodeAction(EndNode endNode, String variable, String messageType){
        List<DroolsAction> actions = new ArrayList<DroolsAction>();

        actions.add(new DroolsConsequenceAction("java",
                "org.drools.core.process.instance.impl.WorkItemImpl workItem = new org.drools.core.process.instance.impl.WorkItemImpl();" + EOL +
                        "workItem.setName(\"Send Task\");" + EOL +
                        "workItem.setNodeInstanceId(kcontext.getNodeInstance().getId());" + EOL +
                        "workItem.setProcessInstanceId(kcontext.getProcessInstance().getId());" + EOL +
                        "workItem.setNodeId(kcontext.getNodeInstance().getNodeId());" + EOL +
                        "workItem.setParameter(\"MessageType\", \"" + messageType + "\");" + EOL +
                        (variable == null ? "" : "workItem.setParameter(\"Message\", " + variable + ");" + EOL) +
                        "workItem.setDeploymentId((String) kcontext.getKnowledgeRuntime().getEnvironment().get(\"deploymentId\"));" + EOL +
                        "((org.drools.core.process.instance.WorkItemManager) kcontext.getKnowledgeRuntime().getWorkItemManager()).internalExecuteWorkItem(workItem);"));
        endNode.setActions(ExtendedNodeImpl.EVENT_NODE_ENTER, actions);
    }

    private void addTriggerToStartNode(StartNode startNode, String triggerEventType) {
        EventTrigger trigger = new EventTrigger();
        EventTypeFilter eventFilter = new EventTypeFilter();
        eventFilter.setType(triggerEventType);
        trigger.addEventFilter(eventFilter);

        String mapping = (String) startNode.getMetaData("TriggerMapping");
        if (mapping != null) {
            trigger.addInMapping(mapping, startNode.getOutMapping(mapping));
        }

        startNode.addTrigger(trigger);
    }

    public ActionNode scriptNode(long id, String name, String script, NodeContainer nodeContainer) {
        ActionNode scriptNode = new ActionNode();
        scriptNode.setId(id);
        scriptNode.setName(name);

        scriptNode.setAction(new DroolsConsequenceAction());
        ((DroolsConsequenceAction)scriptNode.getAction()).setConsequence(script);
        ((DroolsConsequenceAction)scriptNode.getAction()).setDialect(JavaDialect.ID);

        nodeContainer.addNode(scriptNode);

        return scriptNode;
    }

    public WorkItemNode serviceNode(long id, String name, Function function, NodeContainer nodeContainer) {
        WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setId(id);
        workItemNode.setName(name);
        workItemNode.setMetaData("Type", "Service Task");

        Work work = new WorkImpl();
        workItemNode.setWork(work);

        work.setName("Service Task");
        work.setParameter("Interface", function.getMetadata().get("interface"));
        work.setParameter("Operation", function.getMetadata().get("operation"));
        work.setParameter("interfaceImplementationRef", function.getMetadata().get("interface"));
        work.setParameter("operationImplementationRef", function.getMetadata().get("operation"));
        work.setParameter("ParameterType", JSON_NODE);
        String metaImpl = function.getMetadata().get("implementation");
        if(metaImpl == null) {
            metaImpl = "Java";
        }
        work.setParameter("implementation", metaImpl);

        workItemNode.addInMapping("Parameter", DEFAULT_WORKFLOW_VAR);
        workItemNode.addOutMapping("Result", DEFAULT_WORKFLOW_VAR);

        nodeContainer.addNode(workItemNode);

        return workItemNode;

    }

    public static void processVar(String name, RuleFlowProcess process) {
        Variable variable = new Variable();
        variable.setName(name);
        variable.setType(new ObjectDataType(JsonNode.class.getName()));
        process.getVariableScope().getVariables().add(variable);
    }

    public CompositeContextNode subProcessNode(long id, String name, NodeContainer nodeContainer) {
        CompositeContextNode subProcessNode = new CompositeContextNode();
        subProcessNode.setId(id);
        subProcessNode.setName(name);
        VariableScope variableScope = new VariableScope();
        subProcessNode.addContext(variableScope);
        subProcessNode.setDefaultContext(variableScope);
        subProcessNode.setAutoComplete(true);

        nodeContainer.addNode(subProcessNode);

        return subProcessNode;
    }

    public void connect(long fromId, long toId, String uniqueId, NodeContainer nodeContainer) {
        Node from = nodeContainer.getNode(fromId);
        Node to = nodeContainer.getNode(toId);
        ConnectionImpl connection = new ConnectionImpl(
                from, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE,
                to, org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE);
        connection.setMetaData("UniqueId", uniqueId);
    }

    public void validate(RuleFlowProcess process) {
        ProcessValidationError[] errors = RuleFlowProcessValidator.getInstance().validateProcess(process);
        for (ProcessValidationError error : errors) {
            LOGGER.error(error.toString());
        }
        if (errors.length > 0) {
            throw new RuntimeException("Workflow could not be validated !");
        }
    }

}
