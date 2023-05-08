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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.event.process.ProcessInstanceEventMetadata;
import org.kie.kogito.event.usertask.UserTaskInstanceEventMetadata;
import org.kie.kogito.event.usertask.UserTaskInstanceStateDataEvent;
import org.kie.kogito.event.usertask.UserTaskInstanceStateEventBody;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.events.mongodb.codec.CodecUtils.ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UserTaskInstanceDataEventCodecTest {

    private UserTaskInstanceDataEventCodec codec;

    private UserTaskInstanceStateDataEvent event;

    @BeforeEach
    void setUp() {
        codec = new UserTaskInstanceDataEventCodec();

        String source = "testSource";
        String kogitoAddons = "testKogitoAddons";
        String identity = "testIdentity";

        Map<String, Object> metaData = new HashMap<>();
        metaData.put(ProcessInstanceEventMetadata.PROCESS_INSTANCE_ID_META_DATA, "testKogitoProcessInstanceId");
        metaData.put(ProcessInstanceEventMetadata.PROCESS_VERSION_META_DATA, "testKogitoProcessInstanceVersion");
        metaData.put(ProcessInstanceEventMetadata.ROOT_PROCESS_INSTANCE_ID_META_DATA, "testKogitoRootProcessInstanceId");
        metaData.put(ProcessInstanceEventMetadata.PROCESS_ID_META_DATA, "testKogitoProcessId");
        metaData.put(ProcessInstanceEventMetadata.ROOT_PROCESS_ID_META_DATA, "testKogitoRootProcessId");
        metaData.put(UserTaskInstanceEventMetadata.USER_TASK_INSTANCE_STATE_META_DATA, "testKogitoUserTaskInstanceState");
        metaData.put(UserTaskInstanceEventMetadata.USER_TASK_INSTANCE_ID_META_DATA, "testKogitoUserTaskInstanceId");

        UserTaskInstanceStateEventBody body = UserTaskInstanceStateEventBody.create()
                .userTaskInstanceId("testId")
                .userTaskName("testTaskName")
                .userTaskDescription("testTaskDescription")
                .userTaskPriority("testTaskPriority")
                .userTaskReferenceName("testReferenceName")
                .eventDate(new Date())
                .state("testState")
                .actualOwner("testActualOwner")
                .processInstanceId("testKogitoProcessInstanceId")
                .eventUser(identity)
                .build();

        event = new UserTaskInstanceStateDataEvent(source, kogitoAddons, identity, metaData, body);
    }

    @Test
    void generateIdIfAbsentFromDocument() {
        assertThat(codec.generateIdIfAbsentFromDocument(event)).isEqualTo(event);
    }

    @Test
    void documentHasId() {
        assertThat(codec.documentHasId(event)).isTrue();
    }

    @Test
    void getDocumentId() {
        assertThat(codec.getDocumentId(event)).isEqualTo(new BsonString(event.getId()));
    }

    @Test
    void decode() {
        assertThat(codec.decode(mock(BsonReader.class), DecoderContext.builder().build())).isNull();
    }

    @Test
    void encode() {
        try (MockedStatic<CodecUtils> codecUtils = mockStatic(CodecUtils.class)) {
            Codec<Document> mockCodec = mock(Codec.class);
            codecUtils.when(CodecUtils::codec).thenReturn(mockCodec);
            codecUtils.when(() -> CodecUtils.encodeDataEvent(any(), any())).thenCallRealMethod();
            BsonWriter writer = mock(BsonWriter.class);
            EncoderContext context = EncoderContext.builder().build();

            codec.encode(writer, event, context);

            ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
            verify(mockCodec, times(1)).encode(eq(writer), captor.capture(), eq(context));
            Document doc = captor.getValue();

            assertThat(doc).containsEntry(ID, event.getId())
                    .containsEntry("specversion", event.getSpecVersion().toString())
                    .containsEntry("source", event.getSource().toString())
                    .containsEntry("type", event.getType())
                    .containsEntry("time", event.getTime())
                    .containsEntry("subject", event.getSubject())
                    .containsEntry("dataContentType", event.getDataContentType())
                    .containsEntry("dataSchema", event.getDataSchema())
                    .containsEntry("kogitoProcessinstanceId", event.getKogitoProcessInstanceId())
                    .containsEntry("kogitoRootProcessinstanceId", event.getKogitoRootProcessInstanceId())
                    .containsEntry("kogitoProcessId", event.getKogitoProcessId())
                    .containsEntry("kogitoRootProcessId", event.getKogitoRootProcessId())
                    .containsEntry("kogitoAddons", event.getKogitoAddons())
                    .containsEntry("kogitoUserTaskinstanceId", event.getKogitoUserTaskInstanceId())
                    .containsEntry("kogitoUserTaskinstanceState", event.getKogitoUserTaskInstanceState())
                    .containsEntry("kogitoIdentity", event.getKogitoIdentity());

            assertThat(((Document) doc.get("data"))).containsEntry("id", event.getData().getUserTaskInstanceId())
                    .containsEntry("taskName", event.getData().getUserTaskName())
                    .containsEntry("taskDescription", event.getData().getUserTaskDescription())
                    .containsEntry("taskPriority", event.getData().getUserTaskPriority())
                    .containsEntry("referenceName", event.getData().getUserTaskReferenceName())
                    .containsEntry("eventDate", event.getData().getEventDate())
                    .containsEntry("state", event.getData().getState())
                    .containsEntry("actualOwner", event.getData().getActualOwner())
                    .containsEntry("processInstanceId", event.getData().getProcessInstanceId())
                    .containsEntry("identity", event.getData().getEventUser());

        }
    }

    @Test
    void getEncoderClass() {
        assertThat(codec.getEncoderClass()).isEqualTo(UserTaskInstanceStateDataEvent.class);
    }
}
