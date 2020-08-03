package org.kie.kogito.events.knative.ce.http;

import io.cloudevents.CloudEvent;
import io.cloudevents.http.restful.ws.impl.RestfulWSMessageFactory;
import org.jboss.resteasy.spi.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for HTTP requests conversions to CloudEvents interface
 */
public class RestEasyHttpRequestConverter extends AbstractHttpRequestConverter implements HttpRequestConverter<HttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEasyHttpRequestConverter.class);

    /**
     * Converts a given RestEasy HttpRequest into a CloudEvent type, parsing the data payload into a known type
     * Use {@link #from(HttpRequest)} if you don't know the type or if you don't wish to unmarshal the payload.
     *
     * @param request RestEasy HttpRequest
     * @return A CloudEvent
     * @throws IllegalArgumentException if fails to parse the CloudEvent properly
     */
    public CloudEvent from(HttpRequest request) {
        try {
            LOGGER.debug("About to convert an HttpRequest into CloudEvent");
            final String payload = inputStreamToString(request.getInputStream());
            LOGGER.debug("HttpRequest payload to be converted into CloudEvent: \n{}", payload);
            return RestfulWSMessageFactory.create(request.getHttpHeaders().getMediaType(), request.getHttpHeaders().getRequestHeaders(), payload.getBytes()).toEvent();
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    String.format("Failed to parse CloudEvent. For more detailed payload information, turn DEBUG on: '%s'", ex.getMessage()), ex);
        }
    }
}
