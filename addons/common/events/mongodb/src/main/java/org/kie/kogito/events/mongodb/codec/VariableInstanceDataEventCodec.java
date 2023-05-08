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
package org.kie.kogito.events.mongodb.codec;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.kie.kogito.event.process.ProcessInstanceVariableDataEvent;
import org.kie.kogito.event.process.ProcessInstanceVariableEventBody;

import static org.kie.kogito.events.mongodb.codec.CodecUtils.codec;
import static org.kie.kogito.events.mongodb.codec.CodecUtils.encodeDataEvent;

public class VariableInstanceDataEventCodec implements CollectibleCodec<ProcessInstanceVariableDataEvent> {

    @Override
    public ProcessInstanceVariableDataEvent generateIdIfAbsentFromDocument(ProcessInstanceVariableDataEvent variableInstanceDataEvent) {
        return variableInstanceDataEvent;
    }

    @Override
    public boolean documentHasId(ProcessInstanceVariableDataEvent variableInstanceDataEvent) {
        return variableInstanceDataEvent.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(ProcessInstanceVariableDataEvent variableInstanceDataEvent) {
        return new BsonString(variableInstanceDataEvent.getId());
    }

    @Override
    public ProcessInstanceVariableDataEvent decode(BsonReader bsonReader, DecoderContext decoderContext) {
        // The events persist in an outbox collection
        // The events are deleted immediately (in the same transaction)
        // "decode" is not supposed to take place in any scenario
        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, ProcessInstanceVariableDataEvent variableInstanceDataEvent, EncoderContext encoderContext) {
        Document doc = new Document();
        encodeDataEvent(doc, variableInstanceDataEvent);
        doc.put("kogitoVariableName", variableInstanceDataEvent.getKogitoVariableName());
        doc.put("data", encodeData(variableInstanceDataEvent.getData()));
        codec().encode(bsonWriter, doc, encoderContext);
    }

    private Document encodeData(ProcessInstanceVariableEventBody data) {
        Document doc = new Document();
        doc.put("variableName", data.getVariableName());
        doc.put("variableValue", data.getVariableValue());
        doc.put("eventDate", data.getEventDate());
        doc.put("processInstanceId", data.getProcessInstanceId());
        doc.put("processId", data.getProcessId());
        doc.put("identity", data.getEventUser());
        return doc;
    }

    @Override
    public Class<ProcessInstanceVariableDataEvent> getEncoderClass() {
        return ProcessInstanceVariableDataEvent.class;
    }
}
