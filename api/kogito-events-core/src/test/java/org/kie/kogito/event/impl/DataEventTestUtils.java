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

import org.kie.kogito.event.CloudEventMarshaller;
import org.kie.kogito.event.CloudEventUnmarshaller;
import org.kie.kogito.event.CloudEventUnmarshallerFactory;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.DataEventFactory;
import org.kie.kogito.event.EventMarshaller;
import org.kie.kogito.event.EventUnmarshaller;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataEventTestUtils {

    public static <T> void assertDataEventEquals(DataEvent<T> event, DataEvent<T> targetEvent) {
        assertEquals(event.getId(), targetEvent.getId());
        assertEquals(event.getType(), targetEvent.getType());
        assertEquals(event.getSource(), targetEvent.getSource());
        assertEquals(event.getSpecVersion(), targetEvent.getSpecVersion());
        assertEquals(event.getSubject(), targetEvent.getSubject());
        assertEquals(event.getDataContentType(), targetEvent.getDataContentType());
        assertEquals(event.getDataSchema(), targetEvent.getDataSchema());
        assertEquals(event.getExtensionNames(), targetEvent.getExtensionNames());
        for (String extensionName : event.getExtensionNames()) {
            assertEquals(event.getExtension(extensionName), targetEvent.getExtension(extensionName));
        }
        assertEquals(event.getTime(), targetEvent.getTime());
        assertEquals(event.getData(), targetEvent.getData());
    }

    public static <T, V> void testCloudEventMarshalling(DataEvent<V> event, Class<V> clazz, CloudEventMarshaller<T> marshaller, CloudEventUnmarshallerFactory<T> unmarshallerFactory)
            throws IOException {
        CloudEventUnmarshaller<T, V> unmarshaller = unmarshallerFactory.unmarshaller(clazz);
        DataEvent<V> targetEvent =
                DataEventFactory.from(unmarshaller.cloudEvent().convert(marshaller.marshall(event.asCloudEvent(marshaller.cloudEventDataFactory()))), unmarshaller.data());
        assertDataEventEquals(event, targetEvent);
    }

    public static <T> void testEventMarshalling(Object event, EventMarshaller<T> marshaller, EventUnmarshaller<T> unmarshaller) throws IOException {
        assertEquals(event, unmarshaller.unmarshall(marshaller.marshall(event), event.getClass()));
    }
}
