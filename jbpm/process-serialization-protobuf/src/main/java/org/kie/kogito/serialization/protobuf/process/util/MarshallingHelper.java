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
package org.kie.kogito.serialization.protobuf.process.util;

public class MarshallingHelper {

    protected static Object[] toArrayOfObject(long[] longs) {
        Object[] objects = new Object[longs.length];
        for (int i = 0; i < longs.length; i++) {
            objects[i] = longs[i];
        }
        return objects;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte) ((value >>> 24) & 0xFF),
                (byte) ((value >>> 16) & 0xFF),
                (byte) ((value >>> 8) & 0xFF),
                (byte) (value & 0xFF) };
    }

    public static final int byteArrayToInt(byte[] b) {
        return (b[0] << 24)
                + ((b[1] & 0xFF) << 16)
                + ((b[2] & 0xFF) << 8)
                + (b[3] & 0xFF);
    }

    // more efficient than instantiating byte buffers and opening streams
    public static final byte[] longToByteArray(long value) {
        return new byte[] {
                (byte) ((value >>> 56) & 0xFF),
                (byte) ((value >>> 48) & 0xFF),
                (byte) ((value >>> 40) & 0xFF),
                (byte) ((value >>> 32) & 0xFF),
                (byte) ((value >>> 24) & 0xFF),
                (byte) ((value >>> 16) & 0xFF),
                (byte) ((value >>> 8) & 0xFF),
                (byte) (value & 0xFF) };
    }

    public static final long byteArrayToLong(byte[] b) {
        return ((((long) b[0]) & 0xFF) << 56)
                + ((((long) b[1]) & 0xFF) << 48)
                + ((((long) b[2]) & 0xFF) << 40)
                + ((((long) b[3]) & 0xFF) << 32)
                + ((((long) b[4]) & 0xFF) << 24)
                + ((((long) b[5]) & 0xFF) << 16)
                + ((((long) b[6]) & 0xFF) << 8)
                + (((long) b[7]) & 0xFF);
    }

}
