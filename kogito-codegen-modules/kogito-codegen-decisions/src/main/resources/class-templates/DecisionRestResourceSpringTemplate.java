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

import javax.servlet.http.HttpServletResponse;

import java.util.stream.Collectors;

import org.kie.kogito.Application;
import org.kie.kogito.dmn.rest.DMNEvaluationErrorException;
import org.kie.kogito.dmn.rest.DMNJSONUtils;
import org.kie.kogito.dmn.rest.KogitoDMNResult;
import org.kie.kogito.dmn.util.StronglyTypedUtils;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/$nameURL$")
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

    @PostMapping(value = "$dmnMethodUrl$", produces = MediaType.APPLICATION_JSON_VALUE, consumes =
            MediaType.APPLICATION_JSON_VALUE)
    @org.eclipse.microprofile.openapi.annotations.parameters.RequestBody(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json",schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/dmnDefinitions.json#/definitions/InputSet1")), description = "DMN input")
    @org.eclipse.microprofile.openapi.annotations.responses.APIResponse(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json", schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/dmnDefinitions.json#/definitions/OutputSet1")), description = "DMN output")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/dmnDefinitions.json#/definitions/InputSet1")), description = "DMN input")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/dmnDefinitions.json#/definitions/OutputSet1")), description = "DMN output")
    public ResponseEntity<?> dmn(@RequestBody(required = false) $inputType$ variables,
                            HttpServletResponse httpResponse) {
        org.kie.kogito.decision.DecisionModel decision = application.get(org.kie.kogito.decision.DecisionModels.class).getDecisionModel("$modelNamespace$", "$modelName$");
        OutputSet outputSet = (OutputSet)StronglyTypedUtils.convertToOutputSet(variables, OutputSet.class);
        org.kie.dmn.api.core.DMNResult decisionResult = decision.evaluateAll(DMNJSONUtils.ctx(decision, $inputData$));
        enrichResponseHeaders(decisionResult, httpResponse);
        KogitoDMNResult result = new KogitoDMNResult("$modelNamespace$", "$modelName$", decisionResult);
        return $extractContextMethod$(result);
    }

    @GetMapping(produces = MediaType.APPLICATION_XML_VALUE)
    public String dmn() throws java.io.IOException {
        return new String(org.drools.core.util.IoUtils.
                          readBytesFromInputStream(this.getClass()
                                                   .getResourceAsStream(org.kie.dmn.feel.codegen.feel11.CodegenStringUtil.escapeIdentifier("$modelName$") + 
                                                                        ".dmn_nologic")));
    }

    private ResponseEntity buildFailedEvaluationResponse(KogitoDMNResult result){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    private ResponseEntity extractContextIfSucceded(KogitoDMNResult result){
        if (!result.hasErrors()) {
            return ResponseEntity.ok(buildResponse(result.getDmnContext()));
        } else {
            return buildFailedEvaluationResponse(result);
        }
    }

    private ResponseEntity extractStronglyTypedContextIfSucceded(KogitoDMNResult result) {
        if (!result.hasErrors()) {
            return ResponseEntity.build(buildResponse((OutputSet)StronglyTypedUtils.extractOutputSet(result, OutputSet.class)));
        } else {
            return buildFailedEvaluationResponse(result);
        }
    }

    private ResponseEntity extractSingletonDSIfSucceded(KogitoDMNResult result) {
        if (!result.hasErrors()) {
            return ResponseEntity.ok(buildResponse(result.getDecisionResults().get(0).getResult()));
        } else {
            return buildFailedEvaluationResponse(result);
        }
    }

    private ResponseEntity buildDMNResultResponse(KogitoDMNResult result) {
        if (!result.hasErrors()) {
            return ResponseEntity.ok(buildResponse(result));
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

    private void enrichResponseHeaders(org.kie.dmn.api.core.DMNResult result, HttpServletResponse httpResponse) {
        if (!result.getMessages().isEmpty()) {
            String infoWarns = result.getMessages().stream().map(m -> m.getLevel() + " " + m.getMessage()).collect(Collectors.joining(", "));
            httpResponse.addHeader(KOGITO_DECISION_INFOWARN_HEADER, infoWarns);
        }

        org.kie.kogito.decision.DecisionExecutionIdUtils.getOptional(result.getContext())
                .ifPresent(executionId -> httpResponse.addHeader(KOGITO_EXECUTION_ID_HEADER, executionId));
    }
}