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
import org.kie.kogito.event.process.ProcessInstanceStateDataEvent;
import org.kie.kogito.event.process.ProcessInstanceStateEventBody;

import static org.kie.kogito.events.mongodb.codec.CodecUtils.codec;
import static org.kie.kogito.events.mongodb.codec.CodecUtils.encodeDataEvent;

public class ProcessInstanceDataEventCodec implements CollectibleCodec<ProcessInstanceStateDataEvent> {

    @Override
    public ProcessInstanceStateDataEvent generateIdIfAbsentFromDocument(ProcessInstanceStateDataEvent processInstanceDataEvent) {
        return processInstanceDataEvent;
    }

    @Override
    public boolean documentHasId(ProcessInstanceStateDataEvent processInstanceDataEvent) {
        return processInstanceDataEvent.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(ProcessInstanceStateDataEvent processInstanceDataEvent) {
        return new BsonString(processInstanceDataEvent.getId());
    }

    @Override
    public ProcessInstanceStateDataEvent decode(BsonReader bsonReader, DecoderContext decoderContext) {
        // The events persist in an outbox collection
        // The events are deleted immediately (in the same transaction)
        // "decode" is not supposed to take place in any scenario
        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, ProcessInstanceStateDataEvent processInstanceDataEvent, EncoderContext encoderContext) {
        Document doc = new Document();
        encodeDataEvent(doc, processInstanceDataEvent);
        doc.put("kogitoProcessType", processInstanceDataEvent.getKogitoProcessType());
        doc.put("kogitoProcessInstanceVersion", processInstanceDataEvent.getKogitoProcessInstanceVersion());
        doc.put("kogitoParentProcessinstanceId", processInstanceDataEvent.getKogitoParentProcessInstanceId());
        doc.put("kogitoProcessinstanceState", processInstanceDataEvent.getKogitoProcessInstanceState());
        doc.put("kogitoReferenceId", processInstanceDataEvent.getKogitoReferenceId());
        doc.put("kogitoStartFromNode", processInstanceDataEvent.getKogitoStartFromNode());
        doc.put("kogitoIdentity", processInstanceDataEvent.getKogitoIdentity());
        doc.put("data", encodeData(processInstanceDataEvent.getData()));
        codec().encode(bsonWriter, doc, encoderContext);
    }

    private Document encodeData(ProcessInstanceStateEventBody data) {
        Document doc = new Document();
        doc.put("id", data.getProcessInstanceId());
        doc.put("version", data.getProcessVersion());
        doc.put("parentInstanceId", data.getParentInstanceId());
        doc.put("rootInstanceId", data.getRootProcessInstanceId());
        doc.put("processId", data.getProcessId());
        doc.put("processType", data.getProcessType());
        doc.put("rootProcessId", data.getRootProcessId());
        doc.put("processName", data.getProcessName());
        doc.put("eventDate", data.getEventDate());
        doc.put("state", data.getState());
        doc.put("businessKey", data.getBusinessKey());
        doc.put("roles", data.getRoles());
        doc.put("identity", data.getEventUser());

        return doc;
    }

    @Override
    public Class<ProcessInstanceStateDataEvent> getEncoderClass() {
        return ProcessInstanceStateDataEvent.class;
    }
}
