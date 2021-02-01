package org.kie.kogito.pmml.rest;

import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.kogito.Application;

@Path("/$nameURL$")
public class PMMLRestResourceTemplate {

    Application application;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @org.eclipse.microprofile.openapi.annotations.parameters.RequestBody(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json",schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/InputSet")), description = "PMML input")
    @org.eclipse.microprofile.openapi.annotations.responses.APIResponse(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json", schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/ResultSet")), description = "PMML result")
    public Object result(java.util.Map<String, Object> variables) {
        org.kie.kogito.prediction.PredictionModel prediction = application.get(org.kie.kogito.prediction.PredictionModels.class).getPredictionModel("$modelName$");
        org.kie.api.pmml.PMML4Result pmml4Result = prediction.evaluateAll(prediction.newContext(variables));
        java.util.Map<String, Object> toReturn = java.util.Collections.singletonMap(pmml4Result.getResultObjectName(), pmml4Result.getResultVariables().get(pmml4Result.getResultObjectName()));
        return toReturn;
    }

    @POST
    @Path("/descriptive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @org.eclipse.microprofile.openapi.annotations.parameters.RequestBody(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json",schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/InputSet")), description = "PMML input")
    @org.eclipse.microprofile.openapi.annotations.responses.APIResponse(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json", schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/OutputSet")), description = "PMML full output")
    public org.kie.api.pmml.PMML4Result descriptive(java.util.Map<String, Object> variables) {
        org.kie.kogito.prediction.PredictionModel prediction = application.get(org.kie.kogito.prediction.PredictionModels.class).getPredictionModel("$modelName$");
        return prediction.evaluateAll(prediction.newContext(variables));
    }

}