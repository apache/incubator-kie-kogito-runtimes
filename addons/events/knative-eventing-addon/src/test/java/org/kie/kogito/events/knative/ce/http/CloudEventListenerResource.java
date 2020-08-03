package org.kie.kogito.events.knative.ce.http;

import io.cloudevents.CloudEvent;
import org.jboss.resteasy.spi.HttpRequest;
import org.kie.kogito.events.knative.ce.CloudEventConverter;
import org.kie.kogito.events.knative.ce.Printer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class CloudEventListenerResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudEventListenerResource.class);

    @POST()
    @Consumes({MediaType.APPLICATION_JSON, ExtMediaType.CLOUDEVENTS_JSON, MediaType.TEXT_PLAIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response cloudEventListener(@Context HttpRequest request) {
        try {
            final CloudEvent event = new RestEasyHttpRequestConverter().from(request);
            LOGGER.info("CloudEvent processed: {}", Printer.beautify(event));
            return Response.ok(CloudEventConverter.toJson(event)).build();
        } catch (Exception ex) {
            LOGGER.debug("Fail to process CloudEvent: ", ex);
            return Responses.errorProcessingCloudEvent(ex);
        }
    }
}
