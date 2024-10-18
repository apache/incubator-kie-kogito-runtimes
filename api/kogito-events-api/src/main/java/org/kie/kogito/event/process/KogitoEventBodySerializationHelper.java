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
package org.kie.kogito.event.process;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Date;

import org.kie.kogito.jackson.utils.ObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class KogitoEventBodySerializationHelper {

    private KogitoEventBodySerializationHelper() {
    }

    public static String readUTF(DataInput in) throws IOException {
        byte value = in.readByte();
        return value > 0 ? in.readUTF() : null;
    }

    public static void writeUTF(DataOutput out, String string) throws IOException {
        if (string == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            out.writeUTF(string);
        }
    }

    public static Date readDate(DataInput in) throws IOException {
        byte value = in.readByte();
        return value > 0 ? new Date(in.readLong()) : null;
    }

    public static void writeDate(DataOutput out, Date date) throws IOException {
        if (date == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            out.writeLong(date.getTime());
        }
    }

    public static void writeInt(DataOutput out, Integer integer) throws IOException {
        out.writeInt(integer == null ? Integer.MIN_VALUE : integer.intValue());
    }

    public static Integer readInt(DataInput in) throws IOException {
        int integer = in.readInt();
        return integer == Integer.MIN_VALUE ? null : Integer.valueOf(integer);
    }

    public static void writeUTFCollection(DataOutput out, Collection<String> collection) throws IOException {
        if (collection == null) {
            out.writeShort(Short.MIN_VALUE);
        } else {
            out.writeShort(collection.size());
            for (String item : collection) {
                writeUTF(out, item);
            }
        }
    }

    public static <T extends Collection<String>> T readUTFCollection(DataInput in, T holder) throws IOException {
        int size = in.readShort();
        if (size == Short.MIN_VALUE) {
            return null;
        }
        while (size-- > 0) {
            holder.add(readUTF(in));
        }
        return holder;
    }

    public static void writeObject(DataOutput out, Object obj) throws IOException {
        // TODO support java types directly (without using jackson)
        if (obj == null) {
            out.writeByte(0);
        } else {
            Class<?> type = obj.getClass();
            if (JsonNode.class.isAssignableFrom(type)) {
                out.writeByte(1);
            } else {
                out.writeByte(2);
            }
            byte[] bytes = ObjectMapperFactory.get().writeValueAsBytes(obj);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }

    public static Object readObject(DataInput in) throws IOException {
        // TODO support java types directly (without using jackson)
        byte helperByte = in.readByte();
        if (helperByte == 0) {
            return null;
        } else {
            Class<?> type;
            if (helperByte == 1) {
                type = JsonNode.class;
            } else {
                type = Object.class;
            }
            byte[] bytes = new byte[in.readInt()];
            in.readFully(bytes);
            return ObjectMapperFactory.get().readValue(bytes, type);
        }
    }

    public static Date toDate(OffsetDateTime time) {
        return time == null ? null : Date.from(time.toInstant());
    }
}
