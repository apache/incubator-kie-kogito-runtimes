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
package org.kie.kogito.event.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEvent;
import org.kie.api.event.process.ProcessNodeEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.kogito.Addons;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventBatch;
import org.kie.kogito.event.process.AttachmentEventBody;
import org.kie.kogito.event.process.CommentEventBody;
import org.kie.kogito.event.process.MilestoneEventBody;
import org.kie.kogito.event.process.NodeInstanceDataEvent;
import org.kie.kogito.event.process.NodeInstanceEventBody;
import org.kie.kogito.event.process.ProcessErrorEventBody;
import org.kie.kogito.event.process.ProcessInstanceDataEvent;
import org.kie.kogito.event.process.ProcessInstanceEventBody;
import org.kie.kogito.event.process.UserTaskDeadlineDataEvent;
import org.kie.kogito.event.process.UserTaskDeadlineEventBody;
import org.kie.kogito.event.process.UserTaskInstanceDataEvent;
import org.kie.kogito.event.process.UserTaskInstanceEventBody;
import org.kie.kogito.event.process.VariableInstanceDataEvent;
import org.kie.kogito.event.process.VariableInstanceEventBody;
import org.kie.kogito.internal.process.event.HumanTaskDeadlineEvent;
import org.kie.kogito.internal.process.event.KogitoProcessVariableChangedEvent;
import org.kie.kogito.internal.process.event.ProcessWorkItemTransitionEvent;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcessInstance;
import org.kie.kogito.process.workitem.Attachment;
import org.kie.kogito.process.workitem.Comment;
import org.kie.kogito.process.workitem.HumanTaskWorkItem;

import static java.util.stream.Collectors.toList;

public class ProcessInstanceEventBatch implements EventBatch {

    private final String service;
    private Addons addons;
    Collection<DataEvent<?>> processedEvents;

    public ProcessInstanceEventBatch(String service, Addons addons) {
        this.service = service;
        this.addons = addons;
        this.processedEvents = new ArrayList<>();
    }

    @Override
    public void append(Object rawEvent) {
        if (rawEvent instanceof ProcessEvent) {
            addDataEvent((ProcessEvent) rawEvent);
        }
    }

    @Override
    public Collection<DataEvent<?>> events() {
        return processedEvents;
    }

    private void addDataEvent(ProcessEvent event) {
        if (event instanceof ProcessStartedEvent) {
            ProcessInstanceEventBody pi = handleProcessStartedEvent((ProcessStartedEvent) event);
            processedEvents.add(new ProcessInstanceDataEvent(extractRuntimeSource(pi.metaData()), toAddonsString(), pi.metaData(), pi));
        } else if (event instanceof ProcessCompletedEvent) {
            ProcessInstanceEventBody pi = handleProcessCompletedEvent((ProcessCompletedEvent) event);
            processedEvents.add(new ProcessInstanceDataEvent(extractRuntimeSource(pi.metaData()), toAddonsString(), pi.metaData(), pi));
        } else if (event instanceof ProcessNodeTriggeredEvent) {
            NodeInstanceEventBody ni = handleProcessNodeTriggeredEvent((ProcessNodeTriggeredEvent) event);
            processedEvents.add(new NodeInstanceDataEvent((String) ni.getData().get(ProcessInstanceEventBody.PROCESS_ID_META_DATA), toAddonsString(), ni.getData(), ni));
        } else if (event instanceof ProcessNodeLeftEvent) {
            NodeInstanceEventBody ni = handleProcessNodeLeftEvent((ProcessNodeLeftEvent) event);
            processedEvents.add(new NodeInstanceDataEvent((String) ni.getData().get(ProcessInstanceEventBody.PROCESS_ID_META_DATA), toAddonsString(), ni.getData(), ni));
        } else if (event instanceof ProcessWorkItemTransitionEvent) {
            Optional<UserTaskInstanceEventBody> body = handleProcessWorkItemTransitionEvent((ProcessWorkItemTransitionEvent) event);
            body.ifPresent(ht -> {
                processedEvents.add(new UserTaskInstanceDataEvent(extractRuntimeSource(ht.metaData()), toAddonsString(), ht.metaData(), ht));
            });
        } else if (event instanceof ProcessVariableChangedEvent) {
            Optional<VariableInstanceEventBody> body = handleProcessVariableChangedEvent((KogitoProcessVariableChangedEvent) event);
            body.ifPresent(var -> {
                processedEvents.add(new VariableInstanceDataEvent(extractRuntimeSource(var.metaData()), toAddonsString(), var.metaData(), var));
            });
        } else if (event instanceof HumanTaskDeadlineEvent) {
            UserTaskDeadlineEventBody body = buildUserTaskDeadlineEvent((HumanTaskDeadlineEvent) event);
            processedEvents.add(new UserTaskDeadlineDataEvent("UserTaskDeadline" + ((HumanTaskDeadlineEvent) event).getType(), buildSource(body.getProcessId()), toAddonsString(), body, body.getId(),
                    body.getRootProcessInstanceId(), body.getProcessId(), body.getRootProcessId()));
        }
    }

    private String toAddonsString() {
        return addons != null ? addons.toString() : null;
    }

    private Map<String, String> extractMetadata(KogitoWorkflowProcessInstance pi) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(ProcessInstanceEventBody.ID_META_DATA, pi.getId());
        metadata.put(ProcessInstanceEventBody.VERSION_META_DATA, pi.getProcess().getVersion());
        metadata.put(ProcessInstanceEventBody.PARENT_ID_META_DATA, pi.getParentProcessInstanceId());
        metadata.put(ProcessInstanceEventBody.ROOT_ID_META_DATA, pi.getRootProcessInstanceId());
        metadata.put(ProcessInstanceEventBody.PROCESS_ID_META_DATA, pi.getProcessId());
        metadata.put(ProcessInstanceEventBody.PROCESS_TYPE_META_DATA, pi.getProcess().getType());
        metadata.put(ProcessInstanceEventBody.ROOT_PROCESS_ID_META_DATA, pi.getRootProcessId());
        metadata.put(ProcessInstanceEventBody.STATE_META_DATA, String.valueOf(pi.getState()));

        return metadata;
    }

    private UserTaskDeadlineEventBody buildUserTaskDeadlineEvent(HumanTaskDeadlineEvent event) {

        HumanTaskWorkItem workItem = event.getWorkItem();
        KogitoWorkflowProcessInstance pi = (KogitoWorkflowProcessInstance) event.getProcessInstance();
        UserTaskDeadlineEventBody body = UserTaskDeadlineEventBody.create(workItem.getStringId(), event
                .getNotification())
                .state(workItem.getPhaseStatus())
                .taskName(workItem.getTaskName())
                .taskDescription(workItem.getTaskDescription())
                .taskPriority(workItem.getTaskPriority())
                .referenceName(workItem.getReferenceName())
                .actualOwner(workItem.getActualOwner())
                .startDate(workItem.getStartDate())
                .processInstanceId(pi.getStringId())
                .rootProcessInstanceId(pi.getRootProcessInstanceId())
                .processId(pi.getProcessId())
                .rootProcessId(pi.getRootProcessId())
                .inputs(workItem.getParameters())
                .outputs(workItem.getResults()).build();
        return body;
    }

    protected ProcessInstanceEventBody handleProcessStartedEvent(ProcessStartedEvent event) {
        KogitoWorkflowProcessInstance pi = (KogitoWorkflowProcessInstance) event.getProcessInstance();

        ProcessInstanceEventBody.Builder eventBuilder = ProcessInstanceEventBody.create()
                .id(pi.getStringId())
                .version(pi.getProcess().getVersion())
                .parentInstanceId(pi.getParentProcessInstanceId())
                .rootInstanceId(pi.getRootProcessInstanceId())
                .processId(pi.getProcessId())
                .rootProcessId(pi.getRootProcessId())
                .processName(pi.getProcessName())
                .startDate(pi.getStartDate())
                .endDate(pi.getEndDate())
                .state(pi.getState())
                .businessKey(pi.getCorrelationKey())
                .variables(pi.getVariables())
                .milestones(createMilestones(pi));

        if (pi.getState() == KogitoProcessInstance.STATE_ERROR) {
            eventBuilder.error(ProcessErrorEventBody.create()
                    .nodeDefinitionId(pi.getNodeIdInError())
                    .errorMessage(pi.getErrorMessage())
                    .build());
        }

        String securityRoles = (String) pi.getProcess().getMetaData().get("securityRoles");
        if (securityRoles != null) {
            eventBuilder.roles(securityRoles.split(","));
        }

        return eventBuilder.build();
    }

    protected ProcessInstanceEventBody handleProcessCompletedEvent(ProcessCompletedEvent event) {
        KogitoWorkflowProcessInstance pi = (KogitoWorkflowProcessInstance) event.getProcessInstance();

        ProcessInstanceEventBody.Builder eventBuilder = ProcessInstanceEventBody.create()
                .id(pi.getStringId())
                .version(pi.getProcess().getVersion())
                .parentInstanceId(pi.getParentProcessInstanceId())
                .rootInstanceId(pi.getRootProcessInstanceId())
                .processId(pi.getProcessId())
                .rootProcessId(pi.getRootProcessId())
                .processName(pi.getProcessName())
                .startDate(((KogitoWorkflowProcessInstance) event.getProcessInstance()).getEndDate())
                .endDate(pi.getEndDate())
                .state(pi.getState())
                .businessKey(pi.getCorrelationKey())
                .variables(pi.getVariables())
                .milestones(createMilestones(pi));

        if (pi.getState() == KogitoProcessInstance.STATE_ERROR) {
            eventBuilder.error(ProcessErrorEventBody.create()
                    .nodeDefinitionId(pi.getNodeIdInError())
                    .errorMessage(pi.getErrorMessage())
                    .build());
        }

        String securityRoles = (String) pi.getProcess().getMetaData().get("securityRoles");
        if (securityRoles != null) {
            eventBuilder.roles(securityRoles.split(","));
        }

        return eventBuilder.build();
    }

    protected NodeInstanceEventBody handleProcessNodeTriggeredEvent(ProcessNodeTriggeredEvent event) {
        return create(event);
    }

    protected NodeInstanceEventBody handleProcessNodeLeftEvent(ProcessNodeLeftEvent event) {
        return create(event);
    }

    protected Optional<UserTaskInstanceEventBody> handleProcessWorkItemTransitionEvent(ProcessWorkItemTransitionEvent workItemTransitionEvent) {
        KogitoWorkItem workItem = workItemTransitionEvent.getWorkItem();
        if (workItem instanceof HumanTaskWorkItem) {
            return Optional.of(createUserTask(workItemTransitionEvent));
        }
        return Optional.empty();
    }

    protected Optional<VariableInstanceEventBody> handleProcessVariableChangedEvent(KogitoProcessVariableChangedEvent variableChangedEvent) {
        if (!variableChangedEvent.hasTag("internal")) {
            return Optional.of(create(variableChangedEvent));
        }
        return Optional.empty();
    }

    protected UserTaskInstanceEventBody createUserTask(ProcessWorkItemTransitionEvent workItemTransitionEvent) {
        KogitoWorkflowProcessInstance pi = (KogitoWorkflowProcessInstance) workItemTransitionEvent.getProcessInstance();
        HumanTaskWorkItem workItem = (HumanTaskWorkItem) workItemTransitionEvent.getWorkItem();
        return UserTaskInstanceEventBody.create()
                .id(workItem.getStringId())
                .state(workItem.getPhaseStatus())
                .taskName(workItem.getTaskName())
                .taskDescription(workItem.getTaskDescription())
                .taskPriority(workItem.getTaskPriority())
                .referenceName(workItem.getReferenceName())
                .actualOwner(workItem.getActualOwner())
                .startDate(workItem.getStartDate())
                .completeDate(workItem.getCompleteDate())
                .adminGroups(workItem.getAdminGroups())
                .adminUsers(workItem.getAdminUsers())
                .excludedUsers(workItem.getExcludedUsers())
                .potentialGroups(workItem.getPotentialGroups())
                .potentialUsers(workItem.getPotentialUsers())
                .processInstanceId(pi.getStringId())
                .processInstanceVersion(pi.getProcess().getVersion())
                .rootProcessInstanceId(pi.getRootProcessInstanceId())
                .processId(pi.getProcessId())
                .rootProcessId(pi.getRootProcessId())
                .inputs(workItem.getParameters())
                .outputs(workItem.getResults())
                .comments(workItem.getComments().values().stream().map(createComment()).collect(toList()))
                .attachments(workItem.getAttachments().values().stream().map(createAttachment()).collect(toList()))
                .build();
    }

    protected Function<Comment, CommentEventBody> createComment() {
        return comment -> CommentEventBody.create()
                .id(comment.getId())
                .content(comment.getContent())
                .updatedAt(comment.getUpdatedAt())
                .updatedBy(comment.getUpdatedBy())
                .build();
    }

    protected Function<Attachment, AttachmentEventBody> createAttachment() {
        return attachment -> AttachmentEventBody.create()
                .id(attachment.getId())
                .name(attachment.getName())
                .content(attachment.getContent())
                .updatedAt(attachment.getUpdatedAt())
                .updatedBy(attachment.getUpdatedBy())
                .build();
    }

    protected Set<MilestoneEventBody> createMilestones(KogitoWorkflowProcessInstance pi) {
        if (pi.milestones() == null) {
            return Collections.emptySet();
        }

        return pi.milestones().stream()
                .map(m -> MilestoneEventBody.create().id(m.getId()).name(m.getName()).status(m.getStatus().name()).build())
                .collect(Collectors.toSet());
    }

    protected NodeInstanceEventBody create(ProcessNodeEvent event) {
        Map<String, String> metadata = extractMetadata((KogitoWorkflowProcessInstance) event.getProcessInstance());

        KogitoNodeInstance ni = (KogitoNodeInstance) event.getNodeInstance();

        return NodeInstanceEventBody.create()
                .id(ni.getStringId())
                .processInstanceId(event.getProcessInstance().getId())
                .nodeId(String.valueOf(ni.getNodeId()))
                .nodeDefinitionId(ni.getNodeDefinitionId())
                .nodeName(ni.getNodeName())
                .nodeType(ni.getNode().getClass().getSimpleName())
                .eventType(event instanceof ProcessNodeTriggeredEvent ? 1 : 2)
                .eventTime(new Date())
                .exitType(ni.isCancelled() ? ni.getCancelType().ordinal() : null)
                .data(ProcessInstanceEventBody.PROCESS_ID_META_DATA, metadata.get(ProcessInstanceEventBody.PROCESS_ID_META_DATA))
                .build();
    }

    protected VariableInstanceEventBody create(KogitoProcessVariableChangedEvent event) {
        KogitoProcessInstance pi = (KogitoProcessInstance) event.getProcessInstance();

        VariableInstanceEventBody.Builder eventBuilder = VariableInstanceEventBody.create()
                .changeDate(event.getEventDate())
                .processId(pi.getProcessId())
                .processInstanceId(pi.getStringId())
                .rootProcessId(pi.getRootProcessId())
                .rootProcessInstanceId(pi.getRootProcessInstanceId())
                .variableName(event.getVariableId())
                .variableValue(event.getNewValue())
                .variablePreviousValue(event.getOldValue());

        if (event.getNodeInstance() != null) {
            eventBuilder
                    .changedByNodeId(event.getNodeInstance().getNodeDefinitionId())
                    .changedByNodeName(event.getNodeInstance().getNodeName())
                    .changedByNodeType(event.getNodeInstance().getNode().getClass().getSimpleName());
        }

        return eventBuilder.build();
    }

    protected String extractRuntimeSource(Map<String, String> metadata) {
        return buildSource(metadata.get(ProcessInstanceEventBody.PROCESS_ID_META_DATA));
    }

    private String buildSource(String processId) {
        if (processId == null) {
            return null;
        } else {
            return service + "/" + (processId.contains(".") ? processId.substring(processId.lastIndexOf('.') + 1) : processId);
        }
    }
}
