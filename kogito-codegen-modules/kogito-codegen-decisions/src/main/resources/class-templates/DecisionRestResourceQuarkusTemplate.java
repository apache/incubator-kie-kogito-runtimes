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
package org.kie.dmn.kogito.quarkus.example;

import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.kogito.Application;
import org.kie.kogito.dmn.rest.DMNEvaluationErrorException;
import org.kie.kogito.dmn.rest.DMNJSONUtils;
import org.kie.kogito.dmn.rest.KogitoDMNResult;
import org.kie.kogito.dmn.util.StronglyTypedUtils;

@Path("/$nameURL$")
public class DMNRestResourceTemplate {

    Application application;

    private static final String KOGITO_DECISION_INFOWARN_HEADER = "X-Kogito-decision-messages";
    private static final String KOGITO_EXECUTION_ID_HEADER = "X-Kogito-execution-id";

    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
                                    .addSerializer(org.kie.dmn.feel.lang.types.impl.ComparablePeriod.class,
                                                   new org.kie.kogito.dmn.rest.DMNFEELComparablePeriodSerializer()))
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);

    @javax.ws.rs.core.Context
    private org.jboss.resteasy.spi.HttpResponse httpResponse;

    @POST
    @Path("/$dmnMethodUrl$")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @org.eclipse.microprofile.openapi.annotations.parameters.RequestBody(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json",schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/dmnDefinitions.json#/definitions/InputSet1")), description = "DMN input")
    @org.eclipse.microprofile.openapi.annotations.responses.APIResponse(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json", schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/dmnDefinitions.json#/definitions/OutputSet1")), description = "DMN output")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/dmnDefinitions.json#/definitions/InputSet1")), description = "DMN input")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/dmnDefinitions.json#/definitions/OutputSet1")), description = "DMN output")
    public Response dmn($inputType$ variables) {
        org.kie.kogito.decision.DecisionModel decision = application.get(org.kie.kogito.decision.DecisionModels.class).getDecisionModel("$modelNamespace$", "$modelName$");
        OutputSet outputSet = (OutputSet) StronglyTypedUtils.convertToOutputSet(variables, OutputSet.class);
        org.kie.dmn.api.core.DMNResult decisionResult = decision.evaluateAll(DMNJSONUtils.ctx(decision, $inputData$));
        enrichResponseHeaders(decisionResult);
        KogitoDMNResult result = new KogitoDMNResult("$modelNamespace$", "$modelName$", decisionResult);
        return $extractContextMethod$(result);
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public String dmn() throws java.io.IOException {
        return new String(org.drools.core.util.IoUtils.
                          readBytesFromInputStream(this.getClass()
                                                   .getResourceAsStream(org.kie.dmn.feel.codegen.feel11.CodegenStringUtil.escapeIdentifier("$modelName$") + 
                                                                        ".dmn_nologic")));
    }

    private Response extractContextIfSucceded(KogitoDMNResult result){
        if (!result.hasErrors()) {
            return Response.ok(buildResponse(result.getDmnContext())).build();
        } else {
            return buildFailedEvaluationResponse(result);
        }
    }

    private Response buildFailedEvaluationResponse(KogitoDMNResult result){
        return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
    }

    private Response extractStronglyTypedContextIfSucceded(KogitoDMNResult result) {
        if (!result.hasErrors()) {
            return Response.ok(buildResponse((OutputSet)StronglyTypedUtils.extractOutputSet(result, OutputSet.class))).build();
        } else {
            return buildFailedEvaluationResponse(result);
        }
    }

    private Response extractSingletonDSIfSucceded(KogitoDMNResult result) {
        if (!result.hasErrors()) {
            return Response.ok(buildResponse(result.getDecisionResults().get(0).getResult())).build();
        } else {
            return buildFailedEvaluationResponse(result);
        }
    }

    private Response buildDMNResultResponse(KogitoDMNResult result) {
        if (!result.hasErrors()) {
            return Response.ok(buildResponse(result)).build();
        } else {
            return buildFailedEvaluationResponse(result);
        }
    }

    private String buildResponse(Object o){
        try{
            return objectMapper.writeValueAsString(o);
        }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void enrichResponseHeaders(org.kie.dmn.api.core.DMNResult result) {
        if (!result.hasErrors() && !result.getMessages().isEmpty()) {
            String infoWarns = result.getMessages().stream().map(m -> m.getLevel() + " " + m.getMessage()).collect(java.util.stream.Collectors.joining(", "));
            httpResponse.getOutputHeaders().add(KOGITO_DECISION_INFOWARN_HEADER, infoWarns);
        }

        org.kie.kogito.decision.DecisionExecutionIdUtils.getOptional(result.getContext())
                .ifPresent(executionId ->
                        httpResponse.getOutputHeaders().add(KOGITO_EXECUTION_ID_HEADER, executionId)
                );
    }
}