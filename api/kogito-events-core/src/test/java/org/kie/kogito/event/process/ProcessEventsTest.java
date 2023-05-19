/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.event.process;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.kie.kogito.event.AbstractDataEvent;
import org.kie.kogito.event.cloudevents.CloudEventExtensionConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.cloudevents.SpecVersion;
import io.cloudevents.jackson.JsonFormat;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessEventsTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(JsonFormat.getCloudEventJacksonModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Set<String> BASE_EXTENSION_NAMES = Arrays.stream(new String[] {
            CloudEventExtensionConstants.PROCESS_INSTANCE_ID,
            CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_INSTANCE_ID,
            CloudEventExtensionConstants.PROCESS_ID,
            CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_ID,
            CloudEventExtensionConstants.ADDONS,
            CloudEventExtensionConstants.PROCESS_INSTANCE_VERSION,
            CloudEventExtensionConstants.PROCESS_PARENT_PROCESS_INSTANCE_ID,
            CloudEventExtensionConstants.PROCESS_INSTANCE_STATE,
            CloudEventExtensionConstants.PROCESS_REFERENCE_ID,
            CloudEventExtensionConstants.PROCESS_START_FROM_NODE,
            CloudEventExtensionConstants.BUSINESS_KEY,
            CloudEventExtensionConstants.PROCESS_TYPE }).collect(Collectors.toSet());

    private static final String PROCESS_INSTANCE_EVENT_TYPE = "ProcessInstanceEvent";
    private static final String USER_TASK_INSTANCE_EVENT_TYPE = "UserTaskInstanceEvent";
    private static final String VARIABLE_INSTANCE_EVENT_TYPE = "VariableInstanceEvent";

    private static final String ID = "ID";
    private static final SpecVersion SPEC_VERSION = SpecVersion.V1;
    private static final URI SOURCE = URI.create("http://event-test-source");
    private static final OffsetDateTime TIME = OffsetDateTime.parse("2021-11-24T18:00:00.000+01:00");
    private static final String SUBJECT = "SUBJECT";
    private static final String DATA_CONTENT_TYPE = "application/json";
    private static final URI DATA_SCHEMA = URI.create("http://event-test-source/data-schema");

    private static final String PROCESS_ID = "PROCESS_ID";
    private static final String PROCESS_INSTANCE_ID = "PROCESS_INSTANCE_ID";
    private static final String PROCESS_INSTANCE_VERSION = "PROCESS_INSTANCE_VERSION";
    private static final String ROOT_PROCESS_INSTANCE_ID = "ROOT_PROCESS_INSTANCE_ID";
    private static final String ROOT_PROCESS_ID = "ROOT_PROCESS_ID";
    private static final String PROCESS_PARENT_PROCESS_INSTANCE_ID = "PROCESS_PARENT_PROCESS_INSTANCE_ID";
    private static final String PROCESS_INSTANCE_STATE = "PROCESS_INSTANCE_STATE";
    private static final String PROCESS_REFERENCE_ID = "PROCESS_REFERENCE_ID";
    private static final String PROCESS_START_FROM_NODE = "PROCESS_START_FROM_NODE";
    private static final String BUSINESS_KEY = "BUSINESS_KEY";
    private static final String PROCESS_TYPE = "PROCESS_TYPE";
    private static final String ADDONS = "ADDONS";

    private static final String EXTENSION_1 = "EXTENSION_1";
    private static final String EXTENSION_1_VALUE = "EXTENSION_1_VALUE";
    private static final String EXTENSION_2 = "EXTENSION_2";
    private static final String EXTENSION_2_VALUE = "EXTENSION_2_VALUE";

    private static final String VARIABLE_NAME = "VARIABLE_NAME";

    private static final String PROCESS_USER_TASK_INSTANCE_ID = "PROCESS_USER_TASK_INSTANCE_ID";
    private static final String PROCESS_USER_TASK_INSTANCE_STATE = "PROCESS_USER_TASK_INSTANCE_STATE";

    @Test
    void processInstanceDataEvent() throws Exception {
        ProcessInstanceDataEvent event = new ProcessInstanceDataEvent();
        setBaseEventValues(event, PROCESS_INSTANCE_EVENT_TYPE);
        setAdditionalExtensions(event);

        assertExtensionNames(event, BASE_EXTENSION_NAMES, EXTENSION_1, EXTENSION_2);

        String json = OBJECT_MAPPER.writeValueAsString(event);
        assertExtensionsNotDuplicated(json, event.getExtensionNames());

        ProcessInstanceDataEvent deserializedEvent = OBJECT_MAPPER.readValue(json, ProcessInstanceDataEvent.class);

        assertBaseEventValues(deserializedEvent, PROCESS_INSTANCE_EVENT_TYPE);
        assertThat(deserializedEvent.getExtension(EXTENSION_1)).isEqualTo(EXTENSION_1_VALUE);
        assertThat(deserializedEvent.getExtension(EXTENSION_2)).isEqualTo(EXTENSION_2_VALUE);
        assertExtensionNames(deserializedEvent, BASE_EXTENSION_NAMES, EXTENSION_1, EXTENSION_2);
    }

    @Test
    void userTaskInstanceDataEvent() throws Exception {
        UserTaskInstanceDataEvent event = new UserTaskInstanceDataEvent();
        setBaseEventValues(event, USER_TASK_INSTANCE_EVENT_TYPE);
        event.addExtensionAttribute(CloudEventExtensionConstants.PROCESS_USER_TASK_INSTANCE_ID, PROCESS_USER_TASK_INSTANCE_ID);
        event.addExtensionAttribute(CloudEventExtensionConstants.PROCESS_USER_TASK_INSTANCE_STATE, PROCESS_USER_TASK_INSTANCE_STATE);
        setAdditionalExtensions(event);

        assertExtensionNames(event, BASE_EXTENSION_NAMES,
                CloudEventExtensionConstants.PROCESS_USER_TASK_INSTANCE_ID, CloudEventExtensionConstants.PROCESS_USER_TASK_INSTANCE_STATE,
                EXTENSION_1, EXTENSION_2);

        String json = OBJECT_MAPPER.writeValueAsString(event);
        assertExtensionsNotDuplicated(json, event.getExtensionNames());

        UserTaskInstanceDataEvent deserializedEvent = OBJECT_MAPPER.readValue(json, UserTaskInstanceDataEvent.class);
        assertBaseEventValues(deserializedEvent, USER_TASK_INSTANCE_EVENT_TYPE);
        assertThat(deserializedEvent.getExtension(EXTENSION_1)).isEqualTo(EXTENSION_1_VALUE);
        assertThat(deserializedEvent.getExtension(EXTENSION_2)).isEqualTo(EXTENSION_2_VALUE);
        assertThat(deserializedEvent.getExtension(CloudEventExtensionConstants.PROCESS_USER_TASK_INSTANCE_ID)).isEqualTo(PROCESS_USER_TASK_INSTANCE_ID);
        assertThat(deserializedEvent.getExtension(CloudEventExtensionConstants.PROCESS_USER_TASK_INSTANCE_STATE)).isEqualTo(PROCESS_USER_TASK_INSTANCE_STATE);
        assertExtensionNames(deserializedEvent, BASE_EXTENSION_NAMES,
                CloudEventExtensionConstants.PROCESS_USER_TASK_INSTANCE_ID, CloudEventExtensionConstants.PROCESS_USER_TASK_INSTANCE_STATE,
                EXTENSION_1, EXTENSION_2);

    }

    @Test
    void variableInstanceDataEvent() throws Exception {
        VariableInstanceDataEvent event = new VariableInstanceDataEvent();
        setBaseEventValues(event, VARIABLE_INSTANCE_EVENT_TYPE);
        event.addExtensionAttribute(CloudEventExtensionConstants.KOGITO_VARIABLE_NAME, VARIABLE_NAME);
        setAdditionalExtensions(event);

        assertExtensionNames(event, BASE_EXTENSION_NAMES, CloudEventExtensionConstants.KOGITO_VARIABLE_NAME, EXTENSION_1, EXTENSION_2);

        String json = OBJECT_MAPPER.writeValueAsString(event);
        assertExtensionsNotDuplicated(json, event.getExtensionNames());

        VariableInstanceDataEvent deserializedEvent = OBJECT_MAPPER.readValue(json, VariableInstanceDataEvent.class);
        assertBaseEventValues(deserializedEvent, VARIABLE_INSTANCE_EVENT_TYPE);
        assertThat(deserializedEvent.getExtension(EXTENSION_1)).isEqualTo(EXTENSION_1_VALUE);
        assertThat(deserializedEvent.getExtension(EXTENSION_2)).isEqualTo(EXTENSION_2_VALUE);
        assertThat(deserializedEvent.getExtension(CloudEventExtensionConstants.KOGITO_VARIABLE_NAME)).isEqualTo(VARIABLE_NAME);
        assertExtensionNames(event, BASE_EXTENSION_NAMES, CloudEventExtensionConstants.KOGITO_VARIABLE_NAME, EXTENSION_1, EXTENSION_2);
    }

    private static void setBaseEventValues(AbstractDataEvent<?> event, String eventType) {
        event.setType(eventType);
        event.setId(ID);
        event.setSpecVersion(SPEC_VERSION);
        event.setSource(SOURCE);
        event.setTime(TIME);
        event.setSubject(SUBJECT);
        event.setDataContentType(DATA_CONTENT_TYPE);
        event.setDataSchema(DATA_SCHEMA);

        event.setKogitoProcessInstanceId(PROCESS_INSTANCE_ID);
        event.setKogitoProcessInstanceVersion(PROCESS_INSTANCE_VERSION);
        event.setKogitoProcessId(PROCESS_ID);
        event.setKogitoRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID);
        event.setKogitoRootProcessId(ROOT_PROCESS_ID);
        event.setKogitoParentProcessInstanceId(PROCESS_PARENT_PROCESS_INSTANCE_ID);
        event.setKogitoReferenceId(PROCESS_REFERENCE_ID);
        event.setKogitoProcessInstanceState(PROCESS_INSTANCE_STATE);
        event.setKogitoStartFromNode(PROCESS_START_FROM_NODE);
        event.setKogitoBusinessKey(BUSINESS_KEY);
        event.setKogitoProcessType(PROCESS_TYPE);
        event.setKogitoAddons(ADDONS);
    }

    private static void setAdditionalExtensions(AbstractDataEvent<?> event) {
        event.addExtensionAttribute(EXTENSION_1, EXTENSION_1_VALUE);
        event.addExtensionAttribute(EXTENSION_2, EXTENSION_2_VALUE);
    }

    private static void assertBaseEventValues(AbstractDataEvent<?> deserializedEvent, String eventType) {
        assertThat(deserializedEvent.getType()).isEqualTo(eventType);
        assertThat(deserializedEvent.getId()).isEqualTo(ID);
        assertThat(deserializedEvent.getSpecVersion()).isEqualTo(SPEC_VERSION);
        assertThat(deserializedEvent.getSource()).isEqualTo(SOURCE);
        assertThat(deserializedEvent.getTime()).isEqualTo(TIME);
        assertThat(deserializedEvent.getSubject()).isEqualTo(SUBJECT);
        assertThat(deserializedEvent.getDataContentType()).isEqualTo(DATA_CONTENT_TYPE);
        assertThat(deserializedEvent.getDataSchema()).isEqualTo(DATA_SCHEMA);

        assertThat(deserializedEvent.getKogitoProcessInstanceId()).isEqualTo(PROCESS_INSTANCE_ID);
        assertThat(deserializedEvent.getKogitoProcessId()).isEqualTo(PROCESS_ID);
        assertThat(deserializedEvent.getKogitoRootProcessInstanceId()).isEqualTo(ROOT_PROCESS_INSTANCE_ID);
        assertThat(deserializedEvent.getKogitoRootProcessId()).isEqualTo(ROOT_PROCESS_ID);
        assertThat(deserializedEvent.getKogitoParentProcessInstanceId()).isEqualTo(PROCESS_PARENT_PROCESS_INSTANCE_ID);
        assertThat(deserializedEvent.getKogitoReferenceId()).isEqualTo(PROCESS_REFERENCE_ID);
        assertThat(deserializedEvent.getKogitoProcessInstanceState()).isEqualTo(PROCESS_INSTANCE_STATE);
        assertThat(deserializedEvent.getKogitoStartFromNode()).isEqualTo(PROCESS_START_FROM_NODE);
        assertThat(deserializedEvent.getKogitoBusinessKey()).isEqualTo(BUSINESS_KEY);
        assertThat(deserializedEvent.getKogitoProcessType()).isEqualTo(PROCESS_TYPE);
        assertThat(deserializedEvent.getKogitoAddons()).isEqualTo(ADDONS);
    }

    private static void assertExtensionNames(AbstractDataEvent<?> event, Set<String> baseNames, String... names) {
        Set<String> extensionNames = event.getExtensionNames();
        assertThat(extensionNames).hasSize(baseNames.size() + names.length)
                .containsAll(baseNames)
                .contains(names);
    }

    private static void assertExtensionsNotDuplicated(String json, Set<String> extensionNames) {
        extensionNames.forEach(name -> assertOnlyOneTime(json, name));
    }

    private static void assertOnlyOneTime(String json, String propertyName) {
        int count = json.split("\"" + propertyName + "\"").length - 1;
        assertThat(count)
                .withFailMessage("It looks like the extension: %s is duplicated in json: %s", propertyName, json)
                .isEqualTo(1);
    }
}
