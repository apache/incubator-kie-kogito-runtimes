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
package org.jbpm.flow.serialization.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jbpm.flow.serialization.MarshallerContextName;
import org.jbpm.flow.serialization.MarshallerReaderContext;
import org.jbpm.flow.serialization.ProcessInstanceMarshallerException;
import org.jbpm.flow.serialization.ProcessInstanceMarshallerListener;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.AsyncEventNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.CompositeContextNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.DynamicNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.EventNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.EventSubProcessNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.ForEachNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.JoinNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.LambdaSubProcessNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.MilestoneNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.RuleSetNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.StateNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.SubProcessNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.TimerNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.WorkItemNodeInstanceContent;
import org.jbpm.flow.serialization.protobuf.KogitoProcessInstanceProtobuf;
import org.jbpm.flow.serialization.protobuf.KogitoTypesProtobuf;
import org.jbpm.flow.serialization.protobuf.KogitoTypesProtobuf.SLAContext;
import org.jbpm.flow.serialization.protobuf.KogitoTypesProtobuf.WorkflowContext;
import org.jbpm.flow.serialization.protobuf.KogitoWorkItemsProtobuf;
import org.jbpm.flow.serialization.protobuf.KogitoWorkItemsProtobuf.HumanTaskWorkItemData;
import org.jbpm.process.core.context.exclusive.ExclusiveGroup;
import org.jbpm.process.core.context.swimlane.SwimlaneContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.exclusive.ExclusiveGroupInstance;
import org.jbpm.process.instance.context.swimlane.SwimlaneContextInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.humantask.HumanTaskWorkItemImpl;
import org.jbpm.process.instance.impl.humantask.InternalHumanTaskWorkItem;
import org.jbpm.process.instance.impl.humantask.Reassignment;
import org.jbpm.ruleflow.core.WorkflowElementIdentifierFactory;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.core.node.AsyncEventNodeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.DynamicNodeInstance;
import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.jbpm.workflow.instance.node.EventSubProcessNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.jbpm.workflow.instance.node.JoinInstance;
import org.jbpm.workflow.instance.node.LambdaSubProcessNodeInstance;
import org.jbpm.workflow.instance.node.MilestoneNodeInstance;
import org.jbpm.workflow.instance.node.RuleSetNodeInstance;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.definition.process.WorkflowElementIdentifier;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstanceContainer;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.workitem.Attachment;
import org.kie.kogito.process.workitem.Comment;
import org.kie.kogito.process.workitems.InternalKogitoWorkItem;
import org.kie.kogito.process.workitems.impl.KogitoWorkItemImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import static org.jbpm.flow.serialization.protobuf.ProtobufTypeRegistryFactory.protobufTypeRegistryFactoryInstance;

public class ProtobufProcessInstanceReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufProcessInstanceReader.class);
    private RuleFlowProcessInstance ruleFlowProcessInstance;
    private MarshallerReaderContext context;
    private ProtobufVariableReader varReader;
    private ProcessInstanceMarshallerListener[] listeners;

    public ProtobufProcessInstanceReader(MarshallerReaderContext context) {
        this.context = context;
        this.ruleFlowProcessInstance = new RuleFlowProcessInstance();
        this.varReader = new ProtobufVariableReader(context);
        this.listeners = context.get(MarshallerContextName.MARSHALLER_INSTANCE_LISTENER);
    }

    public RuleFlowProcessInstance read(InputStream input) throws IOException {
        LOGGER.info("read process");
        KogitoProcessInstanceProtobuf.ProcessInstance processInstanceProtobuf;

        String format = this.context.get(MarshallerContextName.MARSHALLER_FORMAT);
        if (format != null && MarshallerContextName.MARSHALLER_FORMAT_JSON.equals(format)) {
            KogitoProcessInstanceProtobuf.ProcessInstance.Builder builder = KogitoProcessInstanceProtobuf.ProcessInstance.newBuilder();
            JsonFormat.parser().usingTypeRegistry(protobufTypeRegistryFactoryInstance().create()).ignoringUnknownFields().merge(new InputStreamReader(input), builder);
            processInstanceProtobuf = builder.build();
        } else {
            processInstanceProtobuf = KogitoProcessInstanceProtobuf.ProcessInstance.parseFrom(input);
        }
        return buildWorkflow(processInstanceProtobuf);
    }

    private RuleFlowProcessInstance buildWorkflow(KogitoProcessInstanceProtobuf.ProcessInstance processInstanceProtobuf) {

        RuleFlowProcessInstance processInstance = ruleFlowProcessInstance;
        processInstance.setProcessId(processInstanceProtobuf.getProcessId());
        processInstance.setProcessVersion(processInstanceProtobuf.getProcessVersion());

        processInstance.setInternalProcess(((AbstractProcess<?>) context.get(MarshallerContextName.MARSHALLER_PROCESS)).get());

        processInstance.setId(processInstanceProtobuf.getId());
        processInstance.setState(processInstanceProtobuf.getState());
        processInstance.setSignalCompletion(processInstanceProtobuf.getSignalCompletion());

        if (processInstanceProtobuf.hasStartDate()) {
            processInstance.setStartDate(new Date(processInstanceProtobuf.getStartDate()));
        }

        if (processInstanceProtobuf.hasDescription()) {
            processInstance.setDescription(processInstanceProtobuf.getDescription());
        }

        if (processInstanceProtobuf.hasDeploymentId()) {
            processInstance.setDeploymentId(processInstanceProtobuf.getDeploymentId());
        }

        for (String completedNodeId : processInstanceProtobuf.getCompletedNodeIdsList()) {
            processInstance.addCompletedNodeId(completedNodeId);
        }

        if (processInstanceProtobuf.hasBusinessKey()) {
            processInstance.setCorrelationKey(processInstanceProtobuf.getBusinessKey());
        }

        if (processInstanceProtobuf.hasSla()) {
            SLAContext slaContext = processInstanceProtobuf.getSla();
            if (slaContext.getSlaDueDate() > 0) {
                processInstance.internalSetSlaDueDate(new Date(slaContext.getSlaDueDate()));
            }

            if (slaContext.hasSlaTimerId()) {
                processInstance.internalSetSlaTimerId(slaContext.getSlaTimerId());
            }
            if (slaContext.hasSlaCompliance()) {
                processInstance.internalSetSlaCompliance(slaContext.getSlaCompliance());
            }
        }

        if (processInstanceProtobuf.hasCancelTimerId()) {
            processInstance.internalSetCancelTimerId(processInstanceProtobuf.getCancelTimerId());
        }

        if (processInstanceProtobuf.hasParentProcessInstanceId()) {
            processInstance.setParentProcessInstanceId(processInstanceProtobuf.getParentProcessInstanceId());
        }
        if (processInstanceProtobuf.hasRootProcessInstanceId()) {
            processInstance.setRootProcessInstanceId(processInstanceProtobuf.getRootProcessInstanceId());
        }
        if (processInstanceProtobuf.hasRootProcessId()) {
            processInstance.setRootProcessId(processInstanceProtobuf.getRootProcessId());
        }

        if (processInstanceProtobuf.hasErrorNodeId()) {
            processInstance.internalSetErrorNodeId(processInstanceProtobuf.getErrorNodeId());
        }

        if (processInstanceProtobuf.hasErrorMessage()) {
            processInstance.internalSetErrorMessage(processInstanceProtobuf.getErrorMessage());
        }

        if (processInstanceProtobuf.hasReferenceId()) {
            processInstance.setReferenceId(processInstanceProtobuf.getReferenceId());
        }

        if (processInstanceProtobuf.getSwimlaneContextCount() > 0) {
            SwimlaneContextInstance swimlaneContextInstance = (SwimlaneContextInstance) processInstance.getContextInstance(SwimlaneContext.SWIMLANE_SCOPE);
            for (KogitoTypesProtobuf.SwimlaneContext _swimlane : processInstanceProtobuf.getSwimlaneContextList()) {
                swimlaneContextInstance.setActorId(_swimlane.getSwimlane(), _swimlane.getActorId());
            }
        }

        WorkflowContext workflowContext = processInstanceProtobuf.getContext();

        for (KogitoTypesProtobuf.NodeInstance nodeInstanceProtobuf : workflowContext.getNodeInstanceList()) {
            buildNodeInstance(nodeInstanceProtobuf, processInstance);
        }

        for (KogitoTypesProtobuf.NodeInstanceGroup group : workflowContext.getExclusiveGroupList()) {
            Function<String, KogitoNodeInstance> finder = nodeInstanceId -> processInstance.getNodeInstance(nodeInstanceId, true);
            processInstance.addContextInstance(ExclusiveGroup.EXCLUSIVE_GROUP, buildExclusiveGroupInstance(group, finder));
        }

        processInstance.addContextInstance(VariableScope.VARIABLE_SCOPE, new VariableScopeInstance());
        if (workflowContext.getVariableCount() > 0) {
            VariableScopeInstance variableScopeInstance = (VariableScopeInstance) processInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
            varReader.buildVariables(workflowContext.getVariableList()).forEach(v -> variableScopeInstance.internalSetVariable(v.getName(), v.getValue()));
        }

        if (workflowContext.getIterationLevelsCount() > 0) {
            processInstance.getIterationLevels().putAll(buildIterationLevels(workflowContext.getIterationLevelsList()));
        }
        KogitoProcessRuntime runtime = ((AbstractProcess<?>) context.get(MarshallerContextName.MARSHALLER_PROCESS)).getProcessRuntime();
        Arrays.stream(listeners).forEach(e -> e.afterUnmarshallProcess(runtime, processInstance));
        return processInstance;
    }

    private void setCommonNodeInstanceData(RuleFlowProcessInstance processInstance, KogitoNodeInstanceContainer parentContainer, KogitoTypesProtobuf.NodeInstance nodeInstanceProtobuf,
            NodeInstanceImpl nodeInstanceImpl) {
        if (nodeInstanceImpl.getStringId() == null) {
            nodeInstanceImpl.setId(nodeInstanceProtobuf.getId());
        }

        if (nodeInstanceImpl.getNodeId() == null) {
            nodeInstanceImpl.setNodeId(WorkflowElementIdentifierFactory.fromExternalFormat(nodeInstanceProtobuf.getNodeId()));
        }

        if (nodeInstanceImpl.getNodeInstanceContainer() == null) {
            nodeInstanceImpl.setNodeInstanceContainer(parentContainer);
        }
        if (nodeInstanceImpl.getProcessInstance() == null) {
            nodeInstanceImpl.setProcessInstance(processInstance);
        }

        nodeInstanceImpl.setLevel(nodeInstanceProtobuf.getLevel() == 0 ? 1 : nodeInstanceProtobuf.getLevel());
    }

    protected NodeInstanceImpl buildNodeInstance(KogitoTypesProtobuf.NodeInstance nodeInstance, KogitoNodeInstanceContainer parent) {
        final com.google.protobuf.Any nodeContentProtobuf = nodeInstance.getContent();
        NodeInstanceImpl result = null;
        try {
            if (nodeContentProtobuf.is(RuleSetNodeInstanceContent.class)) {
                RuleSetNodeInstanceContent content = nodeContentProtobuf.unpack(RuleSetNodeInstanceContent.class);
                result = buildRuleSetNodeInstance(content);
            } else if (nodeContentProtobuf.is(ForEachNodeInstanceContent.class)) {
                ForEachNodeInstanceContent content = nodeContentProtobuf.unpack(ForEachNodeInstanceContent.class);
                result = buildForEachNodeInstance(content, nodeInstance, parent);
            } else if (nodeContentProtobuf.is(LambdaSubProcessNodeInstanceContent.class)) {
                LambdaSubProcessNodeInstanceContent content = nodeContentProtobuf.unpack(LambdaSubProcessNodeInstanceContent.class);
                result = buildLambdaSubProcessNodeInstance(content);
            } else if (nodeContentProtobuf.is(SubProcessNodeInstanceContent.class)) {
                SubProcessNodeInstanceContent content = nodeContentProtobuf.unpack(SubProcessNodeInstanceContent.class);
                result = buildSubProcessNodeInstance(content);
            } else if (nodeContentProtobuf.is(StateNodeInstanceContent.class)) {
                StateNodeInstanceContent content = nodeContentProtobuf.unpack(StateNodeInstanceContent.class);
                result = buildStateNodeInstance(content);
            } else if (nodeContentProtobuf.is(JoinNodeInstanceContent.class)) {
                JoinNodeInstanceContent content = nodeContentProtobuf.unpack(JoinNodeInstanceContent.class);
                result = buildJoinInstance(content);
            } else if (nodeContentProtobuf.is(TimerNodeInstanceContent.class)) {
                TimerNodeInstanceContent content = nodeContentProtobuf.unpack(TimerNodeInstanceContent.class);
                result = buildTimerNodeInstance(content);
            } else if (nodeContentProtobuf.is(EventNodeInstanceContent.class)) {
                result = buildEventNodeInstance();
            } else if (nodeContentProtobuf.is(MilestoneNodeInstanceContent.class)) {
                MilestoneNodeInstanceContent content = nodeContentProtobuf.unpack(MilestoneNodeInstanceContent.class);
                result = buildMilestoneNodeInstance(content);
            } else if (nodeContentProtobuf.is(DynamicNodeInstanceContent.class)) {
                DynamicNodeInstanceContent content = nodeContentProtobuf.unpack(DynamicNodeInstanceContent.class);
                result = buildDynamicNodeInstance(content, nodeInstance, parent);
            } else if (nodeContentProtobuf.is(EventSubProcessNodeInstanceContent.class)) {
                EventSubProcessNodeInstanceContent content = nodeContentProtobuf.unpack(EventSubProcessNodeInstanceContent.class);
                result = buildEventSubProcessNodeInstance(content);
            } else if (nodeContentProtobuf.is(CompositeContextNodeInstanceContent.class)) {
                CompositeContextNodeInstanceContent content = nodeContentProtobuf.unpack(CompositeContextNodeInstanceContent.class);
                result = buildCompositeContextNodeInstance(content, nodeInstance, parent);
            } else if (nodeContentProtobuf.is(WorkItemNodeInstanceContent.class)) {
                WorkItemNodeInstanceContent content = nodeContentProtobuf.unpack(WorkItemNodeInstanceContent.class);
                result = buildWorkItemNodeInstance(content);
            } else if (nodeContentProtobuf.is(AsyncEventNodeInstanceContent.class)) {
                AsyncEventNodeInstanceContent content = nodeContentProtobuf.unpack(AsyncEventNodeInstanceContent.class);
                result = buildAsyncEventNodeInstance(content);
            }

            if (Objects.isNull(result)) {
                throw new IllegalArgumentException("Unknown node instance");
            }

            setCommonNodeInstanceData(ruleFlowProcessInstance, parent, nodeInstance, result);

            SLAContext slaNodeInstanceContext = nodeInstance.getSla();
            result.internalSetSlaCompliance(slaNodeInstanceContext.getSlaCompliance());
            if (slaNodeInstanceContext.getSlaDueDate() > 0) {
                result.internalSetSlaDueDate(new Date(slaNodeInstanceContext.getSlaDueDate()));
            }
            result.internalSetSlaTimerId(slaNodeInstanceContext.getSlaTimerId());
            if (nodeInstance.hasTriggerDate()) {
                result.internalSetTriggerTime(new Date(nodeInstance.getTriggerDate()));
            }

            KogitoNodeInstance kogitoNodeInstance = (KogitoNodeInstance) result;
            KogitoProcessRuntime runtime = ((AbstractProcess<?>) context.get(MarshallerContextName.MARSHALLER_PROCESS)).getProcessRuntime();
            Arrays.stream(listeners).forEach(e -> e.afterUnmarshallNode(runtime, kogitoNodeInstance));
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read node instance content");
        }
    }

    private NodeInstanceImpl buildAsyncEventNodeInstance(AsyncEventNodeInstanceContent content) {
        AsyncEventNodeInstance nodeInstance = new AsyncEventNodeInstance();
        nodeInstance.setJobId(content.getJobId());
        return nodeInstance;
    }

    private NodeInstanceImpl buildCompositeContextNodeInstance(CompositeContextNodeInstanceContent content, KogitoTypesProtobuf.NodeInstance protoNodeInstance,
            KogitoNodeInstanceContainer parentContainer) {
        CompositeContextNodeInstance nodeInstance = new CompositeContextNodeInstance();

        if (content.getTimerInstanceIdCount() > 0) {
            List<String> timerInstances = new ArrayList<>();
            for (String _timerId : content.getTimerInstanceIdList()) {
                timerInstances.add(_timerId);
            }
            nodeInstance.internalSetTimerInstances(timerInstances);
        }
        if (!content.getTimerInstanceReferenceMap().isEmpty()) {
            nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
        }

        setCommonNodeInstanceData(ruleFlowProcessInstance, parentContainer, protoNodeInstance, nodeInstance);

        buildWorkflowContext(nodeInstance, content.getContext());
        return nodeInstance;
    }

    private NodeInstanceImpl buildEventSubProcessNodeInstance(EventSubProcessNodeInstanceContent content) {
        EventSubProcessNodeInstance nodeInstance = new EventSubProcessNodeInstance();

        if (content.getTimerInstanceIdCount() > 0) {
            List<String> timerInstances = new ArrayList<>();
            for (String _timerId : content.getTimerInstanceIdList()) {
                timerInstances.add(_timerId);
            }
            nodeInstance.internalSetTimerInstances(timerInstances);
        }
        if (!content.getTimerInstanceReferenceMap().isEmpty()) {
            nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
        }
        buildWorkflowContext(nodeInstance, content.getContext());
        return nodeInstance;
    }

    private NodeInstanceImpl buildDynamicNodeInstance(DynamicNodeInstanceContent content, KogitoTypesProtobuf.NodeInstance protoNodeInstance,
            KogitoNodeInstanceContainer parentContainer) {
        DynamicNodeInstance nodeInstance = new DynamicNodeInstance();
        if (content.getTimerInstanceIdCount() > 0) {
            List<String> timerInstances = new ArrayList<>();
            for (String _timerId : content.getTimerInstanceIdList()) {
                timerInstances.add(_timerId);
            }
            nodeInstance.internalSetTimerInstances(timerInstances);
        }
        if (!content.getTimerInstanceReferenceMap().isEmpty()) {
            nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
        }

        setCommonNodeInstanceData(ruleFlowProcessInstance, parentContainer, protoNodeInstance, nodeInstance);
        buildWorkflowContext(nodeInstance, content.getContext());

        return nodeInstance;

    }

    private NodeInstanceImpl buildMilestoneNodeInstance(MilestoneNodeInstanceContent content) {
        MilestoneNodeInstance nodeInstance = new MilestoneNodeInstance();
        if (content.getTimerInstanceIdCount() > 0) {
            List<String> timerInstances = new ArrayList<>();
            for (String _timerId : content.getTimerInstanceIdList()) {
                timerInstances.add(_timerId);
            }
            nodeInstance.internalSetTimerInstances(timerInstances);
        }
        if (!content.getTimerInstanceReferenceMap().isEmpty()) {
            nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
        }
        return nodeInstance;
    }

    private NodeInstanceImpl buildEventNodeInstance() {
        return new EventNodeInstance();
    }

    private NodeInstanceImpl buildTimerNodeInstance(TimerNodeInstanceContent content) {
        TimerNodeInstance nodeInstance = new TimerNodeInstance();
        nodeInstance.internalSetTimerId(content.getTimerId());
        return nodeInstance;
    }

    private NodeInstanceImpl buildJoinInstance(JoinNodeInstanceContent content) {
        JoinInstance nodeInstance = new JoinInstance();
        if (content.getTriggerCount() > 0) {
            Map<WorkflowElementIdentifier, Integer> triggers = new HashMap<>();
            for (JoinNodeInstanceContent.JoinTrigger _join : content.getTriggerList()) {
                LOGGER.info("unmarshalling join {}", _join.getNodeId());
                triggers.put(WorkflowElementIdentifierFactory.fromExternalFormat(_join.getNodeId()), _join.getCounter());
            }
            nodeInstance.internalSetTriggers(triggers);
        }
        return nodeInstance;
    }

    private NodeInstanceImpl buildStateNodeInstance(StateNodeInstanceContent content) {
        StateNodeInstance nodeInstance = new StateNodeInstance();
        if (content.getTimerInstanceIdCount() > 0) {
            List<String> timerInstances = new ArrayList<>();
            for (String _timerId : content.getTimerInstanceIdList()) {
                timerInstances.add(_timerId);
            }
            nodeInstance.internalSetTimerInstances(timerInstances);
        }
        if (!content.getTimerInstanceReferenceMap().isEmpty()) {
            nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
        }
        return nodeInstance;
    }

    private NodeInstanceImpl buildSubProcessNodeInstance(SubProcessNodeInstanceContent content) {
        SubProcessNodeInstance nodeInstance = new SubProcessNodeInstance();
        nodeInstance.internalSetProcessInstanceId(content.getProcessInstanceId());
        if (content.getTimerInstanceIdCount() > 0) {
            List<String> timerInstances = new ArrayList<>();
            for (String timerId : content.getTimerInstanceIdList()) {
                timerInstances.add(timerId);
            }
            nodeInstance.internalSetTimerInstances(timerInstances);
        }
        if (!content.getTimerInstanceReferenceMap().isEmpty()) {
            nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
        }

        return nodeInstance;
    }

    private NodeInstanceImpl buildLambdaSubProcessNodeInstance(LambdaSubProcessNodeInstanceContent content) {
        LambdaSubProcessNodeInstance nodeInstance = new LambdaSubProcessNodeInstance();
        nodeInstance.internalSetProcessInstanceId(content.getProcessInstanceId());
        if (content.getTimerInstanceIdCount() > 0) {
            nodeInstance.internalSetTimerInstances(new ArrayList<>(content.getTimerInstanceIdList()));
        }
        if (!content.getTimerInstanceReferenceMap().isEmpty()) {
            nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
        }
        return nodeInstance;
    }

    private NodeInstanceImpl buildForEachNodeInstance(ForEachNodeInstanceContent content, KogitoTypesProtobuf.NodeInstance protoNodeInstance, KogitoNodeInstanceContainer parentContainer) {
        ForEachNodeInstance nodeInstance = new ForEachNodeInstance();
        nodeInstance.setExecutedInstances(content.getExecutedInstances());
        nodeInstance.setTotalInstances(content.getTotalInstances());
        nodeInstance.setHasAsyncInstances(content.getHasAsyncInstances());

        setCommonNodeInstanceData(ruleFlowProcessInstance, parentContainer, protoNodeInstance, nodeInstance);
        buildWorkflowContext(nodeInstance, content.getContext());
        return nodeInstance;
    }

    private NodeInstanceImpl buildRuleSetNodeInstance(RuleSetNodeInstanceContent content) {
        RuleSetNodeInstance nodeInstance = new RuleSetNodeInstance();
        nodeInstance.setRuleFlowGroup(content.getRuleFlowGroup());
        if (content.getTimerInstanceIdCount() > 0) {
            nodeInstance.internalSetTimerInstances(new ArrayList<>(content.getTimerInstanceIdList()));
        }
        if (!content.getTimerInstanceReferenceMap().isEmpty()) {
            nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
        }

        return nodeInstance;
    }

    private NodeInstanceImpl buildWorkItemNodeInstance(WorkItemNodeInstanceContent content) {
        try {
            WorkItemNodeInstance nodeInstance = instanceWorkItem(content);
            if (nodeInstance instanceof HumanTaskNodeInstance) {
                HumanTaskNodeInstance humanTaskNodeInstance = (HumanTaskNodeInstance) nodeInstance;
                InternalHumanTaskWorkItem workItem = humanTaskNodeInstance.getWorkItem();
                Any workItemDataMessage = content.getWorkItemData();
                if (workItemDataMessage.is(HumanTaskWorkItemData.class)) {
                    HumanTaskWorkItemData workItemData = workItemDataMessage.unpack(HumanTaskWorkItemData.class);
                    humanTaskNodeInstance.getNotCompletedDeadlineTimers().putAll(buildDeadlines(workItemData.getCompletedDeadlinesMap()));
                    humanTaskNodeInstance.getNotCompletedReassigments().putAll(buildReassignments(workItemData.getCompletedReassigmentsMap()));
                    humanTaskNodeInstance.getNotStartedDeadlineTimers().putAll(buildDeadlines(workItemData.getStartDeadlinesMap()));
                    humanTaskNodeInstance.getNotStartedReassignments().putAll(buildReassignments(workItemData.getStartReassigmentsMap()));

                    if (workItemData.hasTaskName()) {
                        workItem.setTaskName(workItemData.getTaskName());
                    }
                    if (workItemData.hasTaskDescription()) {
                        workItem.setTaskDescription(workItemData.getTaskDescription());
                    }
                    if (workItemData.hasTaskPriority()) {
                        workItem.setTaskPriority(workItemData.getTaskPriority());
                    }
                    if (workItemData.hasTaskReferenceName()) {
                        workItem.setReferenceName(workItemData.getTaskReferenceName());
                    }
                    if (workItemData.hasActualOwner()) {
                        workItem.setActualOwner(workItemData.getActualOwner());
                    }
                    workItem.getAdminUsers().addAll(workItemData.getAdminUsersList());
                    workItem.getAdminGroups().addAll(workItemData.getAdminGroupsList());
                    workItem.getPotentialUsers().addAll(workItemData.getPotUsersList());
                    workItem.getPotentialGroups().addAll(workItemData.getPotGroupsList());
                    workItem.getExcludedUsers().addAll(workItemData.getExcludedUsersList());
                    workItem.getComments().putAll(workItemData.getCommentsList().stream().map(this::buildComment).collect(Collectors.toMap(Comment::getId, Function.identity())));
                    workItem.getAttachments().putAll(workItemData.getAttachmentsList().stream().map(this::buildAttachment).collect(Collectors.toMap(Attachment::getId, Function.identity())));

                }

            }

            nodeInstance.internalSetWorkItemId(content.getWorkItemId());
            InternalKogitoWorkItem workItem = (InternalKogitoWorkItem) nodeInstance.getWorkItem();
            workItem.setId(content.getWorkItemId());
            workItem.setProcessInstanceId(ruleFlowProcessInstance.getStringId());
            workItem.setName(content.getName());
            workItem.setState(content.getState());
            workItem.setDeploymentId(ruleFlowProcessInstance.getDeploymentId());
            workItem.setProcessInstance(ruleFlowProcessInstance);
            workItem.setPhaseId(content.getPhaseId());
            workItem.setPhaseStatus(content.getPhaseStatus());
            workItem.setStartDate(new Date(content.getStartDate()));
            if (content.getCompleteDate() > 0) {
                workItem.setCompleteDate(new Date(content.getCompleteDate()));
            }

            if (content.getTimerInstanceIdCount() > 0) {
                nodeInstance.internalSetTimerInstances(new ArrayList<>(content.getTimerInstanceIdList()));
            }
            if (!content.getTimerInstanceReferenceMap().isEmpty()) {
                nodeInstance.internalSetTimerInstancesReference(new HashMap<>(content.getTimerInstanceReferenceMap()));
            }
            nodeInstance.internalSetProcessInstanceId(content.getErrorHandlingProcessInstanceId());
            varReader.buildVariables(content.getVariableList()).forEach(var -> nodeInstance.getWorkItem().getParameters().put(var.getName(), var.getValue()));
            varReader.buildVariables(content.getResultList()).forEach(var -> nodeInstance.getWorkItem().getResults().put(var.getName(), var.getValue()));
            return nodeInstance;
        } catch (InvalidProtocolBufferException ex) {
            throw new ProcessInstanceMarshallerException("cannot unpack node instance", ex);
        }
    }

    private WorkItemNodeInstance instanceWorkItem(WorkItemNodeInstanceContent content) {
        if (content.hasWorkItemData()) {
            Any workItemDataMessage = content.getWorkItemData();
            if (workItemDataMessage.is(HumanTaskWorkItemData.class)) {
                HumanTaskNodeInstance nodeInstance = new HumanTaskNodeInstance();
                HumanTaskWorkItemImpl workItem = new HumanTaskWorkItemImpl();
                nodeInstance.internalSetWorkItem(workItem);
                return nodeInstance;
            } else {
                throw new ProcessInstanceMarshallerException("Don't know which type of work item is");
            }
        } else {
            WorkItemNodeInstance nodeInstance = new WorkItemNodeInstance();
            KogitoWorkItemImpl workItem = new KogitoWorkItemImpl();
            workItem.setId(UUID.randomUUID().toString());
            nodeInstance.internalSetWorkItem(workItem);
            return nodeInstance;
        }
    }

    private void buildWorkflowContext(CompositeContextNodeInstance container, WorkflowContext workflowContext) {
        if (workflowContext.getNodeInstanceCount() > 0) {
            for (KogitoTypesProtobuf.NodeInstance nodeInstanceProtobuf : workflowContext.getNodeInstanceList()) {
                buildNodeInstance(nodeInstanceProtobuf, container);
            }
        }
        for (KogitoTypesProtobuf.NodeInstanceGroup group : workflowContext.getExclusiveGroupList()) {
            Function<String, KogitoNodeInstance> finder = nodeInstanceId -> container.getNodeInstance(nodeInstanceId, true);
            container.addContextInstance(ExclusiveGroup.EXCLUSIVE_GROUP, buildExclusiveGroupInstance(group, finder));
        }

        container.addContextInstance(VariableScope.VARIABLE_SCOPE, new VariableScopeInstance());
        if (workflowContext.getVariableCount() > 0) {
            VariableScopeInstance variableScopeInstance = (VariableScopeInstance) container.getContextInstance(VariableScope.VARIABLE_SCOPE);
            varReader.buildVariables(workflowContext.getVariableList()).forEach(v -> variableScopeInstance.internalSetVariable(v.getName(), v.getValue()));
        }
        if (workflowContext.getIterationLevelsCount() > 0) {
            container.getIterationLevels().putAll(buildIterationLevels(workflowContext.getIterationLevelsList()));
        }
    }

    private ExclusiveGroupInstance buildExclusiveGroupInstance(KogitoTypesProtobuf.NodeInstanceGroup group, Function<String, KogitoNodeInstance> finder) {
        ExclusiveGroupInstance exclusiveGroupInstance = new ExclusiveGroupInstance();
        for (String nodeInstanceId : group.getGroupNodeInstanceIdList()) {
            KogitoNodeInstance kogitoNodeInstance = finder.apply(nodeInstanceId);
            if (kogitoNodeInstance == null) {
                throw new IllegalArgumentException("Could not find node instance when deserializing exclusive group instance: " + nodeInstanceId);
            }
            exclusiveGroupInstance.addNodeInstance(kogitoNodeInstance);
        }
        return exclusiveGroupInstance;
    }

    private Map<String, Integer> buildIterationLevels(List<KogitoTypesProtobuf.IterationLevel> iterationLevel) {
        Function<KogitoTypesProtobuf.IterationLevel, String> mapKey = KogitoTypesProtobuf.IterationLevel::getId;
        Function<KogitoTypesProtobuf.IterationLevel, Integer> mapValue = KogitoTypesProtobuf.IterationLevel::getLevel;
        return iterationLevel.stream().collect(Collectors.toMap(mapKey, mapValue));
    }

    private Comment buildComment(KogitoWorkItemsProtobuf.Comment comment) {
        Comment result = new Comment(comment.getId(), comment.getUpdatedBy());
        result.setContent(comment.getContent());
        result.setUpdatedAt(new Date(comment.getUpdatedAt()));
        return result;
    }

    private Attachment buildAttachment(KogitoWorkItemsProtobuf.Attachment attachment) {
        Attachment result = new Attachment(attachment.getId(), attachment.getUpdatedBy());
        result.setContent(URI.create(attachment.getContent()));
        result.setUpdatedAt(new Date(attachment.getUpdatedAt()));
        result.setName(attachment.getName());
        return result;
    }

    private Map<String, Map<String, Object>> buildDeadlines(Map<String, KogitoWorkItemsProtobuf.Deadline> deadlinesProtobuf) {
        Map<String, Map<String, Object>> deadlines = new HashMap<>();
        for (Map.Entry<String, KogitoWorkItemsProtobuf.Deadline> entry : deadlinesProtobuf.entrySet()) {
            Map<String, Object> notification = new HashMap<>();
            for (Map.Entry<String, String> pair : entry.getValue().getContentMap().entrySet()) {
                notification.put(pair.getKey(), pair.getValue());
            }
            deadlines.put(entry.getKey(), notification);
        }
        return deadlines;
    }

    private Map<String, Reassignment> buildReassignments(Map<String, KogitoWorkItemsProtobuf.Reassignment> reassignmentsProtobuf) {
        Map<String, Reassignment> reassignments = new HashMap<>();
        for (Map.Entry<String, KogitoWorkItemsProtobuf.Reassignment> entry : reassignmentsProtobuf.entrySet()) {
            reassignments.put(entry.getKey(), new Reassignment(entry.getValue().getUsersList().stream().collect(Collectors
                    .toSet()), entry.getValue().getGroupsList().stream().collect(Collectors.toSet())));
        }
        return reassignments;
    }
}
