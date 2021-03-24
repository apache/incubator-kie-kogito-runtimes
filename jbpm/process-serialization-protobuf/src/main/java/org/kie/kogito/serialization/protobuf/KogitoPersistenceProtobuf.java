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

public final class KogitoPersistenceProtobuf {
    private KogitoPersistenceProtobuf() {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions(
                (com.google.protobuf.ExtensionRegistryLite) registry);
    }

    public interface PersistenceDataOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.PersistenceData)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        java.util.List<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance>
                getProcessInstanceList();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance getProcessInstance(int index);

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        int getProcessInstanceCount();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder>
                getProcessInstanceOrBuilderList();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder getProcessInstanceOrBuilder(
                int index);

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer>
                getProcessTimerList();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getProcessTimer(int index);

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        int getProcessTimerCount();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder>
                getProcessTimerOrBuilderList();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder getProcessTimerOrBuilder(
                int index);
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.PersistenceData}
     */
    public static final class PersistenceData extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.PersistenceData)
            PersistenceDataOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use PersistenceData.newBuilder() to construct.
        private PersistenceData(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private PersistenceData() {
            processInstance_ = java.util.Collections.emptyList();
            processTimer_ = java.util.Collections.emptyList();
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new PersistenceData();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private PersistenceData(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
                throw new java.lang.NullPointerException();
            }
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        case 10: {
                            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
                                processInstance_ = new java.util.ArrayList<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance>();
                                mutable_bitField0_ |= 0x00000001;
                            }
                            processInstance_.add(
                                    input.readMessage(org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.parser(), extensionRegistry));
                            break;
                        }
                        case 34: {
                            if (!((mutable_bitField0_ & 0x00000002) != 0)) {
                                processTimer_ = new java.util.ArrayList<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer>();
                                mutable_bitField0_ |= 0x00000002;
                            }
                            processTimer_.add(
                                    input.readMessage(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.parser(), extensionRegistry));
                            break;
                        }
                        default: {
                            if (!parseUnknownField(
                                    input, unknownFields, extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e).setUnfinishedMessage(this);
            } finally {
                if (((mutable_bitField0_ & 0x00000001) != 0)) {
                    processInstance_ = java.util.Collections.unmodifiableList(processInstance_);
                }
                if (((mutable_bitField0_ & 0x00000002) != 0)) {
                    processTimer_ = java.util.Collections.unmodifiableList(processTimer_);
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData.class,
                            org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData.Builder.class);
        }

        public static final int PROCESS_INSTANCE_FIELD_NUMBER = 1;
        private java.util.List<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance> processInstance_;

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        @java.lang.Override
        public java.util.List<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance> getProcessInstanceList() {
            return processInstance_;
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        @java.lang.Override
        public java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder>
                getProcessInstanceOrBuilderList() {
            return processInstance_;
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        @java.lang.Override
        public int getProcessInstanceCount() {
            return processInstance_.size();
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance getProcessInstance(int index) {
            return processInstance_.get(index);
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder getProcessInstanceOrBuilder(
                int index) {
            return processInstance_.get(index);
        }

        public static final int PROCESS_TIMER_FIELD_NUMBER = 4;
        private java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer> processTimer_;

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        @java.lang.Override
        public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer> getProcessTimerList() {
            return processTimer_;
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        @java.lang.Override
        public java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder>
                getProcessTimerOrBuilderList() {
            return processTimer_;
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        @java.lang.Override
        public int getProcessTimerCount() {
            return processTimer_.size();
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getProcessTimer(int index) {
            return processTimer_.get(index);
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder getProcessTimerOrBuilder(
                int index) {
            return processTimer_.get(index);
        }

        private byte memoizedIsInitialized = -1;

        @java.lang.Override
        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1)
                return true;
            if (isInitialized == 0)
                return false;

            memoizedIsInitialized = 1;
            return true;
        }

        @java.lang.Override
        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            for (int i = 0; i < processInstance_.size(); i++) {
                output.writeMessage(1, processInstance_.get(i));
            }
            for (int i = 0; i < processTimer_.size(); i++) {
                output.writeMessage(4, processTimer_.get(i));
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            for (int i = 0; i < processInstance_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(1, processInstance_.get(i));
            }
            for (int i = 0; i < processTimer_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(4, processTimer_.get(i));
            }
            size += unknownFields.getSerializedSize();
            memoizedSize = size;
            return size;
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData other = (org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData) obj;

            if (!getProcessInstanceList()
                    .equals(other.getProcessInstanceList()))
                return false;
            if (!getProcessTimerList()
                    .equals(other.getProcessTimerList()))
                return false;
            if (!unknownFields.equals(other.unknownFields))
                return false;
            return true;
        }

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptor().hashCode();
            if (getProcessInstanceCount() > 0) {
                hash = (37 * hash) + PROCESS_INSTANCE_FIELD_NUMBER;
                hash = (53 * hash) + getProcessInstanceList().hashCode();
            }
            if (getProcessTimerCount() > 0) {
                hash = (37 * hash) + PROCESS_TIMER_FIELD_NUMBER;
                hash = (53 * hash) + getProcessTimerList().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        @java.lang.Override
        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @java.lang.Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE
                    ? new Builder()
                    : new Builder().mergeFrom(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(
                com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        /**
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.PersistenceData}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.PersistenceData)
                org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceDataOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData.class,
                                org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
                    getProcessInstanceFieldBuilder();
                    getProcessTimerFieldBuilder();
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                if (processInstanceBuilder_ == null) {
                    processInstance_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000001);
                } else {
                    processInstanceBuilder_.clear();
                }
                if (processTimerBuilder_ == null) {
                    processTimer_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000002);
                } else {
                    processTimerBuilder_.clear();
                }
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData build() {
                org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData result = new org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData(this);
                int from_bitField0_ = bitField0_;
                if (processInstanceBuilder_ == null) {
                    if (((bitField0_ & 0x00000001) != 0)) {
                        processInstance_ = java.util.Collections.unmodifiableList(processInstance_);
                        bitField0_ = (bitField0_ & ~0x00000001);
                    }
                    result.processInstance_ = processInstance_;
                } else {
                    result.processInstance_ = processInstanceBuilder_.build();
                }
                if (processTimerBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) != 0)) {
                        processTimer_ = java.util.Collections.unmodifiableList(processTimer_);
                        bitField0_ = (bitField0_ & ~0x00000002);
                    }
                    result.processTimer_ = processTimer_;
                } else {
                    result.processTimer_ = processTimerBuilder_.build();
                }
                onBuilt();
                return result;
            }

            @java.lang.Override
            public Builder clone() {
                return super.clone();
            }

            @java.lang.Override
            public Builder setField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    java.lang.Object value) {
                return super.setField(field, value);
            }

            @java.lang.Override
            public Builder clearField(
                    com.google.protobuf.Descriptors.FieldDescriptor field) {
                return super.clearField(field);
            }

            @java.lang.Override
            public Builder clearOneof(
                    com.google.protobuf.Descriptors.OneofDescriptor oneof) {
                return super.clearOneof(oneof);
            }

            @java.lang.Override
            public Builder setRepeatedField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    int index, java.lang.Object value) {
                return super.setRepeatedField(field, index, value);
            }

            @java.lang.Override
            public Builder addRepeatedField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    java.lang.Object value) {
                return super.addRepeatedField(field, value);
            }

            @java.lang.Override
            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData.getDefaultInstance())
                    return this;
                if (processInstanceBuilder_ == null) {
                    if (!other.processInstance_.isEmpty()) {
                        if (processInstance_.isEmpty()) {
                            processInstance_ = other.processInstance_;
                            bitField0_ = (bitField0_ & ~0x00000001);
                        } else {
                            ensureProcessInstanceIsMutable();
                            processInstance_.addAll(other.processInstance_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.processInstance_.isEmpty()) {
                        if (processInstanceBuilder_.isEmpty()) {
                            processInstanceBuilder_.dispose();
                            processInstanceBuilder_ = null;
                            processInstance_ = other.processInstance_;
                            bitField0_ = (bitField0_ & ~0x00000001);
                            processInstanceBuilder_ =
                                    com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ? getProcessInstanceFieldBuilder() : null;
                        } else {
                            processInstanceBuilder_.addAllMessages(other.processInstance_);
                        }
                    }
                }
                if (processTimerBuilder_ == null) {
                    if (!other.processTimer_.isEmpty()) {
                        if (processTimer_.isEmpty()) {
                            processTimer_ = other.processTimer_;
                            bitField0_ = (bitField0_ & ~0x00000002);
                        } else {
                            ensureProcessTimerIsMutable();
                            processTimer_.addAll(other.processTimer_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.processTimer_.isEmpty()) {
                        if (processTimerBuilder_.isEmpty()) {
                            processTimerBuilder_.dispose();
                            processTimerBuilder_ = null;
                            processTimer_ = other.processTimer_;
                            bitField0_ = (bitField0_ & ~0x00000002);
                            processTimerBuilder_ =
                                    com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ? getProcessTimerFieldBuilder() : null;
                        } else {
                            processTimerBuilder_.addAllMessages(other.processTimer_);
                        }
                    }
                }
                this.mergeUnknownFields(other.unknownFields);
                onChanged();
                return this;
            }

            @java.lang.Override
            public final boolean isInitialized() {
                return true;
            }

            @java.lang.Override
            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.util.List<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance> processInstance_ =
                    java.util.Collections.emptyList();

            private void ensureProcessInstanceIsMutable() {
                if (!((bitField0_ & 0x00000001) != 0)) {
                    processInstance_ = new java.util.ArrayList<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance>(processInstance_);
                    bitField0_ |= 0x00000001;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder> processInstanceBuilder_;

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public java.util.List<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance> getProcessInstanceList() {
                if (processInstanceBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(processInstance_);
                } else {
                    return processInstanceBuilder_.getMessageList();
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public int getProcessInstanceCount() {
                if (processInstanceBuilder_ == null) {
                    return processInstance_.size();
                } else {
                    return processInstanceBuilder_.getCount();
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance getProcessInstance(int index) {
                if (processInstanceBuilder_ == null) {
                    return processInstance_.get(index);
                } else {
                    return processInstanceBuilder_.getMessage(index);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder setProcessInstance(
                    int index, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance value) {
                if (processInstanceBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureProcessInstanceIsMutable();
                    processInstance_.set(index, value);
                    onChanged();
                } else {
                    processInstanceBuilder_.setMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder setProcessInstance(
                    int index, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder builderForValue) {
                if (processInstanceBuilder_ == null) {
                    ensureProcessInstanceIsMutable();
                    processInstance_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    processInstanceBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder addProcessInstance(org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance value) {
                if (processInstanceBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureProcessInstanceIsMutable();
                    processInstance_.add(value);
                    onChanged();
                } else {
                    processInstanceBuilder_.addMessage(value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder addProcessInstance(
                    int index, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance value) {
                if (processInstanceBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureProcessInstanceIsMutable();
                    processInstance_.add(index, value);
                    onChanged();
                } else {
                    processInstanceBuilder_.addMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder addProcessInstance(
                    org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder builderForValue) {
                if (processInstanceBuilder_ == null) {
                    ensureProcessInstanceIsMutable();
                    processInstance_.add(builderForValue.build());
                    onChanged();
                } else {
                    processInstanceBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder addProcessInstance(
                    int index, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder builderForValue) {
                if (processInstanceBuilder_ == null) {
                    ensureProcessInstanceIsMutable();
                    processInstance_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    processInstanceBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder addAllProcessInstance(
                    java.lang.Iterable<? extends org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance> values) {
                if (processInstanceBuilder_ == null) {
                    ensureProcessInstanceIsMutable();
                    com.google.protobuf.AbstractMessageLite.Builder.addAll(
                            values, processInstance_);
                    onChanged();
                } else {
                    processInstanceBuilder_.addAllMessages(values);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder clearProcessInstance() {
                if (processInstanceBuilder_ == null) {
                    processInstance_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000001);
                    onChanged();
                } else {
                    processInstanceBuilder_.clear();
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public Builder removeProcessInstance(int index) {
                if (processInstanceBuilder_ == null) {
                    ensureProcessInstanceIsMutable();
                    processInstance_.remove(index);
                    onChanged();
                } else {
                    processInstanceBuilder_.remove(index);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder getProcessInstanceBuilder(
                    int index) {
                return getProcessInstanceFieldBuilder().getBuilder(index);
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder getProcessInstanceOrBuilder(
                    int index) {
                if (processInstanceBuilder_ == null) {
                    return processInstance_.get(index);
                } else {
                    return processInstanceBuilder_.getMessageOrBuilder(index);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder>
                    getProcessInstanceOrBuilderList() {
                if (processInstanceBuilder_ != null) {
                    return processInstanceBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(processInstance_);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder addProcessInstanceBuilder() {
                return getProcessInstanceFieldBuilder().addBuilder(
                        org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.getDefaultInstance());
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder addProcessInstanceBuilder(
                    int index) {
                return getProcessInstanceFieldBuilder().addBuilder(
                        index, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.getDefaultInstance());
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessInstance process_instance = 1;</code>
             */
            public java.util.List<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder>
                    getProcessInstanceBuilderList() {
                return getProcessInstanceFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder>
                    getProcessInstanceFieldBuilder() {
                if (processInstanceBuilder_ == null) {
                    processInstanceBuilder_ =
                            new com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstance.Builder, org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.ProcessInstanceOrBuilder>(
                                    processInstance_,
                                    ((bitField0_ & 0x00000001) != 0),
                                    getParentForChildren(),
                                    isClean());
                    processInstance_ = null;
                }
                return processInstanceBuilder_;
            }

            private java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer> processTimer_ =
                    java.util.Collections.emptyList();

            private void ensureProcessTimerIsMutable() {
                if (!((bitField0_ & 0x00000002) != 0)) {
                    processTimer_ = new java.util.ArrayList<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer>(processTimer_);
                    bitField0_ |= 0x00000002;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder> processTimerBuilder_;

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer> getProcessTimerList() {
                if (processTimerBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(processTimer_);
                } else {
                    return processTimerBuilder_.getMessageList();
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public int getProcessTimerCount() {
                if (processTimerBuilder_ == null) {
                    return processTimer_.size();
                } else {
                    return processTimerBuilder_.getCount();
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getProcessTimer(int index) {
                if (processTimerBuilder_ == null) {
                    return processTimer_.get(index);
                } else {
                    return processTimerBuilder_.getMessage(index);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder setProcessTimer(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer value) {
                if (processTimerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureProcessTimerIsMutable();
                    processTimer_.set(index, value);
                    onChanged();
                } else {
                    processTimerBuilder_.setMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder setProcessTimer(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder builderForValue) {
                if (processTimerBuilder_ == null) {
                    ensureProcessTimerIsMutable();
                    processTimer_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    processTimerBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder addProcessTimer(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer value) {
                if (processTimerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureProcessTimerIsMutable();
                    processTimer_.add(value);
                    onChanged();
                } else {
                    processTimerBuilder_.addMessage(value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder addProcessTimer(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer value) {
                if (processTimerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureProcessTimerIsMutable();
                    processTimer_.add(index, value);
                    onChanged();
                } else {
                    processTimerBuilder_.addMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder addProcessTimer(
                    org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder builderForValue) {
                if (processTimerBuilder_ == null) {
                    ensureProcessTimerIsMutable();
                    processTimer_.add(builderForValue.build());
                    onChanged();
                } else {
                    processTimerBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder addProcessTimer(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder builderForValue) {
                if (processTimerBuilder_ == null) {
                    ensureProcessTimerIsMutable();
                    processTimer_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    processTimerBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder addAllProcessTimer(
                    java.lang.Iterable<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer> values) {
                if (processTimerBuilder_ == null) {
                    ensureProcessTimerIsMutable();
                    com.google.protobuf.AbstractMessageLite.Builder.addAll(
                            values, processTimer_);
                    onChanged();
                } else {
                    processTimerBuilder_.addAllMessages(values);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder clearProcessTimer() {
                if (processTimerBuilder_ == null) {
                    processTimer_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000002);
                    onChanged();
                } else {
                    processTimerBuilder_.clear();
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public Builder removeProcessTimer(int index) {
                if (processTimerBuilder_ == null) {
                    ensureProcessTimerIsMutable();
                    processTimer_.remove(index);
                    onChanged();
                } else {
                    processTimerBuilder_.remove(index);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder getProcessTimerBuilder(
                    int index) {
                return getProcessTimerFieldBuilder().getBuilder(index);
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder getProcessTimerOrBuilder(
                    int index) {
                if (processTimerBuilder_ == null) {
                    return processTimer_.get(index);
                } else {
                    return processTimerBuilder_.getMessageOrBuilder(index);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder>
                    getProcessTimerOrBuilderList() {
                if (processTimerBuilder_ != null) {
                    return processTimerBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(processTimer_);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder addProcessTimerBuilder() {
                return getProcessTimerFieldBuilder().addBuilder(
                        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance());
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder addProcessTimerBuilder(
                    int index) {
                return getProcessTimerFieldBuilder().addBuilder(
                        index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance());
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.ProcessTimer process_timer = 4;</code>
             */
            public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder>
                    getProcessTimerBuilderList() {
                return getProcessTimerFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder>
                    getProcessTimerFieldBuilder() {
                if (processTimerBuilder_ == null) {
                    processTimerBuilder_ =
                            new com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder>(
                                    processTimer_,
                                    ((bitField0_ & 0x00000002) != 0),
                                    getParentForChildren(),
                                    isClean());
                    processTimer_ = null;
                }
                return processTimerBuilder_;
            }

            @java.lang.Override
            public final Builder setUnknownFields(
                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return super.setUnknownFields(unknownFields);
            }

            @java.lang.Override
            public final Builder mergeUnknownFields(
                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return super.mergeUnknownFields(unknownFields);
            }

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.PersistenceData)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.PersistenceData)
        private static final org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<PersistenceData> PARSER = new com.google.protobuf.AbstractParser<PersistenceData>() {
            @java.lang.Override
            public PersistenceData parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new PersistenceData(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<PersistenceData> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<PersistenceData> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoPersistenceProtobuf.PersistenceData getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor
            getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
    static {
        java.lang.String[] descriptorData = {
                "\n-org/jbpm/marshalling/kogito_persistenc" +
                        "e.proto\022%org.kie.kogito.serialization.pr" +
                        "otobuf\0322org/jbpm/marshalling/kogito_proc" +
                        "ess_instance.proto\032(org/jbpm/marshalling" +
                        "/kogito_timers.proto\"\257\001\n\017PersistenceData" +
                        "\022P\n\020process_instance\030\001 \003(\01326.org.kie.kog" +
                        "ito.serialization.protobuf.ProcessInstan" +
                        "ce\022J\n\rprocess_timer\030\004 \003(\01323.org.kie.kogi" +
                        "to.serialization.protobuf.ProcessTimerBB" +
                        "\n%org.kie.kogito.serialization.protobufB" +
                        "\031KogitoPersistenceProtobufb\006proto3"
        };
        descriptor = com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[] {
                                org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.getDescriptor(),
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.getDescriptor(),
                        });
        internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_PersistenceData_descriptor,
                new java.lang.String[] { "ProcessInstance", "ProcessTimer", });
        org.kie.kogito.serialization.protobuf.KogitoProcessInstanceProtobuf.getDescriptor();
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.getDescriptor();
    }

    // @@protoc_insertion_point(outer_class_scope)
}
