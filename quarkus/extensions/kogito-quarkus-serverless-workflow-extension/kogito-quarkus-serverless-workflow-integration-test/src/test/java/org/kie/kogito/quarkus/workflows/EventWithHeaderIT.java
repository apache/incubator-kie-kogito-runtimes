/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.quarkus.workflows;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.test.quarkus.QuarkusTestProperty;
import org.kie.kogito.test.quarkus.kafka.KafkaTestClient;
import org.kie.kogito.testcontainers.quarkus.KafkaQuarkusTestResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonCloudEventData;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.assertProcessInstanceHasFinished;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.getProcessInstance;

@QuarkusIntegrationTest
@QuarkusTestResource(SimpleServerServicesMock.class)
@QuarkusTestResource(KafkaQuarkusTestResource.class)
class EventWithHeaderIT {

    private static final String SERVICE_URL = "/event-with-headers";
    private static final String SERVICE_GET_BY_ID_URL = SERVICE_URL + "/{id}";
    private static final String EVENT_TYPE = "lock-event";
    private static final String EVENT_TOPIC = "lock-event";

    public static final String SIMPLE_TOKEN = "TEST_TOKEN";

    @QuarkusTestProperty(name = KafkaQuarkusTestResource.KOGITO_KAFKA_PROPERTY)
    String kafkaBootstrapServers;
    ObjectMapper objectMapper;
    KafkaTestClient kafkaClient;

    @BeforeEach
    void setup() {
        kafkaClient = new KafkaTestClient(kafkaBootstrapServers);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(JsonFormat.getCloudEventJacksonModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterEach
    void cleanUp() {
        if (kafkaClient != null) {
            kafkaClient.shutdown();
        }
    }

    @Test
    @SuppressWarnings("squid:S2699")
    void callbackStateSuccessful() throws Exception {
        executeCallbackStateSuccessfulPath(SERVICE_URL,
                SERVICE_GET_BY_ID_URL,
                EVENT_TYPE,
                EVENT_TOPIC);
    }

    String executeCallbackStateSuccessfulPath(String callbackProcessPostUrl,
            String callbackProcessGetByIdUrl,
            String callbackEventType,
            String callbackEventTopic) throws Exception {

        String response = objectMapper.writeValueAsString(CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create(""))
                .withType(callbackEventType)
                .withTime(OffsetDateTime.now())
                .withExtension("xauthorizationsimple", SIMPLE_TOKEN)
                .withData(JsonCloudEventData.wrap(objectMapper.createObjectNode().put("name", "The Kraken")))
                .build());
        kafkaClient.produce(response, callbackEventTopic);
        //wait until the message is received and processed
        Thread.sleep(2000);
        String processInstanceId = getProcessInstance(callbackProcessPostUrl);

        // give some time for the event to be processed and the process to finish.
        assertProcessInstanceHasFinished(callbackProcessGetByIdUrl, processInstanceId, 1, 60);
        return processInstanceId;
    }

    protected static String buildProcessInput(String query) {
        return "{\"workflowdata\": {\"query\": \"" + query + "\"} }";
    }
}
