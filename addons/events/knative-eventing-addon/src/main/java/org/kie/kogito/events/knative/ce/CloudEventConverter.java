package org.kie.kogito.events.knative.ce;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;

/**
 * Simple utility class to convert from CloudEvents objects to a Json String.
 * Wraps invocation to the CE SDK, so we can safely change the inner implementation without impacting callers.
 */
public final class CloudEventConverter {

    private static final EventFormat format = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE);

    private CloudEventConverter() {
    }

    public static String toJson(final CloudEvent cloudEvent) {
        return new String(format.serialize(cloudEvent));
    }

    public static CloudEvent toCloudEvent(final byte[] object) {
        return format.deserialize(object);
    }
}
