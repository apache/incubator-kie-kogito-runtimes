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
import org.kie.kogito.event.process.ProcessInstanceNodeDataEvent;
import org.kie.kogito.event.process.ProcessInstanceNodeEventBody;

import static org.kie.kogito.events.mongodb.codec.CodecUtils.codec;
import static org.kie.kogito.events.mongodb.codec.CodecUtils.encodeDataEvent;

public class NodeInstanceDataEventCodec implements CollectibleCodec<ProcessInstanceNodeDataEvent> {

    @Override
    public ProcessInstanceNodeDataEvent generateIdIfAbsentFromDocument(ProcessInstanceNodeDataEvent nodeInstanceDataEvent) {
        return nodeInstanceDataEvent;
    }

    @Override
    public boolean documentHasId(ProcessInstanceNodeDataEvent processInstanceDataEvent) {
        return processInstanceDataEvent.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(ProcessInstanceNodeDataEvent processInstanceDataEvent) {
        return new BsonString(processInstanceDataEvent.getId());
    }

    @Override
    public ProcessInstanceNodeDataEvent decode(BsonReader bsonReader, DecoderContext decoderContext) {
        // The events persist in an outbox collection
        // The events are deleted immediately (in the same transaction)
        // "decode" is not supposed to take place in any scenario
        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, ProcessInstanceNodeDataEvent nodeInstanceDataEvent, EncoderContext encoderContext) {
        Document doc = new Document();
        encodeDataEvent(doc, nodeInstanceDataEvent);
        doc.put("kogitoProcessType", nodeInstanceDataEvent.getKogitoProcessType());
        doc.put("kogitoProcessInstanceVersion", nodeInstanceDataEvent.getKogitoProcessInstanceVersion());
        doc.put("kogitoParentProcessinstanceId", nodeInstanceDataEvent.getKogitoParentProcessInstanceId());
        doc.put("kogitoProcessinstanceState", nodeInstanceDataEvent.getKogitoProcessInstanceState());
        doc.put("kogitoReferenceId", nodeInstanceDataEvent.getKogitoReferenceId());
        doc.put("kogitoStartFromNode", nodeInstanceDataEvent.getKogitoStartFromNode());
        doc.put("data", encodeData(nodeInstanceDataEvent.getData()));
        codec().encode(bsonWriter, doc, encoderContext);
    }

    private Document encodeData(ProcessInstanceNodeEventBody data) {

        Document doc = new Document();
        doc.put("processInstanceId", data.getProcessInstanceId());
        doc.put("connectionNodeInstanceId", data.getConnectionNodeInstanceId());
        doc.put("id", data.getNodeInstanceId());
        doc.put("nodeId", data.getNodeDefinitionId());
        doc.put("nodeDefinitionId", data.getNodeDefinitionId());
        doc.put("nodeName", data.getNodeName());
        doc.put("nodeType", data.getNodeType());
        doc.put("eventTime", data.getEventDate());
        doc.put("eventType", data.getEventType());

        if (!data.getData().isEmpty()) {
            doc.put("data", new Document(data.getData()));
        }

        return doc;
    }

    @Override
    public Class<ProcessInstanceNodeDataEvent> getEncoderClass() {
        return ProcessInstanceNodeDataEvent.class;
    }
}
