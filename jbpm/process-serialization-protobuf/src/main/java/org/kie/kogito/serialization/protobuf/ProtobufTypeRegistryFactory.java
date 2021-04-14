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
package org.kie.kogito.serialization.protobuf;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.WrappersProto;

public final class ProtobufTypeRegistryFactory {

    private ProtobufTypeRegistryFactory() {
        // do nothing
    }

    public static TypeRegistry create() {
        return TypeRegistry.newBuilder()
                .add(KogitoTypesProtobuf.getDescriptor().getMessageTypes())
                .add(KogitoProcessInstanceProtobuf.getDescriptor().getMessageTypes())
                .add(KogitoNodeInstanceContentsProtobuf.getDescriptor().getMessageTypes())
                .add(KogitoWorkItemsProtobuf.getDescriptor().getMessageTypes())
                .add(WrappersProto.getDescriptor().getMessageTypes())
                .add(Timestamp.getDescriptor())
                .add(Empty.getDescriptor()).build();
    }
}
