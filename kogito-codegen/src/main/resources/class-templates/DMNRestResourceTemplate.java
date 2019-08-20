package org.kie.dmn.kogito.quarkus.example;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.dmn.core.impl.DMNContextImpl;
import org.kie.dmn.kogito.rest.quarkus.DMNResult;
import org.kie.kogito.Application;

@Path("/$nameURL$")
public class DMNRestResourceTemplate {

    Application application;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DMNResult dmn(Map<String, Object> dmnContext) {
        return new DMNResult(application.decisions()
                                        .evaluateAll(application.decisions()
                                                                .getModel("$modelNamespace$", "$modelName$"),
                                                     new DMNContextImpl(dmnContext)));
    }

}
