package org.kie.kogito.events.knative.ce.http;

import io.cloudevents.CloudEvent;

/**
 * Public common interface for HttpRequest converters.
 *
 * @param <H> HttpRequest reference from the target runtime
 */
public interface HttpRequestConverter<H> {

    CloudEvent from(H request);

}
