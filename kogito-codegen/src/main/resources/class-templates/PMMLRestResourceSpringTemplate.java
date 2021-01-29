package org.kie.kogito.pmml.rest;

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

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/InputSet")), description = "PMML input")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json",schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/OutputSet")), description = "PMML output")
    public Object pmml(@RequestBody(required = false) Map<String, Object> variables) {
        PredictionModel prediction = application.get(PredictionModels.class).getPredictionModel("$modelName$");
        return prediction.evaluateAll(prediction.newContext(variables));
    }
}