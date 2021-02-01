package org.kie.kogito.pmml.rest;

import java.util.Collections;
import java.util.Map;

import org.kie.kogito.Application;
import org.kie.kogito.prediction.PredictionModel;

import org.kie.kogito.prediction.PredictionModels;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/$nameURL$")
public class PMMLRestResourceTemplate {

    Application application;


    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/InputSet")), description = "PMML input")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/ResultSet")), description = "PMML result")
    public Object result(@RequestBody(required = false) Map<String, Object> variables) {
        PredictionModel prediction = application.get(PredictionModels.class).getPredictionModel("$modelName$");
        Map<String, Object> toReturn = Collections.singletonMap(pmml4Result.getResultObjectName(), pmml4Result.getResultVariables().get(pmml4Result.getResultObjectName()));
        return toReturn;
    }

    @PostMapping(value = "/descriptive", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/InputSet")), description = "PMML input")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/OutputSet")), description = "PMML full output")
    public org.kie.api.pmml.PMML4Result descriptive(@RequestBody(required = false) Map<String, Object> variables) {
        PredictionModel prediction = application.get(PredictionModels.class).getPredictionModel("$modelName$");
        return prediction.evaluateAll(prediction.newContext(variables));
    }
}