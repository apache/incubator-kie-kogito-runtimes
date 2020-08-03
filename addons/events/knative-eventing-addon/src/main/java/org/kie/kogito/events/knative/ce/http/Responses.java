package org.kie.kogito.events.knative.ce.http;

import io.cloudevents.CloudEvent;
import org.kie.kogito.events.knative.ce.Printer;

import javax.ws.rs.core.Response;

/**
 * Utility class to create responses for CloudEvent processing over HTTP
 */
public final class Responses {

    private static final String ERROR_PROCESSING = "Failed to process HttpRequest into a CloudEvent format";
    private static final String ERROR_CHANNEL_NOT_BOUND = "Channel '%s' not bound, impossible to retransmit CloudEvent internally: %s";

    private Responses() {
    }

    public static Response errorProcessingCloudEvent(Throwable cause) {
        return Response.
                status(Response.Status.BAD_REQUEST).
                entity(new ResponseError(ERROR_PROCESSING, cause)).
                build();
    }

    public static Response channelNotBound(String channelName, CloudEvent cloudEvent) {
        return Response.
                serverError().
                entity(new ResponseError(String.format(ERROR_CHANNEL_NOT_BOUND, channelName, Printer.beautify(cloudEvent))))
                .build();
    }

}
