/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.event.impl;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.event.DummyCloudEvent;
import org.kie.kogito.event.DummyEvent;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.jackson.JsonFormat;

import static org.kie.kogito.event.impl.DataEventTestUtils.testCloudEventMarshalling;

class CloudEventMarshallUnmarshallTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void init() {
        mapper = ObjectMapperFactory.get().registerModule(JsonFormat.getCloudEventJacksonModule());
    }

    @Test
    void testStringMarshaller() throws IOException {
        testCloudEventMarshalling(new DummyCloudEvent(new DummyEvent("pepe"), "pepa"), DummyEvent.class, new StringCloudEventMarshaller(mapper), new StringCloudEventUnmarshallerFactory(mapper));
    }

    @Test
    void testObjectMarshaller() throws IOException {
        testCloudEventMarshalling(new DummyCloudEvent(new DummyEvent("pepe"), "pepa"), DummyEvent.class, new NoOpCloudEventMarshaller(mapper), new ObjectCloudEventUnmarshallerFactory(mapper));
    }

    @Test
    void testByteArrayMarshaller() throws IOException {
        testCloudEventMarshalling(new DummyCloudEvent(new DummyEvent("pepe"), "pepa"), DummyEvent.class, new ByteArrayCloudEventMarshaller(mapper), new ByteArrayCloudEventUnmarshallerFactory(mapper));
    }
}
