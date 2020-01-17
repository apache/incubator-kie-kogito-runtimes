/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.index.service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.index.InfinispanServerTestResource;
import org.kie.kogito.index.event.KogitoJobCloudEvent;
import org.kie.kogito.index.event.KogitoProcessCloudEvent;
import org.kie.kogito.index.event.KogitoUserTaskCloudEvent;
import org.kie.kogito.index.infinispan.protostream.ProtobufService;
import org.kie.kogito.index.messaging.ReactiveMessagingEventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasItems;
import static org.kie.kogito.index.GraphQLUtils.*;
import static org.kie.kogito.index.TestUtils.getDealsProtoBufferFile;
import static org.kie.kogito.index.TestUtils.getJobCloudEvent;
import static org.kie.kogito.index.TestUtils.getProcessCloudEvent;
import static org.kie.kogito.index.TestUtils.getTravelsProtoBufferFile;
import static org.kie.kogito.index.TestUtils.getUserTaskCloudEvent;
import static org.kie.kogito.index.json.JsonUtils.getObjectMapper;
import static org.kie.kogito.index.model.ProcessInstanceState.ACTIVE;
import static org.kie.kogito.index.model.ProcessInstanceState.COMPLETED;
import static org.kie.kogito.index.model.ProcessInstanceState.ERROR;

@QuarkusTest
@QuarkusTestResource(InfinispanServerTestResource.class)
public class IndexingServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexingServiceTest.class);

    @Inject
    ReactiveMessagingEventConsumer consumer;

    @Inject
    ProtobufService protobufService;

    @BeforeAll
    public static void setup() {
        RestAssured.config = RestAssured.config().encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
    }

    private static String formatZonedDateTime(ZonedDateTime time) {
        return time.truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Test
    public void testAddBrokenProtoFile() {
        try {
            protobufService.registerProtoBufferType(getBrokenProtoBufferFile());
            fail("Registering broken proto file should fail");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("Failed to resolve type of field \"travels.traveller\". Type not found : stringa");
        }
    }

    @Test
    public void testAddProtoFileMissingModel() {
        try {
            protobufService.registerProtoBufferType(getProtoBufferFileWithoutModel());
            fail("Registering broken proto file should fail");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("Missing marker for main message type in proto file, please add option kogito_model=\"messagename\"");
        }
    }

    @Test
    public void testAddProtoFileMissingId() {
        try {
            protobufService.registerProtoBufferType(getProtoBufferFileWithoutId());
            fail("Registering broken proto file should fail");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("Missing marker for process id in proto file, please add option kogito_id=\"processid\"");
        }
    }

    @Test
    public void testAddProtoFileMissingModelType() {
        try {
            protobufService.registerProtoBufferType(getProtoBufferFileWithoutModelType());
            fail("Registering broken proto file should fail");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("Could not find message with name: traveller in proto file, e, please review option kogito_model");
        }
    }

    @Test //Reproducer for KOGITO-172
    public void testAddProtoFileTwice() throws Exception {
        protobufService.registerProtoBufferType(getProtoBufferFileV1());
        given().contentType(ContentType.JSON).body("{ \"query\" : \"{Game{ player, id, name, metadata { processInstances { id } } } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Game", isA(Collection.class));
        given().contentType(ContentType.JSON).body("{ \"query\" : \"{ProcessInstances{ id, processId, rootProcessId, rootProcessInstanceId, parentProcessInstanceId } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.ProcessInstances", isA(Collection.class));

        protobufService.registerProtoBufferType(getProtoBufferFileV2());
        given().contentType(ContentType.JSON).body("{ \"query\" : \"{Game{ id, name, company, metadata { processInstances { id } } } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Game", isA(Collection.class));
        given().contentType(ContentType.JSON).body("{ \"query\" : \"{ProcessInstances{ id, processId, rootProcessId, rootProcessInstanceId, parentProcessInstanceId } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.ProcessInstances", isA(Collection.class));
    }

    @Test //Reproducer for KOGITO-334
    public void testDefaultGraphqlTypes() {
        given().contentType(ContentType.JSON).body("{ \"query\" : \"{ProcessInstances{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.ProcessInstances", isA(Collection.class));

        given().contentType(ContentType.JSON).body("{ \"query\" : \"{UserTaskInstances{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.UserTaskInstances", isA(Collection.class));

        given().contentType(ContentType.JSON).body("{ \"query\" : \"{Jobs{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Jobs", isA(Collection.class));
    }

    @Test
    public void testAddProtoFile() throws Exception {
        String processId = "travels";
        String subProcessId = processId + "_sub";
        String processInstanceId = UUID.randomUUID().toString();
        String subProcessInstanceId = UUID.randomUUID().toString();
        String firstTaskId = UUID.randomUUID().toString();
        String secondTaskId = UUID.randomUUID().toString();
        String state = "InProgress";

        protobufService.registerProtoBufferType(getTravelsProtoBufferFile());

        given().contentType(ContentType.JSON).body("{ \"query\" : \"{Travels{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Travels", isA(Collection.class));

        KogitoProcessCloudEvent startEvent = getProcessCloudEvent(processId, processInstanceId, ACTIVE, null, null, null);
        indexProcessCloudEvent(startEvent);

        validateProcessInstance(getProcessInstanceByIdAndState(processInstanceId, ACTIVE), startEvent);

        given().contentType(ContentType.JSON)
                .body(getTravelsByProcessInstanceId(processInstanceId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(startEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances.size()", is(1))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].start", is(formatZonedDateTime(startEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].end", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(startEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].endpoint", is(startEvent.getSource().toString()))
                .body("data.Travels[0].traveller.firstName", is("Maciej"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].flight.flightNumber", is("MX555"));

        KogitoProcessCloudEvent subProcessStartEvent = getProcessCloudEvent(subProcessId, subProcessInstanceId, ACTIVE, processInstanceId, processId, processInstanceId);
        subProcessStartEvent.getData().setVariables(getObjectMapper().readTree("{ \"traveller\":{\"firstName\":\"Maciej\", \"email\":\"mail@mail.com\", \"nationality\":\"Polish\"} }"));
        indexProcessCloudEvent(subProcessStartEvent);

        validateProcessInstance(getProcessInstanceByIdAndState(subProcessInstanceId, ACTIVE), subProcessStartEvent);
        validateProcessInstance(getProcessInstanceByIdAndState(processInstanceId, ACTIVE), startEvent, subProcessInstanceId);

        given().contentType(ContentType.JSON)
                .body(getTravelsByProcessInstanceId(subProcessInstanceId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(subProcessStartEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances.size()", is(2))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].start", is(formatZonedDateTime(startEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].end", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(startEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].endpoint", is(startEvent.getSource().toString()))
                .body("data.Travels[0].metadata.processInstances[1].id", is(subProcessInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].processId", is(subProcessId))
                .body("data.Travels[0].metadata.processInstances[1].rootProcessId", is(processId))
                .body("data.Travels[0].metadata.processInstances[1].rootProcessInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].parentProcessInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].start", is(formatZonedDateTime(subProcessStartEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[1].end", is(nullValue()))
                .body("data.Travels[0].traveller.firstName", is("Maciej"))
                .body("data.Travels[0].traveller.email", is("mail@mail.com"))
                .body("data.Travels[0].traveller.nationality", is("Polish"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].flight.flightNumber", is("MX555"))
                .body("data.Travels[0].flight.arrival", is("2019-08-20T22:12:57.340Z"))
                .body("data.Travels[0].flight.departure", is("2019-08-20T07:12:57.340Z"));

        KogitoProcessCloudEvent endEvent = getProcessCloudEvent(processId, processInstanceId, COMPLETED, null, null, null);
        indexProcessCloudEvent(endEvent);

        validateProcessInstance(getProcessInstanceByIdAndState(processInstanceId, COMPLETED), endEvent, subProcessInstanceId);

        KogitoUserTaskCloudEvent firstUserTaskEvent = getUserTaskCloudEvent(firstTaskId, subProcessId, subProcessInstanceId, processInstanceId, processId, state);

        indexUserTaskCloudEvent(firstUserTaskEvent);

        validateUserTaskInstance(getUserTaskInstanceById(firstTaskId), firstUserTaskEvent);

        given().contentType(ContentType.JSON)
                .body(geTravelsByUserTaskId(firstTaskId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(firstUserTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.userTasks.size()", is(1))
                .body("data.Travels[0].metadata.userTasks[0].id", is(firstTaskId))
                .body("data.Travels[0].metadata.userTasks[0].processInstanceId", is(subProcessInstanceId))
                .body("data.Travels[0].metadata.userTasks[0].description", is("TaskDescription"))
                .body("data.Travels[0].metadata.userTasks[0].name", is("TaskName"))
                .body("data.Travels[0].metadata.userTasks[0].priority", is("High"))
                .body("data.Travels[0].metadata.userTasks[0].actualOwner", is("kogito"))
                .body("data.Travels[0].metadata.userTasks[0].lastUpdate", is(formatZonedDateTime(firstUserTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances.size()", is(2))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].start", is(formatZonedDateTime(endEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].end", is(formatZonedDateTime(endEvent.getData().getEnd().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(endEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[1].id", is(subProcessInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].processId", is(subProcessId))
                .body("data.Travels[0].metadata.processInstances[1].rootProcessId", is(processId))
                .body("data.Travels[0].metadata.processInstances[1].rootProcessInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].parentProcessInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].start", is(formatZonedDateTime(subProcessStartEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[1].end", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[1].endpoint", is(subProcessStartEvent.getSource().toString()))
                .body("data.Travels[0].metadata.processInstances[1].lastUpdate", is(formatZonedDateTime(subProcessStartEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].traveller.firstName", is("Maciej"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].flight.flightNumber", is("MX555"))
                .body("data.Travels[0].flight.arrival", is("2019-08-20T22:12:57.340Z"))
                .body("data.Travels[0].flight.departure", is("2019-08-20T07:12:57.340Z"));

        KogitoUserTaskCloudEvent secondUserTaskEvent = getUserTaskCloudEvent(secondTaskId, processId, processInstanceId, null, null, state);

        indexUserTaskCloudEvent(secondUserTaskEvent);

        validateUserTaskInstance(getUserTaskInstanceById(secondTaskId), secondUserTaskEvent);

        given().contentType(ContentType.JSON)
                .body(geTravelsByUserTaskId(secondTaskId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(secondUserTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.userTasks.size()", is(2))
                .body("data.Travels[0].metadata.userTasks[0].id", is(firstTaskId))
                .body("data.Travels[0].metadata.userTasks[0].processInstanceId", is(subProcessInstanceId))
                .body("data.Travels[0].metadata.userTasks[0].description", is("TaskDescription"))
                .body("data.Travels[0].metadata.userTasks[0].name", is("TaskName"))
                .body("data.Travels[0].metadata.userTasks[0].priority", is("High"))
                .body("data.Travels[0].metadata.userTasks[0].actualOwner", is("kogito"))
                .body("data.Travels[0].metadata.userTasks[0].lastUpdate", is(formatZonedDateTime(firstUserTaskEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.userTasks[1].id", is(secondTaskId))
                .body("data.Travels[0].metadata.userTasks[1].processInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.userTasks[1].description", is("TaskDescription"))
                .body("data.Travels[0].metadata.userTasks[1].name", is("TaskName"))
                .body("data.Travels[0].metadata.userTasks[1].priority", is("High"))
                .body("data.Travels[0].metadata.userTasks[1].actualOwner", is("kogito"))
                .body("data.Travels[0].metadata.userTasks[1].lastUpdate", is(formatZonedDateTime(secondUserTaskEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances.size()", is(2))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].start", is(formatZonedDateTime(endEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].end", is(formatZonedDateTime(endEvent.getData().getEnd().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(endEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[1].id", is(subProcessInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].processId", is(subProcessId))
                .body("data.Travels[0].metadata.processInstances[1].rootProcessId", is(processId))
                .body("data.Travels[0].metadata.processInstances[1].rootProcessInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].parentProcessInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[1].start", is(formatZonedDateTime(subProcessStartEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[1].lastUpdate", is(formatZonedDateTime(subProcessStartEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[1].end", is(nullValue()))
                .body("data.Travels[0].traveller.firstName", is("Maciej"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].flight.flightNumber", is("MX555"))
                .body("data.Travels[0].flight.arrival", is("2019-08-20T22:12:57.340Z"))
                .body("data.Travels[0].flight.departure", is("2019-08-20T07:12:57.340Z"));
    }

    private void validateProcessInstance(String query, KogitoProcessCloudEvent event, String childProcessInstanceId) {
        LOGGER.debug("GraphQL query: {}", query);
        given().contentType(ContentType.JSON).body(query)
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.ProcessInstances[0].id", is(event.getProcessInstanceId()))
                .body("data.ProcessInstances[0].processId", is(event.getProcessId()))
                .body("data.ProcessInstances[0].rootProcessId", is(event.getRootProcessId()))
                .body("data.ProcessInstances[0].rootProcessInstanceId", is(event.getRootProcessInstanceId()))
                .body("data.ProcessInstances[0].parentProcessInstanceId", is(event.getParentProcessInstanceId()))
                .body("data.ProcessInstances[0].parentProcessInstance.id", event.getParentProcessInstanceId() == null ? is(nullValue()) : is(event.getParentProcessInstanceId()))
                .body("data.ProcessInstances[0].parentProcessInstance.processName", event.getParentProcessInstanceId() == null ? is(nullValue()) : is(event.getData().getProcessName()))
                .body("data.ProcessInstances[0].start", is(formatZonedDateTime(event.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.ProcessInstances[0].end", event.getData().getEnd() == null ? is(nullValue()) : is(formatZonedDateTime(event.getData().getEnd().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.ProcessInstances[0].childProcessInstances[0].id", childProcessInstanceId == null ? is(nullValue()) : is(childProcessInstanceId))
                .body("data.ProcessInstances[0].childProcessInstances[0].processName", childProcessInstanceId == null ? is(nullValue()) : is(event.getData().getProcessName()))
                .body("data.ProcessInstances[0].endpoint", is(event.getSource().toString()))
                .body("data.ProcessInstances[0].addons", hasItems(event.getData().getAddons().toArray()))
                .body("data.ProcessInstances[0].error.message", event.getData().getError() == null ? is(nullValue()) : is(event.getData().getError().getMessage()))
                .body("data.ProcessInstances[0].error.nodeDefinitionId", event.getData().getError() == null ? is(nullValue()) : is(event.getData().getError().getNodeDefinitionId()))
                .body("data.ProcessInstances[0].lastUpdate", is(formatZonedDateTime(event.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))));
    }

    private void validateProcessInstance(String query, KogitoProcessCloudEvent event) {
        validateProcessInstance(query, event, null);
    }

    private void indexProcessCloudEvent(KogitoProcessCloudEvent event) throws Exception {
        CompletableFuture.allOf(
                consumer.onProcessInstanceEvent(event).toCompletableFuture(),
                consumer.onProcessInstanceDomainEvent(event).toCompletableFuture()
        ).get();
    }

    private void indexUserTaskCloudEvent(KogitoUserTaskCloudEvent event) throws Exception {
        CompletableFuture.allOf(
                consumer.onUserTaskInstanceEvent(event).toCompletableFuture(),
                consumer.onUserTaskInstanceDomainEvent(event).toCompletableFuture()
        ).get();
    }

    @Test
    public void testIndexingDomainUsingUserTaskEventFirst() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String state = "InProgress";
        String processId = "travels";
        String processInstanceId = UUID.randomUUID().toString();

        protobufService.registerProtoBufferType(getTravelsProtoBufferFile());

        given().contentType(ContentType.JSON).body("{ \"query\" : \"{ Travels{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Travels", isA(Collection.class));

        KogitoUserTaskCloudEvent userTaskEvent = getUserTaskCloudEvent(taskId, processId, processInstanceId, null, null, state);
        consumer.onUserTaskInstanceDomainEvent(userTaskEvent).toCompletableFuture().get();

        given().contentType(ContentType.JSON)
                .body(geTravelsByUserTaskId(taskId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].flight", is(nullValue()))
                .body("data.Travels[0].hotel", is(nullValue()))
                .body("data.Travels[0].traveller", is(nullValue()))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(userTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.userTasks.size()", is(1))
                .body("data.Travels[0].metadata.userTasks[0].id", is(taskId))
                .body("data.Travels[0].metadata.userTasks[0].processInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.userTasks[0].description", is(userTaskEvent.getData().getDescription()))
                .body("data.Travels[0].metadata.userTasks[0].name", is(userTaskEvent.getData().getName()))
                .body("data.Travels[0].metadata.userTasks[0].priority", is(userTaskEvent.getData().getPriority()))
                .body("data.Travels[0].metadata.userTasks[0].actualOwner", is(userTaskEvent.getData().getActualOwner()))
                .body("data.Travels[0].metadata.userTasks[0].lastUpdate", is(formatZonedDateTime(userTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances", is(nullValue()));

        KogitoProcessCloudEvent processEvent = getProcessCloudEvent(processId, processInstanceId, ACTIVE, null, null, null);
        consumer.onProcessInstanceDomainEvent(processEvent).toCompletableFuture().get();

        given().contentType(ContentType.JSON)
                .body(getTravelsByProcessInstanceId(processInstanceId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].flight.flightNumber", is("MX555"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].traveller.firstName", is("Maciej"))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(processEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.userTasks.size()", is(1))
                .body("data.Travels[0].metadata.userTasks[0].id", is(taskId))
                .body("data.Travels[0].metadata.userTasks[0].processInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.userTasks[0].description", is(userTaskEvent.getData().getDescription()))
                .body("data.Travels[0].metadata.userTasks[0].name", is(userTaskEvent.getData().getName()))
                .body("data.Travels[0].metadata.userTasks[0].priority", is(userTaskEvent.getData().getPriority()))
                .body("data.Travels[0].metadata.userTasks[0].actualOwner", is(userTaskEvent.getData().getActualOwner()))
                .body("data.Travels[0].metadata.userTasks[0].lastUpdate", is(formatZonedDateTime(userTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances.size()", is(1))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(processEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))));
    }

    @Test
    public void testIndexingDomainUsingProcessEventFirst() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String state = "InProgress";
        String processId = "travels";
        String processInstanceId = UUID.randomUUID().toString();

        protobufService.registerProtoBufferType(getTravelsProtoBufferFile());

        given().contentType(ContentType.JSON).body("{ \"query\" : \"{ Travels{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Travels", isA(Collection.class));

        KogitoProcessCloudEvent processEvent = getProcessCloudEvent(processId, processInstanceId, ACTIVE, null, null, null);
        consumer.onProcessInstanceDomainEvent(processEvent).toCompletableFuture().get();

        given().contentType(ContentType.JSON)
                .body(getTravelsByProcessInstanceId(processInstanceId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].flight.flightNumber", is("MX555"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].traveller.firstName", is("Maciej"))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(processEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.userTasks", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances.size()", is(1))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(processEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))));


        KogitoUserTaskCloudEvent userTaskEvent = getUserTaskCloudEvent(taskId, processId, processInstanceId, null, null, state);
        consumer.onUserTaskInstanceDomainEvent(userTaskEvent).toCompletableFuture().get();

        given().contentType(ContentType.JSON)
                .body(geTravelsByUserTaskId(taskId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].flight.flightNumber", is("MX555"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].traveller.firstName", is("Maciej"))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(userTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.userTasks.size()", is(1))
                .body("data.Travels[0].metadata.userTasks[0].id", is(taskId))
                .body("data.Travels[0].metadata.userTasks[0].processInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.userTasks[0].description", is(userTaskEvent.getData().getDescription()))
                .body("data.Travels[0].metadata.userTasks[0].name", is(userTaskEvent.getData().getName()))
                .body("data.Travels[0].metadata.userTasks[0].priority", is(userTaskEvent.getData().getPriority()))
                .body("data.Travels[0].metadata.userTasks[0].actualOwner", is(userTaskEvent.getData().getActualOwner()))
                .body("data.Travels[0].metadata.userTasks[0].lastUpdate", is(formatZonedDateTime(userTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances.size()", is(1))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(processEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))));
    }

    @Test
    public void testIndexingDomainParallelEvents() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String state = "InProgress";
        String processId = "travels";
        String processInstanceId = UUID.randomUUID().toString();

        protobufService.registerProtoBufferType(getTravelsProtoBufferFile());

        given().contentType(ContentType.JSON).body("{ \"query\" : \"{ Travels{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Travels", isA(Collection.class));

        KogitoProcessCloudEvent processEvent = getProcessCloudEvent(processId, processInstanceId, ACTIVE, null, null, null);
        KogitoUserTaskCloudEvent userTaskEvent = getUserTaskCloudEvent(taskId, processId, processInstanceId, null, null, state);

        CompletableFuture.allOf(
                consumer.onProcessInstanceDomainEvent(processEvent).toCompletableFuture(),
                consumer.onUserTaskInstanceDomainEvent(userTaskEvent).toCompletableFuture()
        ).get();

        given().contentType(ContentType.JSON)
                .body(getTravelsByProcessInstanceId(processInstanceId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].flight.flightNumber", is("MX555"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].traveller.firstName", is("Maciej"))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(userTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.userTasks.size()", is(1))
                .body("data.Travels[0].metadata.userTasks[0].id", is(taskId))
                .body("data.Travels[0].metadata.userTasks[0].processInstanceId", is(processInstanceId))
                .body("data.Travels[0].metadata.userTasks[0].description", is(userTaskEvent.getData().getDescription()))
                .body("data.Travels[0].metadata.userTasks[0].name", is(userTaskEvent.getData().getName()))
                .body("data.Travels[0].metadata.userTasks[0].priority", is(userTaskEvent.getData().getPriority()))
                .body("data.Travels[0].metadata.userTasks[0].actualOwner", is(userTaskEvent.getData().getActualOwner()))
                .body("data.Travels[0].metadata.userTasks[0].lastUpdate", is(formatZonedDateTime(userTaskEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances.size()", is(1))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(processEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))));
    }

    @Test
    public void testProcessInstanceIndex() throws Exception {
        String processId = "travels";
        String processInstanceId = UUID.randomUUID().toString();
        String subProcessId = processId + "_sub";
        String subProcessInstanceId = UUID.randomUUID().toString();

        protobufService.registerProtoBufferType(getTravelsProtoBufferFile());

        given().contentType(ContentType.JSON).body("{ \"query\" : \"{ Travels{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Travels", isA(Collection.class));

        KogitoProcessCloudEvent startEvent = getProcessCloudEvent(processId, processInstanceId, ACTIVE, null, null, null);
        indexProcessCloudEvent(startEvent);

        validateProcessInstance(getProcessInstanceById(processInstanceId), startEvent);
        validateProcessInstance(getProcessInstanceByIdAndState(processInstanceId, ACTIVE), startEvent);
        validateProcessInstance(getProcessInstanceByIdAndProcessId(processInstanceId, processId), startEvent);
        validateProcessInstance(getProcessInstanceByIdAndStart(processInstanceId, formatZonedDateTime(startEvent.getData().getStart())), startEvent);
        validateProcessInstance(getProcessInstanceByIdAndAddon(processInstanceId, "process-management"), startEvent);

        given().contentType(ContentType.JSON)
                .body(getTravelsByProcessInstanceId(processInstanceId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(startEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))                
                .body("data.Travels[0].metadata.processInstances.size()", is(1))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].state", is(ACTIVE.name()))
                .body("data.Travels[0].metadata.processInstances[0].start", is(formatZonedDateTime(startEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].lastUpdate", is(formatZonedDateTime(startEvent.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].end", is(nullValue()))
                .body("data.Travels[0].flight.flightNumber", is("MX555"))
                .body("data.Travels[0].hotel.name", is("Meriton"))
                .body("data.Travels[0].traveller.firstName", is("Maciej"));

        KogitoProcessCloudEvent endEvent = getProcessCloudEvent(processId, processInstanceId, COMPLETED, null, null, null);
        endEvent.getData().setEnd(ZonedDateTime.now());
        endEvent.getData().setVariables(getObjectMapper().readTree("{ \"traveller\":{\"firstName\":\"Maciej\"},\"hotel\":{\"name\":\"Ibis\"},\"flight\":{\"arrival\":\"2019-08-20T22:12:57.340Z\",\"departure\":\"2019-08-20T07:12:57.340Z\",\"flightNumber\":\"QF444\"} }"));
        indexProcessCloudEvent(endEvent);

        validateProcessInstance(getProcessInstanceByIdAndState(processInstanceId, COMPLETED), endEvent);

        given().contentType(ContentType.JSON)
                .body(getTravelsByProcessInstanceId(processInstanceId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Travels[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.lastUpdate", is(formatZonedDateTime(endEvent.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances.size()", is(1))
                .body("data.Travels[0].metadata.processInstances[0].id", is(processInstanceId))
                .body("data.Travels[0].metadata.processInstances[0].processId", is(processId))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].rootProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].parentProcessInstanceId", is(nullValue()))
                .body("data.Travels[0].metadata.processInstances[0].state", is(COMPLETED.name()))
                .body("data.Travels[0].metadata.processInstances[0].start", is(formatZonedDateTime(endEvent.getData().getStart().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].metadata.processInstances[0].end", is(formatZonedDateTime(endEvent.getData().getEnd().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Travels[0].flight.flightNumber", is("QF444"))
                .body("data.Travels[0].hotel.name", is("Ibis"))
                .body("data.Travels[0].traveller.firstName", is("Maciej"));

        KogitoProcessCloudEvent event = getProcessCloudEvent(subProcessId, subProcessInstanceId, ACTIVE, processInstanceId, processId, processInstanceId);
        indexProcessCloudEvent(event);

        validateProcessInstance(getProcessInstanceByParentProcessInstanceId(processInstanceId), event);
        validateProcessInstance(getProcessInstanceByIdAndNullParentProcessInstanceId(processInstanceId, true), endEvent, subProcessInstanceId);
        validateProcessInstance(getProcessInstanceByRootProcessInstanceId(processInstanceId), event);
        validateProcessInstance(getProcessInstanceByIdAndNullRootProcessInstanceId(processInstanceId, true), endEvent, subProcessInstanceId);
        validateProcessInstance(getProcessInstanceById(processInstanceId), endEvent, subProcessInstanceId);
        validateProcessInstance(getProcessInstanceByIdAndParentProcessInstanceId(subProcessInstanceId, processInstanceId), event);

        KogitoProcessCloudEvent errorEvent = getProcessCloudEvent(subProcessId, subProcessInstanceId, ERROR, processInstanceId, processId, processInstanceId);
        indexProcessCloudEvent(errorEvent);

        validateProcessInstance(getProcessInstanceByIdAndErrorNode(subProcessInstanceId, errorEvent.getData().getError().getNodeDefinitionId()), errorEvent);
    }

    @Test
    public void testUserTaskInstanceIndex() throws Exception {
        String taskId = UUID.randomUUID().toString();
        String state = "InProgress";
        String processId = "deals";
        String processInstanceId = UUID.randomUUID().toString();

        protobufService.registerProtoBufferType(getDealsProtoBufferFile());

        given().contentType(ContentType.JSON).body("{ \"query\" : \"{ Deals{ id } }\" }")
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200).body("data.Deals", isA(Collection.class));

        KogitoUserTaskCloudEvent event = getUserTaskCloudEvent(taskId, processId, processInstanceId, null, null, state);
        indexUserTaskCloudEvent(event);

        validateUserTaskInstance(getUserTaskInstanceById(taskId), event);
        validateUserTaskInstance(getUserTaskInstanceByIdAndActualOwner(taskId, "kogito"), event);

        validateUserTaskInstance(getUserTaskInstanceByIdAndPotentialGroups(taskId, new ArrayList<>(event.getData().getPotentialGroups())), event);
        validateUserTaskInstance(getUserTaskInstanceByIdAndPotentialUsers(taskId, new ArrayList<>(event.getData().getPotentialUsers())), event);
        validateUserTaskInstance(getUserTaskInstanceByIdAndState(taskId, event.getData().getState()), event);
        validateUserTaskInstance(getUserTaskInstanceByIdAndStarted(taskId, formatZonedDateTime(event.getData().getStarted())), event);
        validateUserTaskInstance(getUserTaskInstanceByIdAndCompleted(taskId, formatZonedDateTime(event.getData().getCompleted())), event);

        given().contentType(ContentType.JSON)
                .body(getDealsByTaskId(taskId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Deals[0].id", is(processInstanceId))
                .body("data.Deals[0].metadata.lastUpdate", is(formatZonedDateTime(event.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Deals[0].metadata.userTasks.size()", is(1))
                .body("data.Deals[0].metadata.userTasks[0].id", is(taskId))
                .body("data.Deals[0].metadata.userTasks[0].description", is("TaskDescription"))
                .body("data.Deals[0].metadata.userTasks[0].state", is("InProgress"))
                .body("data.Deals[0].metadata.userTasks[0].name", is("TaskName"))
                .body("data.Deals[0].metadata.userTasks[0].priority", is("High"))
                .body("data.Deals[0].metadata.userTasks[0].actualOwner", is("kogito"))
                .body("data.Deals[0].metadata.userTasks[0].started", is(formatZonedDateTime(event.getData().getStarted().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Deals[0].metadata.userTasks[0].completed", is(formatZonedDateTime(event.getData().getCompleted().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Deals[0].metadata.userTasks[0].lastUpdate", is(formatZonedDateTime(event.getTime().withZoneSameInstant(ZoneOffset.UTC))));

        event = getUserTaskCloudEvent(taskId, processId, processInstanceId, null, null, state);
        event.getData().setCompleted(ZonedDateTime.now());
        event.getData().setPriority("Low");
        event.getData().setActualOwner("admin");
        event.getData().setState("Completed");

        indexUserTaskCloudEvent(event);

        validateUserTaskInstance(getUserTaskInstanceByIdAndActualOwner(taskId, "admin"), event);

        given().contentType(ContentType.JSON)
                .body(getDealsByTaskId(taskId))
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Deals[0].id", is(processInstanceId))
                .body("data.Deals[0].metadata.lastUpdate", is(formatZonedDateTime(event.getTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Deals[0].metadata.userTasks.size()", is(1))
                .body("data.Deals[0].metadata.userTasks[0].id", is(taskId))
                .body("data.Deals[0].metadata.userTasks[0].description", is("TaskDescription"))
                .body("data.Deals[0].metadata.userTasks[0].state", is("Completed"))
                .body("data.Deals[0].metadata.userTasks[0].name", is("TaskName"))
                .body("data.Deals[0].metadata.userTasks[0].priority", is("Low"))
                .body("data.Deals[0].metadata.userTasks[0].actualOwner", is("admin"))
                .body("data.Deals[0].metadata.userTasks[0].started", is(formatZonedDateTime(event.getData().getStarted().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Deals[0].metadata.userTasks[0].completed", is(formatZonedDateTime(event.getData().getCompleted().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Deals[0].metadata.userTasks[0].lastUpdate", is(formatZonedDateTime(event.getTime().withZoneSameInstant(ZoneOffset.UTC))));
    }

    @Test
    public void testJobIndex() throws Exception {
        String jobId = UUID.randomUUID().toString();
        String processId = "deals";
        String processInstanceId = UUID.randomUUID().toString();
        
        KogitoJobCloudEvent event = getJobCloudEvent(jobId, processId, processInstanceId, null, null);
        
        consumer.onJobEvent(event).toCompletableFuture().get();

        validateJob(getJobById(jobId), event);
    }

    private void validateJob(String query, KogitoJobCloudEvent event) {
        LOGGER.debug("GraphQL query: {}", query);
        given().contentType(ContentType.JSON).body(query)
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.Jobs[0].id", is(event.getData().getId()))
                .body("data.Jobs[0].processId", is(event.getData().getProcessId()))
                .body("data.Jobs[0].processInstanceId", is(event.getData().getProcessInstanceId()))
                .body("data.Jobs[0].rootProcessId", is(event.getData().getRootProcessId()))
                .body("data.Jobs[0].rootProcessInstanceId", is(event.getData().getRootProcessInstanceId()))
                .body("data.Jobs[0].status", is(event.getData().getStatus()))
                .body("data.Jobs[0].expirationTime", is(formatZonedDateTime(event.getData().getExpirationTime().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Jobs[0].priority", is(event.getData().getPriority()))
                .body("data.Jobs[0].callbackEndpoint", is(event.getData().getCallbackEndpoint()))
                .body("data.Jobs[0].repeatInterval", is(event.getData().getRepeatInterval().intValue()))
                .body("data.Jobs[0].repeatLimit", is(event.getData().getRepeatLimit()))
                .body("data.Jobs[0].scheduledId", is(event.getData().getScheduledId()))
                .body("data.Jobs[0].retries", is(event.getData().getRetries()))
                .body("data.Jobs[0].lastUpdate", is(formatZonedDateTime(event.getData().getLastUpdate().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.Jobs[0].executionCounter", is(event.getData().getExecutionCounter()));
    }

    private void validateUserTaskInstance(String query, KogitoUserTaskCloudEvent event) {
        LOGGER.debug("GraphQL query: {}", query);
        given().contentType(ContentType.JSON).body(query)
                .when().post("/graphql")
                .then().log().ifValidationFails().statusCode(200)
                .body("data.UserTaskInstances[0].id", is(event.getUserTaskInstanceId()))
                .body("data.UserTaskInstances[0].processId", is(event.getProcessId()))
                .body("data.UserTaskInstances[0].rootProcessId", is(event.getRootProcessId()))
                .body("data.UserTaskInstances[0].rootProcessInstanceId", is(event.getRootProcessInstanceId()))
                .body("data.UserTaskInstances[0].description", is(event.getData().getDescription()))
                .body("data.UserTaskInstances[0].name", is(event.getData().getName()))
                .body("data.UserTaskInstances[0].priority", is(event.getData().getPriority()))
                .body("data.UserTaskInstances[0].actualOwner", is(event.getData().getActualOwner()))
                .body("data.UserTaskInstances[0].excludedUsers", hasItems(event.getData().getExcludedUsers().toArray()))
                .body("data.UserTaskInstances[0].potentialUsers", hasItems(event.getData().getPotentialUsers().toArray()))
                .body("data.UserTaskInstances[0].potentialGroups", hasItems(event.getData().getPotentialGroups().toArray()))
                .body("data.UserTaskInstances[0].started", is(formatZonedDateTime(event.getData().getStarted().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.UserTaskInstances[0].completed", is(formatZonedDateTime(event.getData().getCompleted().withZoneSameInstant(ZoneOffset.UTC))))
                .body("data.UserTaskInstances[0].lastUpdate", is(formatZonedDateTime(event.getTime().withZoneSameInstant(ZoneOffset.UTC))));
    }

    private String getProtoBufferFileWithoutModelType() {
        return "   option kogito_id=\"travels\";\n" +
                "   option kogito_model=\"traveller\";\n" +
                "message travels {\n" +
                "   optional string traveller = 1;\n" +
                "   optional string hotel = 2;\n" +
                "   optional string flight = 3;\n" +
                "}\n" +
                "\n";
    }

    private String getProtoBufferFileWithoutId() {
        return "   option kogito_model=\"travels\";\n" +
                "message travels {\n" +
                "   optional string traveller = 1;\n" +
                "   optional string hotel = 2;\n" +
                "   optional string flight = 3;\n" +
                "}\n" +
                "\n";
    }

    private String getProtoBufferFileWithoutModel() {
        return "   option kogito_id=\"travels\";\n" +
                "message travels {\n" +
                "   optional string traveller = 1;\n" +
                "   optional string hotel = 2;\n" +
                "   optional string flight = 3;\n" +
                "}\n" +
                "\n";
    }

    private String getBrokenProtoBufferFile() {
        return "message travels {\n" +
                "   optional stringa traveller = 1;\n" +
                "   optional string hotel = 2;\n" +
                "   optional string flight = 3;\n" +
                "}\n" +
                "\n";
    }

    private String getProtoBufferFileV1() {
        return "import \"kogito-index.proto\";\n" +
                "option kogito_model=\"Game\";\n" +
                "option kogito_id=\"game\";\n" +
                "message Game {\n" +
                "   optional string player = 1;\n" +
                "   optional string id = 2;\n" +
                "   optional string name = 3;\n" +
                "   optional org.kie.kogito.index.model.KogitoMetadata metadata = 4;\n" +
                "}\n" +
                "\n";
    }

    private String getProtoBufferFileV2() {
        return "import \"kogito-index.proto\";\n" +
                "option kogito_model=\"Game\";\n" +
                "option kogito_id=\"game\";\n" +
                "message Game {\n" +
                "   optional string id = 1;\n" +
                "   optional string name = 2;\n" +
                "   optional string company = 3;\n" +
                "   optional org.kie.kogito.index.model.KogitoMetadata metadata = 4;\n" +
                "}\n" +
                "\n";
    }
}