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
package org.kie.kogito.event.serializer;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.kie.kogito.event.process.CloudEventVisitor;
import org.kie.kogito.event.process.KogitoEventBodySerializationHelper;
import org.kie.kogito.event.process.KogitoMarshallEventSupport;
import org.kie.kogito.event.process.MultipleProcessInstanceDataEvent;
import org.kie.kogito.event.process.ProcessInstanceDataEvent;
import org.kie.kogito.event.process.ProcessInstanceErrorDataEvent;
import org.kie.kogito.event.process.ProcessInstanceErrorEventBody;
import org.kie.kogito.event.process.ProcessInstanceNodeDataEvent;
import org.kie.kogito.event.process.ProcessInstanceNodeEventBody;
import org.kie.kogito.event.process.ProcessInstanceSLADataEvent;
import org.kie.kogito.event.process.ProcessInstanceSLAEventBody;
import org.kie.kogito.event.process.ProcessInstanceStateDataEvent;
import org.kie.kogito.event.process.ProcessInstanceStateEventBody;
import org.kie.kogito.event.process.ProcessInstanceVariableDataEvent;
import org.kie.kogito.event.process.ProcessInstanceVariableEventBody;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;

import io.cloudevents.SpecVersion;

public class MultipleProcessInstanceDataEventDeserializer extends JsonDeserializer<MultipleProcessInstanceDataEvent> implements ResolvableDeserializer {

    private JsonDeserializer<Object> defaultDeserializer;

    public MultipleProcessInstanceDataEventDeserializer(JsonDeserializer<Object> deserializer) {
        this.defaultDeserializer = deserializer;
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
    }

    @Override
    public MultipleProcessInstanceDataEvent deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode dataContentType = node.get("datacontenttype");
        if (dataContentType != null && MultipleProcessInstanceDataEvent.BINARY_CONTENT_TYPE.equals(dataContentType.asText())) {
            MultipleProcessInstanceDataEvent event = new MultipleProcessInstanceDataEvent();
            event.setDataContentType(dataContentType.asText());
            event.setSource(URI.create(node.get("source").asText()));
            event.setType(node.get("type").asText());
            event.setSpecVersion(SpecVersion.parse(node.get("specversion").asText()));
            event.setId(node.get("id").asText());
            JsonNode data = node.get("data");
            if (data != null) {
                event.setData(readFromBytes(data.binaryValue(), isCompressed(node)));
            }
            return event;
        } else {
            JsonParser newParser = node.traverse(p.getCodec());
            newParser.nextToken();
            return (MultipleProcessInstanceDataEvent) defaultDeserializer.deserialize(newParser, ctxt);
        }
    }

    private static boolean isCompressed(JsonNode node) {
        JsonNode compress = node.get(MultipleProcessInstanceDataEvent.COMPRESS_DATA);
        return compress != null && compress.isBoolean() ? compress.asBoolean() : false;
    }

    public static Collection<ProcessInstanceDataEvent<? extends KogitoMarshallEventSupport>> readFromBytes(byte[] binaryValue, boolean compressed) throws IOException {
        InputStream wrappedIn = new ByteArrayInputStream(binaryValue);
        if (compressed) {
            wrappedIn = new GZIPInputStream(wrappedIn);
        }
        try (DataInputStream in = new DataInputStream(wrappedIn)) {
            int size = in.readShort();
            Collection<ProcessInstanceDataEvent<? extends KogitoMarshallEventSupport>> result = new ArrayList<>(size);
            List<ProcessInstanceDataEventExtensionRecord> infos = new ArrayList<>();
            while (size-- > 0) {
                byte readInfo = in.readByte();
                ProcessInstanceDataEventExtensionRecord info;
                if (readInfo == -1) {
                    info = new ProcessInstanceDataEventExtensionRecord();
                    info.readEvent(in);
                    infos.add(info);
                } else {
                    info = infos.get(readInfo);
                }
                String type = in.readUTF();
                result.add(getCloudEvent(in, type, info));
            }
            return result;
        }
    }

    private static ProcessInstanceDataEvent<? extends KogitoMarshallEventSupport> getCloudEvent(DataInputStream in, String type, ProcessInstanceDataEventExtensionRecord info) throws IOException {
        switch (type) {
            case ProcessInstanceVariableDataEvent.VAR_TYPE:
                ProcessInstanceVariableDataEvent item = buildDataEvent(in, new ProcessInstanceVariableDataEvent(), new ProcessInstanceVariableEventBody(), info);
                item.setKogitoVariableName(item.getData().getVariableName());
                return item;
            case ProcessInstanceStateDataEvent.STATE_TYPE:
                return buildDataEvent(in, new ProcessInstanceStateDataEvent(), new ProcessInstanceStateEventBody(), info);
            case ProcessInstanceNodeDataEvent.NODE_TYPE:
                return buildDataEvent(in, new ProcessInstanceNodeDataEvent(), new ProcessInstanceNodeEventBody(), info);
            case ProcessInstanceErrorDataEvent.ERROR_TYPE:
                return buildDataEvent(in, new ProcessInstanceErrorDataEvent(), new ProcessInstanceErrorEventBody(), info);
            case ProcessInstanceSLADataEvent.SLA_TYPE:
                return buildDataEvent(in, new ProcessInstanceSLADataEvent(), new ProcessInstanceSLAEventBody(), info);
            default:
                throw new UnsupportedOperationException("Unrecognized event type " + type);
        }
    }

    private static <T extends ProcessInstanceDataEvent<V>, V extends KogitoMarshallEventSupport & CloudEventVisitor> T buildDataEvent(DataInput in, T cloudEvent, V body,
            ProcessInstanceDataEventExtensionRecord info) throws IOException {
        int delta = KogitoEventBodySerializationHelper.readInteger(in);
        cloudEvent.setTime(info.getTime().plus(delta, ChronoUnit.MILLIS));
        KogitoDataEventSerializationHelper.readCloudEventAttrs(in, cloudEvent);
        KogitoDataEventSerializationHelper.populateCloudEvent(cloudEvent, info);
        body.readEvent(in);
        body.visit(cloudEvent);
        cloudEvent.setData(body);
        return cloudEvent;
    }

}
