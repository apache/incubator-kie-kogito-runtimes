package org.kie.kogito.events.knative.ce.http;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

class ResponsesTest {

    @Test
    void errorProcessingCloudEvent() {
        final Response response = Responses.errorProcessingCloudEvent(new IllegalArgumentException("Fail!"));
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        Assertions.assertThat(response.getEntity()).isInstanceOf(ResponseError.class);
    }

    @Test
    void channelNotBound() {
        final Response response = Responses.channelNotBound("MyChannel", null);
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        Assertions.assertThat(response.getEntity()).isInstanceOf(ResponseError.class);
    }
}