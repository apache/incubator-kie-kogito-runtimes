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
package org.kie.kogito.event.avro;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.event.DummyCloudEvent;
import org.kie.kogito.event.DummyEvent;

import static org.kie.kogito.event.impl.DataEventTestUtils.testCloudEventMarshalling;
import static org.kie.kogito.event.impl.DataEventTestUtils.testEventMarshalling;

class CloudEventMarshallUnmarshallTest {

    private static AvroUtils avroUtils;

    @BeforeAll
    static void init() throws IOException {
        avroUtils = new AvroUtils();
    }

    @Test
    void testCloudEventMarshaller() throws IOException {
        testCloudEventMarshalling(new DummyCloudEvent(new DummyEvent("pepe"), "pepa"), DummyEvent.class, new AvroCloudEventMarshaller(avroUtils), new AvroCloudEventUnmarshallerFactory(avroUtils));
    }

    @Test
    void testEventMarshaller() throws IOException {
        testEventMarshalling(new DummyEvent("pepe"), new AvroEventMarshaller(avroUtils), new AvroEventUnmarshaller(avroUtils));
    }
}
