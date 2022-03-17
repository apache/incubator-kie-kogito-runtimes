package org.kie.kogito.services.event.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.kie.kogito.event.EventMarshaller;

import com.fasterxml.jackson.databind.ObjectMapper;

class StringEventMarshallerTest {

    private EventMarshaller<String> marshaller = new StringEventMarshaller(new ObjectMapper());;

    @Test
    void testDataMarshaller() {
        DummyEvent dataEvent = new DummyEvent("pepe");
        assertEquals(
                "{\"dummyField\":\"pepe\"}",
                marshaller.marshall(dataEvent));
    }

    @Test
    void testEventMarshaller() {
        DummyEvent dataEvent = new DummyEvent("pepe");
        String jsonString = marshaller.marshall(dataEvent);
        assertTrue(jsonString.contains("\"dummyField\":\"pepe\""));
    }
}