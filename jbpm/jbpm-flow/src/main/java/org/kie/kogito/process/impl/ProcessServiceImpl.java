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
package org.kie.kogito.process.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jbpm.process.instance.impl.humantask.HumanTaskHelper;
import org.jbpm.process.instance.impl.humantask.HumanTaskTransition;
import org.jbpm.util.JsonSchemaUtil;
import org.kie.kogito.*;
import org.kie.kogito.incubation.process.workitem.impl.WorkItemServiceImpl;
import org.kie.kogito.incubation.processes.services.workitems.WorkItemService;
import org.kie.kogito.incubation.processes.workitem.WorkItemId;
import org.kie.kogito.process.*;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.workitem.*;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;

public class ProcessServiceImpl implements ProcessService {

    private final Application application;
    private final WorkItemService workItemService;

    public ProcessServiceImpl(Application application) {
        this.application = application;
        this.workItemService = new WorkItemServiceImpl(application);
    }

    @Override
    public <T extends Model> ProcessInstance<T> createProcessInstance(Process<T> process, String businessKey,
            T model,
            String startFromNodeId) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<T> pi = process.createInstance(businessKey, model);
            if (startFromNodeId != null) {
                pi.startFrom(startFromNodeId);
            } else {
                pi.start();
            }
            return pi;
        });
    }

    @Override
    public <T extends MappableToModel<R>, R> List<R> getProcessInstanceOutput(Process<T> process) {
        return process.instances().values().stream()
                .map(ProcessInstance::variables)
                .map(MappableToModel::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public <T extends MappableToModel<R>, R> Optional<R> findById(Process<T> process, String id) {
        return process.instances()
                .findById(id, ProcessInstanceReadMode.READ_ONLY)
                .map(ProcessInstance::variables)
                .map(MappableToModel::toModel);
    }

    @Override
    public <T extends MappableToModel<R>, R> Optional<R> delete(Process<T> process, String id) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(),
                () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> {
                            pi.abort();
                            return pi;
                        })
                        .map(ProcessInstance::checkError)
                        .map(ProcessInstance::variables)
                        .map(MappableToModel::toModel));
    }

    @Override
    public <T extends MappableToModel<R>, R> Optional<R> update(Process<T> process, String id, T resource) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(),
                () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateVariables(resource))
                        .map(MappableToModel::toModel));
    }

    @Override
    public <T extends Model> Optional<List<WorkItem>> getTasks(Process<T> process, String id, String user, List<String> groups) {
        return process.instances()
                .findById(id, ProcessInstanceReadMode.READ_ONLY)
                .map(pi -> pi.workItems(Policies.of(user, groups)));
    }

    @Override
    public <T extends Model> Optional<WorkItem> signalTask(Process<T> process, String id, String taskNodeName, String taskName) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> process
                .instances()
                .findById(id)
                .map(pi -> {
                    pi.send(Sig.of(taskNodeName, Collections.emptyMap()));
                    return pi;
                })
                .map(pi -> getTaskByName(pi, taskName).orElse(null)));
    }

    @Override
    public <T extends Model> Optional<WorkItem> getTaskByName(ProcessInstance<T> pi, String taskName) {
        return pi
                .workItems()
                .stream()
                .filter(wi -> wi.getName().equals(taskName))
                .findFirst();
    }

    @Override
    public <T extends MappableToModel<R>, R> Optional<R> completeTask(Process<T> process,
            String id,
            String taskId,
            String phase,
            String user,
            List<String> groups,
            MapOutput taskModel) {
        WorkItemId workItemId = new WorkItemId(process.id(), id, taskId);
        HumanTaskTransition transition = HumanTaskTransition.withModel(phase, taskModel, Policies.of(user, groups));
        return workItemService.complete(workItemId, transition)
                .map(m -> ((T) m).toModel()); // cast to be removed https://issues.redhat.com/browse/KOGITO-5448
    }

    @Override
    public <T extends Model, R extends MapOutput> Optional<R> saveTask(Process<T> process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            MapOutput model,
            Function<Map<String, Object>, R> mapper) {
        WorkItemId workItemId = new WorkItemId(process.id(), id, taskId);
        return workItemService.save(workItemId, model, Policies.of(user, groups))
                .map(m -> mapper.apply(m.toMap()));

    }

    @Override
    public <T extends MappableToModel<R>, R> Optional<R> taskTransition(
            Process<T> process,
            String id,
            String taskId,
            String phase,
            String user,
            List<String> groups,
            MapOutput model) {
        WorkItemId workItemId = new WorkItemId(process.id(), id, taskId);
        HumanTaskTransition transition = HumanTaskTransition.withModel(phase, model, Policies.of(user, groups));
        return workItemService.transition(workItemId, transition)
                .map(m -> ((T) m).toModel()); // cast to be removed https://issues.redhat.com/browse/KOGITO-5448
    }

    @Override
    public <T extends MappableToModel<?>, R> Optional<R> getTask(Process<T> process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            Function<WorkItem, R> mapper) {
        WorkItemId workItemId = new WorkItemId(process.id(), id, taskId);
        return workItemService.get(workItemId, Policies.of(user, groups))
                .map(mapper);
    }

    @Override
    public <T extends MappableToModel<R>, R> Optional<R> abortTask(Process<T> process,
            String instanceId,
            String taskId,
            String phase,
            String user,
            List<String> groups) {
        WorkItemId workItemId = new WorkItemId(process.id(), instanceId, taskId);
        HumanTaskTransition transition = HumanTaskTransition.withoutModel(phase, Policies.of(user, groups));
        return workItemService.abort(workItemId, transition)
                .map(m -> ((T) m).toModel()); // cast to be removed https://issues.redhat.com/browse/KOGITO-5448
    }

    @Override
    public <T extends Model> Optional<Comment> addComment(Process<T> process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            String commentInfo) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.addComment(wi, commentInfo, user),
                                Policies.of(user, groups))));
    }

    @Override
    public <T extends Model> Optional<Comment> updateComment(Process<T> process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups,
            String commentInfo) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.updateComment(wi, commentId, commentInfo, user),
                                Policies.of(user, groups))));
    }

    @Override
    public <T extends Model> Optional<Boolean> deleteComment(Process<T> process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.deleteComment(wi, commentId, user),
                                Policies.of(user, groups))));
    }

    @Override
    public <T extends Model> Optional<Attachment> addAttachment(Process<T> process,
            String id,
            String taskId,
            String user,
            List<String> groups,
            AttachmentInfo attachmentInfo) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.addAttachment(wi, attachmentInfo, user),
                                Policies.of(user, groups))));
    }

    @Override
    public <T extends Model> Optional<Attachment> updateAttachment(Process<T> process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups,
            AttachmentInfo attachment) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.updateAttachment(wi, attachmentId, attachment, user),
                                Policies.of(user, groups))));
    }

    @Override
    public <T extends Model> Optional<Boolean> deleteAttachment(Process<T> process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups) {
        return UnitOfWorkExecutor.executeInUnitOfWork(
                application.unitOfWorkManager(), () -> process
                        .instances()
                        .findById(id)
                        .map(pi -> pi.updateWorkItem(
                                taskId,
                                wi -> HumanTaskHelper.deleteAttachment(wi, attachmentId, user),
                                Policies.of(user, groups))));
    }

    @Override
    public <T extends Model> Optional<Attachment> getAttachment(Process<T> process,
            String id,
            String taskId,
            String attachmentId,
            String user,
            List<String> groups) {
        return process.instances().findById(id)
                .map(pi -> HumanTaskHelper.findTask(pi, taskId, Policies.of(user, groups)))
                .map(HumanTaskWorkItem::getAttachments)
                .map(attachments -> attachments.get(attachmentId));
    }

    @Override
    public <T extends Model> Optional<Collection<Attachment>> getAttachments(Process<T> process,
            String id,
            String taskId,
            String user,
            List<String> groups) {
        return process.instances().findById(id)
                .map(pi -> HumanTaskHelper.findTask(pi, taskId, Policies.of(user, groups)))
                .map(HumanTaskWorkItem::getAttachments)
                .map(Map::values);
    }

    @Override
    public <T extends Model> Optional<Comment> getComment(Process<T> process,
            String id,
            String taskId,
            String commentId,
            String user,
            List<String> groups) {
        return process.instances().findById(id)
                .map(pi -> HumanTaskHelper.findTask(pi, taskId, Policies.of(user, groups)))
                .map(HumanTaskWorkItem::getComments)
                .map(comments -> comments.get(commentId));
    }

    @Override
    public <T extends Model> Optional<Collection<Comment>> getComments(Process<T> process,
            String id,
            String taskId,
            String user,
            List<String> groups) {
        return process.instances().findById(id)
                .map(pi -> HumanTaskHelper.findTask(pi, taskId, Policies.of(user, groups)))
                .map(HumanTaskWorkItem::getComments)
                .map(Map::values);
    }

    @Override
    public <T extends MappableToModel<R>, R> Optional<R> signalProcessInstance(Process<T> process, String id, Object data, String signalName) {
        return UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(),
                () -> process.instances().findById(id)
                        .map(pi -> {
                            pi.send(Sig.of(signalName, data));
                            return pi.checkError().variables().toModel();
                        }));
    }

    //Schema
    @Override
    public <T extends Model> Map<String, Object> getSchemaAndPhases(Process<T> process,
            String id,
            String taskId,
            String taskName,
            String user,
            List<String> groups) {
        return JsonSchemaUtil.addPhases(
                process,
                application.config().get(ProcessConfig.class).workItemHandlers().forName("Human Task"),
                id,
                taskId,
                Policies.of(user, groups),
                JsonSchemaUtil.load(Thread.currentThread().getContextClassLoader(), process.id(), taskName));
    }
}
