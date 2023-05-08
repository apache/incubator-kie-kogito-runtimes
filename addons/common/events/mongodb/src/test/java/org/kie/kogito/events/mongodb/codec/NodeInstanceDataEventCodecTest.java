/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
import org.kie.kogito.event.process.ProcessInstanceNodeDataEvent;
import org.kie.kogito.event.process.ProcessInstanceNodeEventBody;
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

class NodeInstanceDataEventCodecTest {

    private NodeInstanceDataEventCodec codec;

    private ProcessInstanceNodeDataEvent event;

    @BeforeEach
    void setUp() {
        codec = new NodeInstanceDataEventCodec();

        String source = "testSource";
        String kogitoAddons = "testKogitoAddons";

        Map<String, Object> metaData = new HashMap<>();
        metaData.put(ProcessInstanceEventMetadata.PROCESS_INSTANCE_ID_META_DATA, "testKogitoProcessInstanceId");
        metaData.put(ProcessInstanceEventMetadata.PROCESS_VERSION_META_DATA, "testKogitoProcessInstanceVersion");
        metaData.put(ProcessInstanceEventMetadata.ROOT_PROCESS_INSTANCE_ID_META_DATA, "testKogitoRootProcessInstanceId");
        metaData.put(ProcessInstanceEventMetadata.PROCESS_ID_META_DATA, "testKogitoProcessId");
        metaData.put(ProcessInstanceEventMetadata.PROCESS_TYPE_META_DATA, "testKogitoProcessType");
        metaData.put(ProcessInstanceEventMetadata.ROOT_PROCESS_ID_META_DATA, "testKogitoRootProcessId");
        metaData.put(ProcessInstanceEventMetadata.PARENT_PROCESS_INSTANCE_ID_META_DATA, "testKogitoParentProcessInstanceId");
        metaData.put(ProcessInstanceEventMetadata.PROCESS_INSTANCE_STATE_META_DATA, "testKogitoProcessInstanceState");

        ProcessInstanceNodeEventBody body = ProcessInstanceNodeEventBody.create()
                .nodeInstanceId("testId")
                .processInstanceId("testProcessInstanceId")
                .connectionNodeInstanceId("connectionNodeInstanceId")
                .eventDate(new Date())
                .eventType(1)
                .data("test", 2)
                .nodeDefinitionId("testNodeDefinitionId")
                .nodeName("testNodeName")
                .nodeType("testNodeType")
                .build();

        event = new ProcessInstanceNodeDataEvent(source, kogitoAddons, "identity", metaData, body);
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
                    .containsEntry("kogitoProcessInstanceVersion", event.getKogitoProcessInstanceVersion())
                    .containsEntry("kogitoRootProcessinstanceId", event.getKogitoRootProcessInstanceId())
                    .containsEntry("kogitoProcessId", event.getKogitoProcessId())
                    .containsEntry("kogitoProcessType", event.getKogitoProcessType())
                    .containsEntry("kogitoRootProcessId", event.getKogitoRootProcessId())
                    .containsEntry("kogitoAddons", event.getKogitoAddons())
                    .containsEntry("kogitoParentProcessinstanceId", event.getKogitoParentProcessInstanceId())
                    .containsEntry("kogitoProcessinstanceState", event.getKogitoProcessInstanceState())
                    .containsEntry("kogitoReferenceId", event.getKogitoReferenceId())
                    .containsEntry("kogitoStartFromNode", event.getKogitoStartFromNode());

            assertThat(((Document) doc.get("data")))
                    .containsEntry("id", event.getData().getNodeInstanceId())
                    .containsEntry("processInstanceId", event.getData().getProcessInstanceId())
                    .containsEntry("connectionNodeInstanceId", event.getData().getConnectionNodeInstanceId())
                    .containsEntry("nodeDefinitionId", event.getData().getNodeDefinitionId())
                    .containsEntry("nodeName", event.getData().getNodeName())
                    .containsEntry("nodeType", event.getData().getNodeType())
                    .containsEntry("eventTime", event.getData().getEventDate())
                    .containsEntry("eventType", event.getData().getEventType());
        }
    }

    @Test
    void getEncoderClass() {
        assertThat(codec.getEncoderClass()).isEqualTo(ProcessInstanceNodeDataEvent.class);
    }
}
