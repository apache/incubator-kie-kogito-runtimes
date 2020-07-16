package org.kie.kogito.pmml.rest;

import java.time.Period;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.drools.core.beliefsystem.simple.SimpleMode;
import org.kie.api.pmml.PMML4Result;
import org.kie.kogito.Application;


@Path("/$nameURL$")
public class PMMLRestResourceTemplate {

    Application application;

    private static final String KOGITO_DECISION_INFOWARN_HEADER = "X-Kogito-decision-messages";

    @javax.ws.rs.core.Context
    private org.jboss.resteasy.spi.HttpResponse httpResponse;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object pmml($inputType$ variables) {
        org.kie.kogito.prediction.PredictionModel prediction = application.predictionModels().getPredictionModel("$modelName$");
        return wrapResult(prediction.evaluateAll(prediction.newContext(variables)));
    }

    private Object wrapResult(PMML4Result result){
        try {
            return objectMapper.writeValueAsString(result);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);


}