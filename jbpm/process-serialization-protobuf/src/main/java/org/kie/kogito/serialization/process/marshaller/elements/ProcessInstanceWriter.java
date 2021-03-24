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
package org.kie.kogito.serialization.process.marshaller.elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jbpm.process.core.context.exclusive.ExclusiveGroup;
import org.jbpm.process.core.context.swimlane.SwimlaneContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.context.exclusive.ExclusiveGroupInstance;
import org.jbpm.process.instance.context.swimlane.SwimlaneContextInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
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
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.process.workitem.Attachment;
import org.kie.kogito.process.workitem.Comment;
import org.kie.kogito.process.workitem.HumanTaskWorkItem;
import org.kie.kogito.serialization.process.ProcessMarshallerFactory;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.CompositeContextNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.DynamicNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.EventNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.EventSubProcessNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.ForEachNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.JoinNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.JoinNodeInstanceContent.JoinTrigger;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.LambdaSubProcessNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.MilestoneNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.RuleSetNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.StateNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.SubProcessNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.TimerNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoNodeInstanceContentsProtobuf.WorkItemNodeInstanceContent;
import org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf;
import org.kie.kogito.serialization.protobuf.KogitoTypesProtobuf;
import org.kie.kogito.serialization.protobuf.KogitoWorkItemsProtobuf;
import org.kie.kogito.serialization.protobuf.KogitoWorkItemsProtobuf.HumanTaskWorkItemData;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;

public class ProcessInstanceWriter {

    private Environment env;

    public ProcessInstanceWriter(Environment env) {
        this.env = env;
    }

    public void writeProcessInstance(WorkflowProcessInstanceImpl workFlow, OutputStream os) throws IOException {

        KogitoProcessInstanceProtobuf.ProcessInstance.Builder _instance = KogitoProcessInstanceProtobuf.ProcessInstance.newBuilder()
                .setId(workFlow.getStringId())
                .setProcessId(workFlow.getProcessId())
                .setState(workFlow.getState())
                .setProcessType(workFlow.getProcess().getType())
                .setSignalCompletion(workFlow.isSignalCompletion())
                .setStartDate(workFlow.getStartDate().getTime());

        if (workFlow.getDescription() != null) {
            _instance.setDescription(workFlow.getDescription());
        }
        if (workFlow.getDeploymentId() != null) {
            _instance.setDeploymentId(workFlow.getDeploymentId());
        }
        _instance.addAllCompletedNodeIds(workFlow.getCompletedNodeIds());
        if (workFlow.getCorrelationKey() != null) {
            _instance.setBusinessKey(workFlow.getCorrelationKey());
        }

        _instance.setSla(buildSLAContext(workFlow.getSlaCompliance(), workFlow.getSlaDueDate(), workFlow.getSlaTimerId()));

        if (workFlow.getParentProcessInstanceStringId() != null) {
            _instance.setParentProcessInstanceId(workFlow.getParentProcessInstanceStringId());
        }
        if (workFlow.getRootProcessInstanceId() != null) {
            _instance.setRootProcessInstanceId(workFlow.getRootProcessInstanceId());
        }
        if (workFlow.getRootProcessId() != null) {
            _instance.setRootProcessId(workFlow.getRootProcessId());
        }
        if (workFlow.getNodeIdInError() != null) {
            _instance.setErrorNodeId(workFlow.getNodeIdInError());
        }
        if (workFlow.getErrorMessage() != null) {
            _instance.setErrorMessage(workFlow.getErrorMessage());
        }
        if (workFlow.getReferenceId() != null) {
            _instance.setReferenceId(workFlow.getReferenceId());
        }

        _instance.addAllSwimlaneContext(buildSwimlaneContexts((SwimlaneContextInstance) workFlow.getContextInstance(SwimlaneContext.SWIMLANE_SCOPE)));

        List<NodeInstance> nodeInstances = new ArrayList<>(workFlow.getNodeInstances());
        List<ContextInstance> exclusiveGroupInstances = workFlow.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance) workFlow.getContextInstance(VariableScope.VARIABLE_SCOPE);
        List<Map.Entry<String, Object>> variables = new ArrayList<Map.Entry<String, Object>>(variableScopeInstance.getVariables().entrySet());
        List<Map.Entry<String, Integer>> iterationlevels = new ArrayList<Map.Entry<String, Integer>>(workFlow.getIterationLevels().entrySet());
        _instance.setContext(buildWorkflowContext(nodeInstances, exclusiveGroupInstances, variables, iterationlevels));

        KogitoProcessInstanceProtobuf.ProcessInstance piProtobuf = _instance.build();

        String format = (String) this.env.get(ProcessMarshallerFactory.FORMAT);
        if (format != null && "json".equals(format)) {
            os.write(JsonFormat.printer().print(piProtobuf).getBytes());
        } else {
            piProtobuf.writeTo(os);
        }
    }

    private KogitoTypesProtobuf.SLAContext buildSLAContext(int slaCompliance, Date slaDueDate, String slaTimerId) {
        KogitoTypesProtobuf.SLAContext.Builder slaContextBuilder = KogitoTypesProtobuf.SLAContext.newBuilder()
                .setSlaCompliance(slaCompliance);
        if (slaDueDate != null) {
            slaContextBuilder.setSlaDueDate(slaDueDate.getTime());
        }
        if (slaTimerId != null) {
            slaContextBuilder.setSlaTimerId(slaTimerId);
        }
        return slaContextBuilder.build();
    }

    private List<KogitoTypesProtobuf.SwimlaneContext> buildSwimlaneContexts(SwimlaneContextInstance swimlaneContextInstance) {
        if (swimlaneContextInstance == null) {
            return Collections.emptyList();
        }

        List<KogitoTypesProtobuf.SwimlaneContext> contexts = new ArrayList<>();

        Map<String, String> swimlaneActors = swimlaneContextInstance.getSwimlaneActors();
        for (Map.Entry<String, String> entry : swimlaneActors.entrySet()) {
            contexts.add(KogitoTypesProtobuf.SwimlaneContext.newBuilder()
                    .setSwimlane(entry.getKey())
                    .setActorId(entry.getValue())
                    .build());
        }
        return contexts;
    }

    private KogitoTypesProtobuf.WorkflowContext buildWorkflowContext(List<NodeInstance> nodeInstances, List<ContextInstance> exclusiveGroupInstances, List<Entry<String, Object>> variables,
            List<Entry<String, Integer>> iterationlevels) {

        KogitoTypesProtobuf.WorkflowContext.Builder _workflowContextBuilder = KogitoTypesProtobuf.WorkflowContext.newBuilder();
        _workflowContextBuilder.addAllNodeInstance(buildNodeInstances(nodeInstances));
        _workflowContextBuilder.addAllExclusiveGroup(buildGroups(exclusiveGroupInstances));
        _workflowContextBuilder.addAllVariable(buildVariables(variables));
        _workflowContextBuilder.addAllIterationLevels(buildIterationLevels(iterationlevels));
        return _workflowContextBuilder.build();

    }

    private List<org.kie.kogito.serialization.protobuf.KogitoTypesProtobuf.NodeInstance> buildNodeInstances(List<NodeInstance> nodeInstances) {
        Collections.sort(nodeInstances, new Comparator<NodeInstance>() {
            @Override
            public int compare(NodeInstance o1, NodeInstance o2) {
                return ((KogitoNodeInstance) o1).getStringId().compareTo(((KogitoNodeInstance) o2).getStringId());
            }
        });

        List<KogitoTypesProtobuf.NodeInstance> nodeInstancesProtobuf = new ArrayList<>();
        for (NodeInstance nodeInstance : nodeInstances) {
            KogitoTypesProtobuf.NodeInstance.Builder _node = KogitoTypesProtobuf.NodeInstance.newBuilder()
                    .setId(((KogitoNodeInstance) nodeInstance).getStringId())
                    .setNodeId(nodeInstance.getNodeId())
                    .setLevel(((org.jbpm.workflow.instance.NodeInstance) nodeInstance).getLevel())
                    .setTriggerDate(((org.jbpm.workflow.instance.NodeInstance) nodeInstance).getTriggerTime().getTime());

            _node.setSla(buildSLAContext(((org.jbpm.workflow.instance.NodeInstance) nodeInstance).getSlaCompliance(),
                    ((org.jbpm.workflow.instance.NodeInstance) nodeInstance).getSlaDueDate(),
                    ((org.jbpm.workflow.instance.NodeInstance) nodeInstance).getSlaTimerId()));

            _node.setContent(buildNodeInstanceContent(nodeInstance));

            nodeInstancesProtobuf.add(_node.build());
        }
        return nodeInstancesProtobuf;
    }

    private Any buildNodeInstanceContent(NodeInstance nodeInstance) {
        if (nodeInstance instanceof RuleSetNodeInstance) {
            return buildRuleSetNodeInstance((RuleSetNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof ForEachNodeInstance) {
            return buildForEachNodeInstance((ForEachNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof LambdaSubProcessNodeInstance) {
            return buildLambdaSubProcessNodeInstance((LambdaSubProcessNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof SubProcessNodeInstance) {
            return buildSubProcessNodeInstance((SubProcessNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof StateNodeInstance) {
            return buildStateNodeInstance((StateNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof JoinInstance) {
            return buildJoinInstance((JoinInstance) nodeInstance);
        } else if (nodeInstance instanceof TimerNodeInstance) {
            return buildTimerNodeInstance((TimerNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof EventNodeInstance) {
            return buildEventNodeInstance((EventNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof MilestoneNodeInstance) {
            return buildMilestoneNodeInstance((MilestoneNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof DynamicNodeInstance) {
            return buildDynamicNodeInstance((DynamicNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof EventSubProcessNodeInstance) {
            return buildEventSubProcessNodeInstance((EventSubProcessNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof CompositeContextNodeInstance) {
            return buildCompositeContextNodeInstance((CompositeContextNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof HumanTaskNodeInstance) {
            return buildHumanTaskNodeInstance((HumanTaskNodeInstance) nodeInstance);
        } else if (nodeInstance instanceof WorkItemNodeInstance) {
            return buildWorkItemNodeInstance((WorkItemNodeInstance) nodeInstance);
        } else {
            throw new IllegalArgumentException("Unknown node instance type: " + nodeInstance);
        }
    }

    private Any buildRuleSetNodeInstance(RuleSetNodeInstance nodeInstance) {
        RuleSetNodeInstanceContent.Builder _ruleSet = RuleSetNodeInstanceContent.newBuilder();
        _ruleSet.setRuleFlowGroup(nodeInstance.getRuleFlowGroup());
        _ruleSet.addAllTimerInstanceId(nodeInstance.getTimerInstances());

        Map<String, FactHandle> facts = ((RuleSetNodeInstance) nodeInstance).getFactHandles();
        if (facts != null && facts.size() > 0) {
            for (Map.Entry<String, FactHandle> entry : facts.entrySet()) {
                RuleSetNodeInstanceContent.TextMapEntry.Builder _textMapEntry = RuleSetNodeInstanceContent.TextMapEntry.newBuilder();
                _textMapEntry.setName(entry.getKey());
                _textMapEntry.setValue(entry.getValue().toExternalForm());
                _ruleSet.addMapEntry(_textMapEntry.build());
            }
        }

        return Any.pack(_ruleSet.build());
    }

    private Any buildForEachNodeInstance(ForEachNodeInstance nodeInstance) {
        ForEachNodeInstanceContent.Builder foreachBuilder = ForEachNodeInstanceContent.newBuilder();

        foreachBuilder.addAllTimerInstanceId(nodeInstance.getTimerInstances());
        List<NodeInstance> nodeInstances = nodeInstance.getNodeInstances().stream().filter(e -> e instanceof CompositeContextNodeInstance).collect(Collectors.toList());
        List<ContextInstance> exclusiveGroupInstances = nodeInstance.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance) nodeInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
        List<Map.Entry<String, Object>> variables = new ArrayList<Map.Entry<String, Object>>(variableScopeInstance.getVariables().entrySet());
        List<Map.Entry<String, Integer>> iterationlevels = new ArrayList<Map.Entry<String, Integer>>(nodeInstance.getIterationLevels().entrySet());
        foreachBuilder.setContext(buildWorkflowContext(nodeInstances, exclusiveGroupInstances, variables, iterationlevels));

        return Any.pack(foreachBuilder.build());
    }

    private Any buildLambdaSubProcessNodeInstance(LambdaSubProcessNodeInstance nodeInstance) {

        LambdaSubProcessNodeInstanceContent.Builder builder = LambdaSubProcessNodeInstanceContent.newBuilder();
        builder.setProcessInstanceId(nodeInstance.getProcessInstanceId());
        List<String> timerInstances = nodeInstance.getTimerInstances();
        if (timerInstances != null) {
            builder.addAllTimerInstanceId(timerInstances);
        }

        return Any.pack(builder.build());
    }

    private Any buildSubProcessNodeInstance(SubProcessNodeInstance nodeInstance) {
        SubProcessNodeInstanceContent.Builder builder = SubProcessNodeInstanceContent.newBuilder();
        builder.setProcessInstanceId(nodeInstance.getProcessInstanceId());
        List<String> timerInstances = nodeInstance.getTimerInstances();
        if (timerInstances != null) {
            builder.addAllTimerInstanceId(timerInstances);
        }
        return Any.pack(builder.build());
    }

    private Any buildStateNodeInstance(StateNodeInstance nodeInstance) {
        StateNodeInstanceContent.Builder builder = StateNodeInstanceContent.newBuilder();
        List<String> timerInstances = nodeInstance.getTimerInstances();
        if (timerInstances != null) {
            builder.addAllTimerInstanceId(timerInstances);
        }
        return Any.pack(builder.build());
    }

    private Any buildJoinInstance(JoinInstance nodeInstance) {
        JoinNodeInstanceContent.Builder _join = JoinNodeInstanceContent.newBuilder();
        Map<Long, Integer> triggers = nodeInstance.getTriggers();
        List<Long> keys = new ArrayList<Long>(triggers.keySet());
        Collections.sort(keys, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return o1.compareTo(o2);
            }
        });

        for (Long key : keys) {
            _join.addTrigger(JoinTrigger.newBuilder()
                    .setNodeId(key)
                    .setCounter(triggers.get(key))
                    .build());
        }

        return Any.pack(_join.build());
    }

    private Any buildTimerNodeInstance(TimerNodeInstance nodeInstance) {
        return Any.pack(TimerNodeInstanceContent.newBuilder().setTimerId(nodeInstance.getTimerId()).build());
    }

    private Any buildEventNodeInstance(EventNodeInstance nodeInstance) {
        return Any.pack(EventNodeInstanceContent.newBuilder().build());
    }

    private Any buildMilestoneNodeInstance(MilestoneNodeInstance nodeInstance) {
        MilestoneNodeInstanceContent.Builder builder = MilestoneNodeInstanceContent.newBuilder();

        List<String> timerInstances = ((MilestoneNodeInstance) nodeInstance).getTimerInstances();
        if (timerInstances != null) {
            builder.addAllTimerInstanceId(timerInstances);
        }
        return Any.pack(builder.build());
    }

    private Any buildDynamicNodeInstance(DynamicNodeInstance nodeInstance) {

        DynamicNodeInstanceContent.Builder builder = DynamicNodeInstanceContent.newBuilder();
        List<String> timerInstances = nodeInstance.getTimerInstances();
        if (timerInstances != null) {
            builder.addAllTimerInstanceId(timerInstances);
        }

        List<NodeInstance> nodeInstances = new ArrayList<>(nodeInstance.getNodeInstances());
        List<ContextInstance> exclusiveGroupInstances = nodeInstance.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance) nodeInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
        List<Map.Entry<String, Object>> variables = new ArrayList<Map.Entry<String, Object>>(variableScopeInstance.getVariables().entrySet());
        List<Map.Entry<String, Integer>> iterationlevels = new ArrayList<Map.Entry<String, Integer>>(nodeInstance.getIterationLevels().entrySet());
        builder.setContext(buildWorkflowContext(nodeInstances, exclusiveGroupInstances, variables, iterationlevels));

        return Any.pack(builder.build());
    }

    private Any buildEventSubProcessNodeInstance(EventSubProcessNodeInstance nodeInstance) {

        EventSubProcessNodeInstanceContent.Builder builder = EventSubProcessNodeInstanceContent.newBuilder();
        List<String> timerInstances = nodeInstance.getTimerInstances();
        if (timerInstances != null) {
            builder.addAllTimerInstanceId(timerInstances);
        }

        List<NodeInstance> nodeInstances = new ArrayList<>(nodeInstance.getNodeInstances());
        List<ContextInstance> exclusiveGroupInstances = nodeInstance.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance) nodeInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
        List<Map.Entry<String, Object>> variables = new ArrayList<Map.Entry<String, Object>>(variableScopeInstance.getVariables().entrySet());
        List<Map.Entry<String, Integer>> iterationlevels = new ArrayList<Map.Entry<String, Integer>>(nodeInstance.getIterationLevels().entrySet());
        builder.setContext(buildWorkflowContext(nodeInstances, exclusiveGroupInstances, variables, iterationlevels));

        return Any.pack(builder.build());
    }

    private Any buildCompositeContextNodeInstance(CompositeContextNodeInstance nodeInstance) {

        CompositeContextNodeInstanceContent.Builder builder = CompositeContextNodeInstanceContent.newBuilder();
        List<String> timerInstances = nodeInstance.getTimerInstances();
        if (timerInstances != null) {
            builder.addAllTimerInstanceId(timerInstances);
        }

        List<NodeInstance> nodeInstances = new ArrayList<>(nodeInstance.getNodeInstances());
        List<ContextInstance> exclusiveGroupInstances = nodeInstance.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance) nodeInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
        List<Map.Entry<String, Object>> variables = new ArrayList<Map.Entry<String, Object>>(variableScopeInstance.getVariables().entrySet());
        List<Map.Entry<String, Integer>> iterationlevels = new ArrayList<Map.Entry<String, Integer>>(nodeInstance.getIterationLevels().entrySet());
        builder.setContext(buildWorkflowContext(nodeInstances, exclusiveGroupInstances, variables, iterationlevels));

        return Any.pack(builder.build());
    }

    private Any buildWorkItemNodeInstance(WorkItemNodeInstance nodeInstance) {
        return Any.pack(buildWorkItemNodeInstanceBuilder(nodeInstance).build());
    }

    private WorkItemNodeInstanceContent.Builder buildWorkItemNodeInstanceBuilder(WorkItemNodeInstance nodeInstance) {
        WorkItemNodeInstanceContent.Builder builder = WorkItemNodeInstanceContent.newBuilder();

        builder.setWorkItemId(nodeInstance.getWorkItemId());
        List<String> timerInstances = nodeInstance.getTimerInstances();
        if (timerInstances != null) {
            builder.addAllTimerInstanceId(timerInstances);
        }
        if (nodeInstance.getExceptionHandlingProcessInstanceId() != null) {
            builder.setErrorHandlingProcessInstanceId(nodeInstance.getExceptionHandlingProcessInstanceId());
        }
        KogitoWorkItem workItem = nodeInstance.getWorkItem();

        builder.setName(workItem.getName())
                .setState(workItem.getState())
                .setPhaseId(workItem.getPhaseId())
                .setPhaseStatus(workItem.getPhaseStatus())
                .setStartDate(workItem.getStartDate().getTime())
                .setCompleteDate(workItem.getCompleteDate().getTime())
                .addAllVariable(buildVariables(new ArrayList<>(workItem.getParameters().entrySet())));

        return builder;
    }

    private Any buildHumanTaskNodeInstance(HumanTaskNodeInstance nodeInstance) {
        WorkItemNodeInstanceContent.Builder builder = buildWorkItemNodeInstanceBuilder(nodeInstance);
        builder.setWorkItemData(Any.pack(buildHumanTaskWorkItemData((HumanTaskWorkItem) nodeInstance.getWorkItem())));
        return Any.pack(builder.build());
    }

    private List<KogitoTypesProtobuf.NodeInstanceGroup> buildGroups(List<ContextInstance> exclusiveGroupInstances) {
        if (exclusiveGroupInstances == null) {
            return Collections.emptyList();
        }

        List<KogitoTypesProtobuf.NodeInstanceGroup> groupProtobuf = new ArrayList<>();
        for (ContextInstance contextInstance : exclusiveGroupInstances) {
            KogitoTypesProtobuf.NodeInstanceGroup.Builder _exclusive = KogitoTypesProtobuf.NodeInstanceGroup.newBuilder();
            ExclusiveGroupInstance exclusiveGroupInstance = (ExclusiveGroupInstance) contextInstance;
            Collection<KogitoNodeInstance> groupNodeInstances = exclusiveGroupInstance.getNodeInstances();
            for (KogitoNodeInstance nodeInstance : groupNodeInstances) {
                _exclusive.addGroupNodeInstanceId(nodeInstance.getStringId());
            }
            groupProtobuf.add(_exclusive.build());
        }

        return groupProtobuf;
    }

    private List<KogitoTypesProtobuf.Variable> buildVariables(List<Map.Entry<String, Object>> variables) {
        Collections.sort(variables, new Comparator<Map.Entry<String, Object>>() {
            @Override
            public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        List<KogitoTypesProtobuf.Variable> variablesProtobuf = new ArrayList<>();
        for (Map.Entry<String, Object> entry : variables) {
            KogitoTypesProtobuf.Variable.Builder variableBuilder = KogitoTypesProtobuf.Variable.newBuilder();
            variableBuilder.setName(entry.getKey());
            if (entry.getValue() != null) {
                variableBuilder.setDataType(entry.getValue().getClass().getName()).setValue(buildByteString(entry.getValue()));
            }
            variablesProtobuf.add(variableBuilder.build());
        }
        return variablesProtobuf;
    }

    private List<KogitoTypesProtobuf.IterationLevel> buildIterationLevels(List<Entry<String, Integer>> iterationlevels) {
        Collections.sort(iterationlevels, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        List<KogitoTypesProtobuf.IterationLevel> levelsProtobuf = new ArrayList<>();
        for (Map.Entry<String, Integer> level : iterationlevels) {
            if (level.getValue() != null) {
                KogitoTypesProtobuf.IterationLevel levelProtobuf = KogitoTypesProtobuf.IterationLevel.newBuilder()
                        .setId(level.getKey())
                        .setLevel(level.getValue())
                        .build();
                levelsProtobuf.add(levelProtobuf);
            }
        }
        return levelsProtobuf;
    }

    private ByteString buildByteString(Object value) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(stream)) {
            out.writeObject(value);
            return ByteString.copyFrom(stream.toByteArray());
        } catch (IOException e) {
            return ByteString.EMPTY;
        }
    }

    private HumanTaskWorkItemData buildHumanTaskWorkItemData(HumanTaskWorkItem workItem) {
        HumanTaskWorkItemData.Builder builder = HumanTaskWorkItemData.newBuilder()
                .setTaskName(workItem.getTaskName())
                .setTaskDescription(workItem.getTaskDescription())
                .setTaskPriority(workItem.getTaskPriority())
                .setActualOwner(workItem.getActualOwner())
                .setTaskReferenceName(workItem.getReferenceName());

        if (workItem.getPotentialUsers() != null) {
            builder.addAllPotUsers(workItem.getPotentialUsers());
        }

        if (workItem.getPotentialGroups() != null) {
            builder.addAllPotGroups(workItem.getPotentialGroups());
        }

        if (workItem.getExcludedUsers() != null) {
            builder.addAllExcludedUsers(workItem.getExcludedUsers());
        }

        if (workItem.getAdminUsers() != null) {
            builder.addAllAdminUsers(workItem.getAdminUsers());
        }

        if (workItem.getAdminGroups() != null) {
            builder.addAllAdminGroups(workItem.getAdminGroups());
        }

        if (workItem.getComments() != null) {
            builder.addAllComments(buildComments(workItem.getComments().values()));
        }

        if (workItem.getAttachments() != null) {
            builder.addAllAttachments(buildAttachments(workItem.getAttachments().values()));
        }

        return builder.build();
    }

    private List<KogitoWorkItemsProtobuf.Comment> buildComments(Iterable<Comment> comments) {
        List<KogitoWorkItemsProtobuf.Comment> commentsProtobuf = new ArrayList<>();
        for (Comment comment : comments) {
            KogitoWorkItemsProtobuf.Comment workItemComment = KogitoWorkItemsProtobuf.Comment.newBuilder()
                    .setId(comment.getId().toString())
                    .setContent(comment.getContent())
                    .setUpdatedBy(comment.getUpdatedBy())
                    .setUpdatedAt(comment.getUpdatedAt().getTime())
                    .build();
            commentsProtobuf.add(workItemComment);
        }
        return commentsProtobuf;
    }

    private List<KogitoWorkItemsProtobuf.Attachment> buildAttachments(Iterable<Attachment> attachments) {
        List<KogitoWorkItemsProtobuf.Attachment> attachmentProtobuf = new ArrayList<>();
        for (Attachment attachment : attachments) {
            KogitoWorkItemsProtobuf.Attachment workItemAttachment = KogitoWorkItemsProtobuf.Attachment.newBuilder()
                    .setId(attachment.getId().toString()).setContent(attachment
                            .getContent().toString())
                    .setUpdatedBy(attachment.getUpdatedBy()).setUpdatedAt(attachment.getUpdatedAt().getTime())
                    .setName(attachment.getName())
                    .build();
            attachmentProtobuf.add(workItemAttachment);
        }
        return attachmentProtobuf;
    }

}
