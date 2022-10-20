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
package org.kie.kogito.workflows.services;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.kie.kogito.event.CloudEventMarshaller;

import io.cloudevents.CloudEvent;

public class JavaSerializationMarshaller<T> implements CloudEventMarshaller<byte[]> {

    @Override
    public byte[] marshall(CloudEvent dataEvent) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeUTF(dataEvent.getSpecVersion().toString());
            out.writeUTF(dataEvent.getId());
            out.writeUTF(dataEvent.getType());
            out.writeUTF(dataEvent.getSource().toString());
            writeOptional(out, dataEvent.getTime());
            writeOptional(out, dataEvent.getSubject());
            writeOptional(out, dataEvent.getDataSchema());
            writeOptional(out, dataEvent.getDataContentType());
            byte[] dataBytes = dataEvent.getData().toBytes();
            out.write(dataBytes, 0, dataBytes.length);
        }
        return bytes.toByteArray();
    }

    private void writeOptional(DataOutputStream out, Object object) throws IOException {
        if (object != null) {
            out.writeBoolean(true);
            out.writeUTF(object.toString());
        } else {
            out.writeBoolean(false);
        }
    }
}
