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
import org.kie.kogito.event.usertask.UserTaskInstanceStateDataEvent;
import org.kie.kogito.event.usertask.UserTaskInstanceStateEventBody;

import static org.kie.kogito.events.mongodb.codec.CodecUtils.codec;
import static org.kie.kogito.events.mongodb.codec.CodecUtils.encodeDataEvent;

public class UserTaskInstanceDataEventCodec implements CollectibleCodec<UserTaskInstanceStateDataEvent> {

    @Override
    public UserTaskInstanceStateDataEvent generateIdIfAbsentFromDocument(UserTaskInstanceStateDataEvent userTaskInstanceDataEvent) {
        return userTaskInstanceDataEvent;
    }

    @Override
    public boolean documentHasId(UserTaskInstanceStateDataEvent userTaskInstanceDataEvent) {
        return userTaskInstanceDataEvent.getId() != null;
    }

    @Override
    public BsonValue getDocumentId(UserTaskInstanceStateDataEvent userTaskInstanceDataEvent) {
        return new BsonString(userTaskInstanceDataEvent.getId());
    }

    @Override
    public UserTaskInstanceStateDataEvent decode(BsonReader bsonReader, DecoderContext decoderContext) {
        // The events persist in an outbox collection
        // The events are deleted immediately (in the same transaction)
        // "decode" is not supposed to take place in any scenario
        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, UserTaskInstanceStateDataEvent userTaskInstanceDataEvent, EncoderContext encoderContext) {
        Document doc = new Document();
        encodeDataEvent(doc, userTaskInstanceDataEvent);
        doc.put("kogitoUserTaskinstanceId", userTaskInstanceDataEvent.getKogitoUserTaskInstanceId());
        doc.put("kogitoUserTaskinstanceState", userTaskInstanceDataEvent.getKogitoUserTaskInstanceState());
        doc.put("data", encodeData(userTaskInstanceDataEvent.getData()));
        codec().encode(bsonWriter, doc, encoderContext);
    }

    private Document encodeData(UserTaskInstanceStateEventBody data) {
        Document doc = new Document();
        doc.put("id", data.getUserTaskInstanceId());
        doc.put("taskName", data.getUserTaskName());
        doc.put("taskDescription", data.getUserTaskDescription());
        doc.put("taskPriority", data.getUserTaskPriority());
        doc.put("referenceName", data.getUserTaskReferenceName());
        doc.put("eventDate", data.getEventDate());
        doc.put("state", data.getState());
        doc.put("actualOwner", data.getActualOwner());
        doc.put("processInstanceId", data.getProcessInstanceId());
        doc.put("identity", data.getEventUser());
        return doc;
    }

    @Override
    public Class<UserTaskInstanceStateDataEvent> getEncoderClass() {
        return UserTaskInstanceStateDataEvent.class;
    }
}
