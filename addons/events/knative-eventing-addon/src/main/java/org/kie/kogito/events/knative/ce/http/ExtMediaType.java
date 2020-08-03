package org.kie.kogito.events.knative.ce.http;

import javax.ws.rs.core.MediaType;

/**
 * Extends {@link MediaType} to CloudEvents support
 */
// this shouldn't be provided by the CE SDK? Send a PR.
public final class ExtMediaType {

    public static final String CLOUDEVENTS_JSON = "application/cloudevents+json";
    public static final MediaType CLOUDEVENTS_JSON_TYPE = new MediaType("application", "cloudevents+json");

    private ExtMediaType() {

    }
}
