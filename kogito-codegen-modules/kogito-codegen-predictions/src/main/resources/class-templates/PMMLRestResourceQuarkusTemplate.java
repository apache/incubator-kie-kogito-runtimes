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
package org.kie.kogito.pmml.rest;

import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.kogito.Application;

@Path("/$nameURL$")
public class PMMLRestResourceTemplate extends org.kie.kogito.pmml.AbstractPMMLRestResource {

    final String MODEL_NAME;
    Application application;

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @org.eclipse.microprofile.openapi.annotations.parameters.RequestBody(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json",schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/InputSet")), description = "PMML input")
    @org.eclipse.microprofile.openapi.annotations.responses.APIResponse(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json", schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/ResultSet")), description = "PMML result")
    public Object result(java.util.Map<String, Object> variables) {
        return super.result(application, MODEL_NAME, variables);
    }

    @POST
    @Path("/descriptive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @org.eclipse.microprofile.openapi.annotations.parameters.RequestBody(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json",schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/InputSet")), description = "PMML input")
    @org.eclipse.microprofile.openapi.annotations.responses.APIResponse(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json", schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/OutputSet")), description = "PMML full output")
    public org.kie.api.pmml.PMML4Result descriptive(java.util.Map<String, Object> variables) {
        return super.descriptive(application, MODEL_NAME, variables);
    }

    @javax.ws.rs.ext.Provider
    public static class ErrorExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<java.lang.Exception> {

        public javax.ws.rs.core.Response toResponse(java.lang.Exception e) {
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(org.kie.kogito.pmml.AbstractPMMLRestResource.getJsonErrorMessage(e)).build();
        }
    }

}