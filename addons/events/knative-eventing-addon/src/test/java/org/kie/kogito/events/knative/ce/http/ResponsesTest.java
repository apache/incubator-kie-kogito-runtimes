package org.kie.kogito.events.knative.ce.http;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.*;

class ResponsesTest {

    @Test
    void errorProcessingCloudEvent() {
        final Response response = Responses.errorProcessingCloudEvent(new IllegalArgumentException("Fail!"));
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(ResponseError.class);
    }

    @Test
    void channelNotBound() {
        final Response response = Responses.channelNotBound("MyChannel", null);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(ResponseError.class);
    }
}