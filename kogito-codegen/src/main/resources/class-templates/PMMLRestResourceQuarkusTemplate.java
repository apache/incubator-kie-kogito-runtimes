package org.kie.kogito.pmml.rest;

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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @org.eclipse.microprofile.openapi.annotations.parameters.RequestBody(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json",schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/InputSet")), description = "PMML input")
    @org.eclipse.microprofile.openapi.annotations.responses.APIResponse(content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "application/json", schema = @org.eclipse.microprofile.openapi.annotations.media.Schema(ref = "/pmmlDefinitions.json#/definitions/OutputSet")), description = "PMML output")
    public Object pmml(java.util.Map<String, Object> variables) {
        org.kie.kogito.prediction.PredictionModel prediction = application.get(org.kie.kogito.prediction.PredictionModels.class).getPredictionModel("$modelName$");
        return prediction.evaluateAll(prediction.newContext(variables));
    }
}