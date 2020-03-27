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

package org.kie.kogito.process.management;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.kie.kogito.Application;
import org.kie.kogito.process.Processes;

@Path("/management/processes/")
public class ProcessInstanceManagementResource extends BaseProcessInstanceManagementResource<Response> {

    //CDI
    public ProcessInstanceManagementResource() {
        this(null, null);
    }

    @Inject
    public ProcessInstanceManagementResource(Processes processes, Application application) {
        super(processes, application);
    }

    @Override
    protected  <R> Response buildOkResponse(R body) {
        return Response
                .status(Response.Status.OK)
                .entity(body)
                .build();
    }

    @Override
    protected Response badRequestResponse(String message) {
        return Response
                .status(Status.BAD_REQUEST)
                .entity(message)
                .build();
    }

    @Override
    protected Response notFoundResponse(String message) {
        return Response
                .status(Status.NOT_FOUND)
                .entity(message)
                .build();
    }

    @Override
    @GET
    @Path("{processId}/instances/{processInstanceId}/error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInstanceInError(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId) {
        return doGetInstanceInError(processId, processInstanceId);
    }

    @Override
    @GET
    @Path("{processId}/instances/{processInstanceId}/nodeInstances")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWorkItemsInProcessInstance(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId) {
        return doGetWorkItemsInProcessInstance(processId, processInstanceId);
    }

    @Override
    @POST
    @Path("{processId}/instances/{processInstanceId}/retrigger")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retriggerInstanceInError(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId) {
        return doRetriggerInstanceInError(processId, processInstanceId);
    }

    @Override
    @POST
    @Path("{processId}/instances/{processInstanceId}/skip")
    @Produces(MediaType.APPLICATION_JSON)
    public Response skipInstanceInError(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId) {
        return doSkipInstanceInError(processId, processInstanceId);
    }

    @Override
    @POST
    @Path("{processId}/instances/{processInstanceId}/nodes/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerNodeInstanceId(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId, @PathParam("nodeId") String nodeId) {
        return doTriggerNodeInstanceId(processId, processInstanceId, nodeId);
    }

    @Override
    @POST
    @Path("{processId}/instances/{processInstanceId}/nodeInstances/{nodeInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retriggerNodeInstanceId(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId, @PathParam("nodeInstanceId") String nodeInstanceId) {
        return doRetriggerNodeInstanceId(processId, processInstanceId, nodeInstanceId);
    }

    @Override
    @DELETE
    @Path("{processId}/instances/{processInstanceId}/nodeInstances/{nodeInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelNodeInstanceId(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId, @PathParam("nodeInstanceId") String nodeInstanceId) {
        return doCancelNodeInstanceId(processId, processInstanceId, nodeInstanceId);
    }

    @Override
    @DELETE
    @Path("{processId}/instances/{processInstanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelProcessInstanceId(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId) {
        return doCancelProcessInstanceId(processId, processInstanceId);
    }
}
