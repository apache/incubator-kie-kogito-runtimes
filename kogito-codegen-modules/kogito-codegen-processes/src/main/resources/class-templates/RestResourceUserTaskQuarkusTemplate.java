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
package com.myspace.demo;

import java.util.List;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.impl.Sig;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;

public class $Type$Resource {

    @POST
    @Path("/{id}/$taskName$")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signal(@PathParam("id") final String id, @Context UriInfo uriInfo) {
        return processService.signalTask(process, id, "$taskNodeName$", "$taskName$")
                .map(task -> Response
                        .created(uriInfo.getAbsolutePathBuilder().path(task.getId()).build())
                        .entity(task.getResults())
                        .build())
                .orElseThrow(NotFoundException::new);
    }

    @POST
    @Path("/{id}/$taskName$/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public $Type$Output completeTask(@PathParam("id") final String id,
                                     @PathParam("taskId") final String taskId,
                                     @QueryParam("phase") @DefaultValue("complete") final String phase,
                                     @QueryParam("user") final String user,
                                     @QueryParam("group") final List<String> groups,
                                     final $TaskOutput$ model) {
        return processService.completeTask(process, id, taskId, phase, user, groups, model, $Type$Output.class)
                .orElseThrow(NotFoundException::new);
    }

    @PUT
    @Path("/{id}/$taskName$/{taskId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public $TaskOutput$ saveTask(@PathParam("id") final String id,
                                 @PathParam("taskId") final String taskId,
                                 @QueryParam("user") final String user,
                                 @QueryParam("group") final List<String> groups,
                                 final $TaskOutput$ model) {
        return processService.saveTask(process, id, taskId, user, groups, model, $TaskOutput$::fromMap)
                .orElseThrow(NotFoundException::new);
    }

    @POST
    @Path("/{id}/$taskName$/{taskId}/phases/{phase}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public $Type$Output taskTransition(
            @PathParam("id") final String id,
            @PathParam("taskId") final String taskId,
            @PathParam("phase") final String phase,
            @QueryParam("user") final String user,
            @QueryParam("group") final List<String> groups,
            final $TaskOutput$ model) {
        return processService.taskTransition(process, id, taskId, phase, user, groups, model, $Type$Output.class)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/{id}/$taskName$/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public $TaskModel$ getTask(@PathParam("id") String id,
                               @PathParam("taskId") String taskId,
                               @QueryParam("user") final String user,
                               @QueryParam("group") final List<String> groups) {
        return processService.getTask(process, id, taskId, user, groups, wi-> ($TaskModel$) $TaskModel$.from(wi))
                .orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("/{id}/$taskName$/{taskId}")
    @Produces(MediaType.APPLICATION_JSON)
    public $Type$Output abortTask(@PathParam("id") final String id,
                                  @PathParam("taskId") final String taskId,
                                  @QueryParam("phase") @DefaultValue("abort") final String phase,
                                  @QueryParam("user") final String user,
                                  @QueryParam("group") final List<String> groups) {
        return processService.abortTask(process, id, taskId, phase, user, groups, $Type$Output.class)
                .orElseThrow(() -> new NotFoundException());
    }

    @GET
    @Path("$taskName$/schema")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getSchema() {
        return JsonSchemaUtil.load(this.getClass().getClassLoader(), process.id(), "$taskName$");
    }

    @GET
    @Path("/{id}/$taskName$/{taskId}/schema")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getSchemaAndPhases(@PathParam("id") final String id,
                                                  @PathParam("taskId") final String taskId,
                                                  @QueryParam("user") final String user,
                                                  @QueryParam("group") final List<String> groups) {
        return processService.getSchemaAndPhases(process, id, taskId, user, groups);
    }

    @POST
    @Path("/{id}/$taskName$/{taskId}/comments")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addComment(@PathParam("id") final String id,
                               @PathParam("taskId") final String taskId,
                               @QueryParam("user") final String user,
                               @QueryParam("group") final List<String> groups,
                               String commentInfo,
                               @Context UriInfo uriInfo) {
        return processService.addComment(process, id, taskId, user, groups, commentInfo)
                .map(comment -> Response.created(uriInfo.getAbsolutePathBuilder().path(comment.getId().toString()).build())
                        .entity(comment).build())
                .orElseThrow(NotFoundException::new);
    }

    @PUT
    @Path("/{id}/$taskName$/{taskId}/comments/{commentId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Comment updateComment(@PathParam("id") final String id,
                                 @PathParam("taskId") final String taskId,
                                 @PathParam("commentId") final String commentId,
                                 @QueryParam("user") final String user,
                                 @QueryParam("group") final List<String> groups,
                                 String comment) {
        return processService.updateComment(process, id, taskId, commentId, user, groups, comment)
                .orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("/{id}/$taskName$/{taskId}/comments/{commentId}")
    public Response deleteComment(@PathParam("id") final String id,
                                  @PathParam("taskId") final String taskId,
                                  @PathParam("commentId") final String commentId,
                                  @QueryParam("user") final String user,
                                  @QueryParam("group") final List<String> groups) {
        return processService.deleteComment(process, id, taskId, commentId, user, groups)
                .map(removed -> (removed ? Response.ok() : Response.status(Status.NOT_FOUND)).build())
                .orElseThrow(NotFoundException::new);
    }

    @POST
    @Path("/{id}/$taskName$/{taskId}/attachments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addAttachment(@PathParam("id") final String id,
                                  @PathParam("taskId") final String taskId,
                                  @QueryParam("user") final String user,
                                  @QueryParam("group") final List<String> groups,
                                  AttachmentInfo attachmentInfo,
                                  @Context UriInfo uriInfo) {
        return processService.addAttachment(process, id, taskId, user, groups, attachmentInfo)
                .map(attachment -> Response
                        .created(uriInfo.getAbsolutePathBuilder().path(attachment.getId().toString()).build())
                        .entity(attachment).build())
                .orElseThrow(NotFoundException::new);
    }

    @PUT
    @Path("/{id}/$taskName$/{taskId}/attachments/{attachmentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Attachment updateAttachment(@PathParam("id") final String id,
                                       @PathParam("taskId") final String taskId,
                                       @PathParam("attachmentId") final String attachmentId,
                                       @QueryParam("user") final String user,
                                       @QueryParam("group") final List<String> groups,
                                       AttachmentInfo attachment) {
        return processService.updateAttachment(process, id, taskId, attachmentId, user, groups, attachment)
                .orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("/{id}/$taskName$/{taskId}/attachments/{attachmentId}")
    public Response deleteAttachment(@PathParam("id") final String id,
                                     @PathParam("taskId") final String taskId,
                                     @PathParam("attachmentId") final String attachmentId,
                                     @QueryParam("user") final String user,
                                     @QueryParam("group") final List<String> groups) {
        return processService.deleteAttachment(process, id, taskId, attachmentId, user, groups)
                .map(removed -> (removed ? Response.ok() : Response.status(Status.NOT_FOUND)).build())
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/{id}/$taskName$/{taskId}/attachments/{attachmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Attachment getAttachment(@PathParam("id") final String id,
                                    @PathParam("taskId") final String taskId,
                                    @PathParam("attachmentId") final String attachmentId,
                                    @QueryParam("user") final String user,
                                    @QueryParam("group") final List<String> groups) {
        return processService.getAttachment(process, id, taskId, attachmentId, user, groups)
                .orElseThrow(() -> new NotFoundException("Attachment " + attachmentId + " not found"));
    }

    @GET
    @Path("/{id}/$taskName$/{taskId}/attachments")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Attachment> getAttachments(@PathParam("id") final String id,
                                                 @PathParam("taskId") final String taskId,
                                                 @QueryParam("user") final String user,
                                                 @QueryParam("group") final List<String> groups) {
        return processService.getAttachments(process, id, taskId, user, groups)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("/{id}/$taskName$/{taskId}/comments/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Comment getComment(@PathParam("id") final String id,
                              @PathParam("taskId") final String taskId,
                              @PathParam("commentId") final String commentId,
                              @QueryParam("user") final String user,
                              @QueryParam("group") final List<String> groups) {
        return processService.getComment(process, id, taskId, commentId, user, groups)
                .orElseThrow(() -> new NotFoundException("Comment " + commentId + " not found"));
    }

    @GET
    @Path("/{id}/$taskName$/{taskId}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Comment> getComments(@PathParam("id") final String id,
                                           @PathParam("taskId") final String taskId,
                                           @QueryParam("user") final String user,
                                           @QueryParam("group") final List<String> groups) {
        return processService.getComments(process, id, taskId, user, groups)
                .orElseThrow(NotFoundException::new);
    }
}
