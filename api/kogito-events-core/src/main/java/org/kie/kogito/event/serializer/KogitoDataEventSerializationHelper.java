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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.kie.kogito.event.AbstractDataEvent;
import org.kie.kogito.event.DataEvent;

import io.cloudevents.SpecVersion;

import static org.kie.kogito.event.process.KogitoEventBodySerializationHelper.*;

class KogitoDataEventSerializationHelper {

    private KogitoDataEventSerializationHelper() {
    }

    public static void writeCloudEventAttrs(DataOutput out, DataEvent<?> data) throws IOException {
        out.writeUTF(data.getSpecVersion().toString());
        out.writeUTF(data.getId());
        out.writeUTF(data.getSource().toString());
        out.writeLong(data.getTime().toInstant().toEpochMilli());
        writeUTF(out, data.getSubject());
        writeUTF(out, data.getDataContentType());
        writeUTF(out, data.getDataSchema() != null ? data.getDataSchema().toString() : null);
        //TODO include more optional attributes?
    }

    public static <T extends AbstractDataEvent<?>> T readCloudEventAttrs(DataInput in, T data) throws IOException {
        data.setSpecVersion(SpecVersion.parse(in.readUTF()));
        data.setId(in.readUTF());
        data.setSource(URI.create(in.readUTF()));
        data.setTime(OffsetDateTime.ofInstant(Instant.ofEpochMilli(in.readLong()), ZoneOffset.UTC));
        data.setSubject(readUTF(in));
        data.setDataContentType(readUTF(in));
        data.setDataSchema(URI.create(readUTF(in)));
        return data;
    }

}
