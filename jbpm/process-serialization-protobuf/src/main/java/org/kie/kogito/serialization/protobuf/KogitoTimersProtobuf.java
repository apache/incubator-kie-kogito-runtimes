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

public final class KogitoTimersProtobuf {
    private KogitoTimersProtobuf() {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions(
                (com.google.protobuf.ExtensionRegistryLite) registry);
    }

    public interface TimersOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.Timers)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer>
                getTimerList();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer getTimer(int index);

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        int getTimerCount();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder>
                getTimerOrBuilderList();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder getTimerOrBuilder(
                int index);

        /**
         * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
         * 
         * @return Whether the procTimer field is set.
         */
        boolean hasProcTimer();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
         * 
         * @return The procTimer.
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getProcTimer();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder getProcTimerOrBuilder();
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.Timers}
     */
    public static final class Timers extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.Timers)
            TimersOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use Timers.newBuilder() to construct.
        private Timers(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private Timers() {
            timer_ = java.util.Collections.emptyList();
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new Timers();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private Timers(
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
                                timer_ = new java.util.ArrayList<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer>();
                                mutable_bitField0_ |= 0x00000001;
                            }
                            timer_.add(
                                    input.readMessage(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.parser(), extensionRegistry));
                            break;
                        }
                        case 18: {
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder subBuilder = null;
                            if (((bitField0_ & 0x00000001) != 0)) {
                                subBuilder = procTimer_.toBuilder();
                            }
                            procTimer_ = input.readMessage(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.parser(), extensionRegistry);
                            if (subBuilder != null) {
                                subBuilder.mergeFrom(procTimer_);
                                procTimer_ = subBuilder.buildPartial();
                            }
                            bitField0_ |= 0x00000001;
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
                    timer_ = java.util.Collections.unmodifiableList(timer_);
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timers_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timers_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers.Builder.class);
        }

        private int bitField0_;
        public static final int TIMER_FIELD_NUMBER = 1;
        private java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer> timer_;

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        @java.lang.Override
        public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer> getTimerList() {
            return timer_;
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        @java.lang.Override
        public java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder>
                getTimerOrBuilderList() {
            return timer_;
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        @java.lang.Override
        public int getTimerCount() {
            return timer_.size();
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer getTimer(int index) {
            return timer_.get(index);
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder getTimerOrBuilder(
                int index) {
            return timer_.get(index);
        }

        public static final int PROC_TIMER_FIELD_NUMBER = 2;
        private org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer procTimer_;

        /**
         * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
         * 
         * @return Whether the procTimer field is set.
         */
        @java.lang.Override
        public boolean hasProcTimer() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
         * 
         * @return The procTimer.
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getProcTimer() {
            return procTimer_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance() : procTimer_;
        }

        /**
         * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder getProcTimerOrBuilder() {
            return procTimer_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance() : procTimer_;
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
            for (int i = 0; i < timer_.size(); i++) {
                output.writeMessage(1, timer_.get(i));
            }
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeMessage(2, getProcTimer());
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            for (int i = 0; i < timer_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(1, timer_.get(i));
            }
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(2, getProcTimer());
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers) obj;

            if (!getTimerList()
                    .equals(other.getTimerList()))
                return false;
            if (hasProcTimer() != other.hasProcTimer())
                return false;
            if (hasProcTimer()) {
                if (!getProcTimer()
                        .equals(other.getProcTimer()))
                    return false;
            }
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
            if (getTimerCount() > 0) {
                hash = (37 * hash) + TIMER_FIELD_NUMBER;
                hash = (53 * hash) + getTimerList().hashCode();
            }
            if (hasProcTimer()) {
                hash = (37 * hash) + PROC_TIMER_FIELD_NUMBER;
                hash = (53 * hash) + getProcTimer().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.Timers}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.Timers)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimersOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timers_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timers_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers.newBuilder()
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
                    getTimerFieldBuilder();
                    getProcTimerFieldBuilder();
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                if (timerBuilder_ == null) {
                    timer_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000001);
                } else {
                    timerBuilder_.clear();
                }
                if (procTimerBuilder_ == null) {
                    procTimer_ = null;
                } else {
                    procTimerBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timers_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (timerBuilder_ == null) {
                    if (((bitField0_ & 0x00000001) != 0)) {
                        timer_ = java.util.Collections.unmodifiableList(timer_);
                        bitField0_ = (bitField0_ & ~0x00000001);
                    }
                    result.timer_ = timer_;
                } else {
                    result.timer_ = timerBuilder_.build();
                }
                if (((from_bitField0_ & 0x00000002) != 0)) {
                    if (procTimerBuilder_ == null) {
                        result.procTimer_ = procTimer_;
                    } else {
                        result.procTimer_ = procTimerBuilder_.build();
                    }
                    to_bitField0_ |= 0x00000001;
                }
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers.getDefaultInstance())
                    return this;
                if (timerBuilder_ == null) {
                    if (!other.timer_.isEmpty()) {
                        if (timer_.isEmpty()) {
                            timer_ = other.timer_;
                            bitField0_ = (bitField0_ & ~0x00000001);
                        } else {
                            ensureTimerIsMutable();
                            timer_.addAll(other.timer_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.timer_.isEmpty()) {
                        if (timerBuilder_.isEmpty()) {
                            timerBuilder_.dispose();
                            timerBuilder_ = null;
                            timer_ = other.timer_;
                            bitField0_ = (bitField0_ & ~0x00000001);
                            timerBuilder_ =
                                    com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ? getTimerFieldBuilder() : null;
                        } else {
                            timerBuilder_.addAllMessages(other.timer_);
                        }
                    }
                }
                if (other.hasProcTimer()) {
                    mergeProcTimer(other.getProcTimer());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer> timer_ =
                    java.util.Collections.emptyList();

            private void ensureTimerIsMutable() {
                if (!((bitField0_ & 0x00000001) != 0)) {
                    timer_ = new java.util.ArrayList<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer>(timer_);
                    bitField0_ |= 0x00000001;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder> timerBuilder_;

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer> getTimerList() {
                if (timerBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(timer_);
                } else {
                    return timerBuilder_.getMessageList();
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public int getTimerCount() {
                if (timerBuilder_ == null) {
                    return timer_.size();
                } else {
                    return timerBuilder_.getCount();
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer getTimer(int index) {
                if (timerBuilder_ == null) {
                    return timer_.get(index);
                } else {
                    return timerBuilder_.getMessage(index);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder setTimer(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer value) {
                if (timerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureTimerIsMutable();
                    timer_.set(index, value);
                    onChanged();
                } else {
                    timerBuilder_.setMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder setTimer(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder builderForValue) {
                if (timerBuilder_ == null) {
                    ensureTimerIsMutable();
                    timer_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    timerBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder addTimer(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer value) {
                if (timerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureTimerIsMutable();
                    timer_.add(value);
                    onChanged();
                } else {
                    timerBuilder_.addMessage(value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder addTimer(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer value) {
                if (timerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureTimerIsMutable();
                    timer_.add(index, value);
                    onChanged();
                } else {
                    timerBuilder_.addMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder addTimer(
                    org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder builderForValue) {
                if (timerBuilder_ == null) {
                    ensureTimerIsMutable();
                    timer_.add(builderForValue.build());
                    onChanged();
                } else {
                    timerBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder addTimer(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder builderForValue) {
                if (timerBuilder_ == null) {
                    ensureTimerIsMutable();
                    timer_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    timerBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder addAllTimer(
                    java.lang.Iterable<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer> values) {
                if (timerBuilder_ == null) {
                    ensureTimerIsMutable();
                    com.google.protobuf.AbstractMessageLite.Builder.addAll(
                            values, timer_);
                    onChanged();
                } else {
                    timerBuilder_.addAllMessages(values);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder clearTimer() {
                if (timerBuilder_ == null) {
                    timer_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000001);
                    onChanged();
                } else {
                    timerBuilder_.clear();
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public Builder removeTimer(int index) {
                if (timerBuilder_ == null) {
                    ensureTimerIsMutable();
                    timer_.remove(index);
                    onChanged();
                } else {
                    timerBuilder_.remove(index);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder getTimerBuilder(
                    int index) {
                return getTimerFieldBuilder().getBuilder(index);
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder getTimerOrBuilder(
                    int index) {
                if (timerBuilder_ == null) {
                    return timer_.get(index);
                } else {
                    return timerBuilder_.getMessageOrBuilder(index);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder>
                    getTimerOrBuilderList() {
                if (timerBuilder_ != null) {
                    return timerBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(timer_);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder addTimerBuilder() {
                return getTimerFieldBuilder().addBuilder(
                        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.getDefaultInstance());
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder addTimerBuilder(
                    int index) {
                return getTimerFieldBuilder().addBuilder(
                        index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.getDefaultInstance());
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.Timer timer = 1;</code>
             */
            public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder>
                    getTimerBuilderList() {
                return getTimerFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder>
                    getTimerFieldBuilder() {
                if (timerBuilder_ == null) {
                    timerBuilder_ =
                            new com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder>(
                                    timer_,
                                    ((bitField0_ & 0x00000001) != 0),
                                    getParentForChildren(),
                                    isClean());
                    timer_ = null;
                }
                return timerBuilder_;
            }

            private org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer procTimer_;
            private com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder> procTimerBuilder_;

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             * 
             * @return Whether the procTimer field is set.
             */
            public boolean hasProcTimer() {
                return ((bitField0_ & 0x00000002) != 0);
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             * 
             * @return The procTimer.
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getProcTimer() {
                if (procTimerBuilder_ == null) {
                    return procTimer_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance() : procTimer_;
                } else {
                    return procTimerBuilder_.getMessage();
                }
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             */
            public Builder setProcTimer(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer value) {
                if (procTimerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    procTimer_ = value;
                    onChanged();
                } else {
                    procTimerBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             */
            public Builder setProcTimer(
                    org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder builderForValue) {
                if (procTimerBuilder_ == null) {
                    procTimer_ = builderForValue.build();
                    onChanged();
                } else {
                    procTimerBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             */
            public Builder mergeProcTimer(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer value) {
                if (procTimerBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) != 0) &&
                            procTimer_ != null &&
                            procTimer_ != org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance()) {
                        procTimer_ =
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.newBuilder(procTimer_).mergeFrom(value).buildPartial();
                    } else {
                        procTimer_ = value;
                    }
                    onChanged();
                } else {
                    procTimerBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             */
            public Builder clearProcTimer() {
                if (procTimerBuilder_ == null) {
                    procTimer_ = null;
                    onChanged();
                } else {
                    procTimerBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder getProcTimerBuilder() {
                bitField0_ |= 0x00000002;
                onChanged();
                return getProcTimerFieldBuilder().getBuilder();
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder getProcTimerOrBuilder() {
                if (procTimerBuilder_ != null) {
                    return procTimerBuilder_.getMessageOrBuilder();
                } else {
                    return procTimer_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance() : procTimer_;
                }
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.ProcessTimer proc_timer = 2;</code>
             */
            private com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder>
                    getProcTimerFieldBuilder() {
                if (procTimerBuilder_ == null) {
                    procTimerBuilder_ =
                            new com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder>(
                                    getProcTimer(),
                                    getParentForChildren(),
                                    isClean());
                    procTimer_ = null;
                }
                return procTimerBuilder_;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.Timers)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.Timers)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<Timers> PARSER = new com.google.protobuf.AbstractParser<Timers>() {
            @java.lang.Override
            public Timers parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new Timers(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<Timers> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<Timers> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timers getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface TimerOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.Timer)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
         * 
         * @return The enum numeric value on the wire for type.
         */
        int getTypeValue();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
         * 
         * @return The type.
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType getType();

        /**
         * <code>.google.protobuf.Any data = 2;</code>
         * 
         * @return Whether the data field is set.
         */
        boolean hasData();

        /**
         * <code>.google.protobuf.Any data = 2;</code>
         * 
         * @return The data.
         */
        com.google.protobuf.Any getData();

        /**
         * <code>.google.protobuf.Any data = 2;</code>
         */
        com.google.protobuf.AnyOrBuilder getDataOrBuilder();
    }

    /**
     * <pre>
     * timers
     * </pre>
     *
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.Timer}
     */
    public static final class Timer extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.Timer)
            TimerOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use Timer.newBuilder() to construct.
        private Timer(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private Timer() {
            type_ = 0;
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new Timer();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private Timer(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
                throw new java.lang.NullPointerException();
            }
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
                        case 8: {
                            int rawValue = input.readEnum();

                            type_ = rawValue;
                            break;
                        }
                        case 18: {
                            com.google.protobuf.Any.Builder subBuilder = null;
                            if (data_ != null) {
                                subBuilder = data_.toBuilder();
                            }
                            data_ = input.readMessage(com.google.protobuf.Any.parser(), extensionRegistry);
                            if (subBuilder != null) {
                                subBuilder.mergeFrom(data_);
                                data_ = subBuilder.buildPartial();
                            }

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
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timer_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timer_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder.class);
        }

        /**
         * Protobuf enum {@code org.kie.kogito.serialization.protobuf.Timer.TimerType}
         */
        public enum TimerType
                implements com.google.protobuf.ProtocolMessageEnum {
            /**
             * <code>EXPIRE = 0;</code>
             */
            EXPIRE(0),
            /**
             * <code>ACTIVATION = 1;</code>
             */
            ACTIVATION(1),
            /**
             * <code>BEHAVIOR = 2;</code>
             */
            BEHAVIOR(2),
            /**
             * <code>PROCESS = 3;</code>
             */
            PROCESS(3),
            /**
             * <code>TIMER_NODE = 4;</code>
             */
            TIMER_NODE(4),
            UNRECOGNIZED(-1),
            ;

            /**
             * <code>EXPIRE = 0;</code>
             */
            public static final int EXPIRE_VALUE = 0;
            /**
             * <code>ACTIVATION = 1;</code>
             */
            public static final int ACTIVATION_VALUE = 1;
            /**
             * <code>BEHAVIOR = 2;</code>
             */
            public static final int BEHAVIOR_VALUE = 2;
            /**
             * <code>PROCESS = 3;</code>
             */
            public static final int PROCESS_VALUE = 3;
            /**
             * <code>TIMER_NODE = 4;</code>
             */
            public static final int TIMER_NODE_VALUE = 4;

            public final int getNumber() {
                if (this == UNRECOGNIZED) {
                    throw new java.lang.IllegalArgumentException(
                            "Can't get the number of an unknown enum value.");
                }
                return value;
            }

            /**
             * @param value The numeric wire value of the corresponding enum entry.
             * @return The enum associated with the given numeric wire value.
             * @deprecated Use {@link #forNumber(int)} instead.
             */
            @java.lang.Deprecated
            public static TimerType valueOf(int value) {
                return forNumber(value);
            }

            /**
             * @param value The numeric wire value of the corresponding enum entry.
             * @return The enum associated with the given numeric wire value.
             */
            public static TimerType forNumber(int value) {
                switch (value) {
                    case 0:
                        return EXPIRE;
                    case 1:
                        return ACTIVATION;
                    case 2:
                        return BEHAVIOR;
                    case 3:
                        return PROCESS;
                    case 4:
                        return TIMER_NODE;
                    default:
                        return null;
                }
            }

            public static com.google.protobuf.Internal.EnumLiteMap<TimerType>
                    internalGetValueMap() {
                return internalValueMap;
            }

            private static final com.google.protobuf.Internal.EnumLiteMap<TimerType> internalValueMap =
                    new com.google.protobuf.Internal.EnumLiteMap<TimerType>() {
                        public TimerType findValueByNumber(int number) {
                            return TimerType.forNumber(number);
                        }
                    };

            public final com.google.protobuf.Descriptors.EnumValueDescriptor
                    getValueDescriptor() {
                if (this == UNRECOGNIZED) {
                    throw new java.lang.IllegalStateException(
                            "Can't get the descriptor of an unrecognized enum value.");
                }
                return getDescriptor().getValues().get(ordinal());
            }

            public final com.google.protobuf.Descriptors.EnumDescriptor
                    getDescriptorForType() {
                return getDescriptor();
            }

            public static final com.google.protobuf.Descriptors.EnumDescriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.getDescriptor().getEnumTypes().get(0);
            }

            private static final TimerType[] VALUES = values();

            public static TimerType valueOf(
                    com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
                if (desc.getType() != getDescriptor()) {
                    throw new java.lang.IllegalArgumentException(
                            "EnumValueDescriptor is not for this type.");
                }
                if (desc.getIndex() == -1) {
                    return UNRECOGNIZED;
                }
                return VALUES[desc.getIndex()];
            }

            private final int value;

            private TimerType(int value) {
                this.value = value;
            }

            // @@protoc_insertion_point(enum_scope:org.kie.kogito.serialization.protobuf.Timer.TimerType)
        }

        public static final int TYPE_FIELD_NUMBER = 1;
        private int type_;

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
         * 
         * @return The enum numeric value on the wire for type.
         */
        @java.lang.Override
        public int getTypeValue() {
            return type_;
        }

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
         * 
         * @return The type.
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType getType() {
            @SuppressWarnings("deprecation")
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType result = org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType.valueOf(type_);
            return result == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType.UNRECOGNIZED : result;
        }

        public static final int DATA_FIELD_NUMBER = 2;
        private com.google.protobuf.Any data_;

        /**
         * <code>.google.protobuf.Any data = 2;</code>
         * 
         * @return Whether the data field is set.
         */
        @java.lang.Override
        public boolean hasData() {
            return data_ != null;
        }

        /**
         * <code>.google.protobuf.Any data = 2;</code>
         * 
         * @return The data.
         */
        @java.lang.Override
        public com.google.protobuf.Any getData() {
            return data_ == null ? com.google.protobuf.Any.getDefaultInstance() : data_;
        }

        /**
         * <code>.google.protobuf.Any data = 2;</code>
         */
        @java.lang.Override
        public com.google.protobuf.AnyOrBuilder getDataOrBuilder() {
            return getData();
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
            if (type_ != org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType.EXPIRE.getNumber()) {
                output.writeEnum(1, type_);
            }
            if (data_ != null) {
                output.writeMessage(2, getData());
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (type_ != org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType.EXPIRE.getNumber()) {
                size += com.google.protobuf.CodedOutputStream
                        .computeEnumSize(1, type_);
            }
            if (data_ != null) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(2, getData());
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer) obj;

            if (type_ != other.type_)
                return false;
            if (hasData() != other.hasData())
                return false;
            if (hasData()) {
                if (!getData()
                        .equals(other.getData()))
                    return false;
            }
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
            hash = (37 * hash) + TYPE_FIELD_NUMBER;
            hash = (53 * hash) + type_;
            if (hasData()) {
                hash = (37 * hash) + DATA_FIELD_NUMBER;
                hash = (53 * hash) + getData().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer prototype) {
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
         * <pre>
         * timers
         * </pre>
         *
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.Timer}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.Timer)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timer_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timer_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.newBuilder()
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
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                type_ = 0;

                if (dataBuilder_ == null) {
                    data_ = null;
                } else {
                    data_ = null;
                    dataBuilder_ = null;
                }
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Timer_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer(this);
                result.type_ = type_;
                if (dataBuilder_ == null) {
                    result.data_ = data_;
                } else {
                    result.data_ = dataBuilder_.build();
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.getDefaultInstance())
                    return this;
                if (other.type_ != 0) {
                    setTypeValue(other.getTypeValue());
                }
                if (other.hasData()) {
                    mergeData(other.getData());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int type_ = 0;

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
             * 
             * @return The enum numeric value on the wire for type.
             */
            @java.lang.Override
            public int getTypeValue() {
                return type_;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
             * 
             * @param value The enum numeric value on the wire for type to set.
             * @return This builder for chaining.
             */
            public Builder setTypeValue(int value) {

                type_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
             * 
             * @return The type.
             */
            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType getType() {
                @SuppressWarnings("deprecation")
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType result = org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType.valueOf(type_);
                return result == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType.UNRECOGNIZED : result;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
             * 
             * @param value The type to set.
             * @return This builder for chaining.
             */
            public Builder setType(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer.TimerType value) {
                if (value == null) {
                    throw new NullPointerException();
                }

                type_ = value.getNumber();
                onChanged();
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Timer.TimerType type = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearType() {

                type_ = 0;
                onChanged();
                return this;
            }

            private com.google.protobuf.Any data_;
            private com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> dataBuilder_;

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             * 
             * @return Whether the data field is set.
             */
            public boolean hasData() {
                return dataBuilder_ != null || data_ != null;
            }

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             * 
             * @return The data.
             */
            public com.google.protobuf.Any getData() {
                if (dataBuilder_ == null) {
                    return data_ == null ? com.google.protobuf.Any.getDefaultInstance() : data_;
                } else {
                    return dataBuilder_.getMessage();
                }
            }

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             */
            public Builder setData(com.google.protobuf.Any value) {
                if (dataBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    data_ = value;
                    onChanged();
                } else {
                    dataBuilder_.setMessage(value);
                }

                return this;
            }

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             */
            public Builder setData(
                    com.google.protobuf.Any.Builder builderForValue) {
                if (dataBuilder_ == null) {
                    data_ = builderForValue.build();
                    onChanged();
                } else {
                    dataBuilder_.setMessage(builderForValue.build());
                }

                return this;
            }

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             */
            public Builder mergeData(com.google.protobuf.Any value) {
                if (dataBuilder_ == null) {
                    if (data_ != null) {
                        data_ =
                                com.google.protobuf.Any.newBuilder(data_).mergeFrom(value).buildPartial();
                    } else {
                        data_ = value;
                    }
                    onChanged();
                } else {
                    dataBuilder_.mergeFrom(value);
                }

                return this;
            }

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             */
            public Builder clearData() {
                if (dataBuilder_ == null) {
                    data_ = null;
                    onChanged();
                } else {
                    data_ = null;
                    dataBuilder_ = null;
                }

                return this;
            }

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             */
            public com.google.protobuf.Any.Builder getDataBuilder() {

                onChanged();
                return getDataFieldBuilder().getBuilder();
            }

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             */
            public com.google.protobuf.AnyOrBuilder getDataOrBuilder() {
                if (dataBuilder_ != null) {
                    return dataBuilder_.getMessageOrBuilder();
                } else {
                    return data_ == null ? com.google.protobuf.Any.getDefaultInstance() : data_;
                }
            }

            /**
             * <code>.google.protobuf.Any data = 2;</code>
             */
            private com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>
                    getDataFieldBuilder() {
                if (dataBuilder_ == null) {
                    dataBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>(
                            getData(),
                            getParentForChildren(),
                            isClean());
                    data_ = null;
                }
                return dataBuilder_;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.Timer)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.Timer)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<Timer> PARSER = new com.google.protobuf.AbstractParser<Timer>() {
            @java.lang.Override
            public Timer parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new Timer(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<Timer> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<Timer> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Timer getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface ExpireTimerDataOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.ExpireTimerData)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>int64 handle_id = 1;</code>
         * 
         * @return Whether the handleId field is set.
         */
        boolean hasHandleId();

        /**
         * <code>int64 handle_id = 1;</code>
         * 
         * @return The handleId.
         */
        long getHandleId();

        /**
         * <code>string entry_point_id = 2;</code>
         * 
         * @return Whether the entryPointId field is set.
         */
        boolean hasEntryPointId();

        /**
         * <code>string entry_point_id = 2;</code>
         * 
         * @return The entryPointId.
         */
        java.lang.String getEntryPointId();

        /**
         * <code>string entry_point_id = 2;</code>
         * 
         * @return The bytes for entryPointId.
         */
        com.google.protobuf.ByteString
                getEntryPointIdBytes();

        /**
         * <code>string class_name = 3;</code>
         * 
         * @return Whether the className field is set.
         */
        boolean hasClassName();

        /**
         * <code>string class_name = 3;</code>
         * 
         * @return The className.
         */
        java.lang.String getClassName();

        /**
         * <code>string class_name = 3;</code>
         * 
         * @return The bytes for className.
         */
        com.google.protobuf.ByteString
                getClassNameBytes();

        /**
         * <code>int64 next_fire_timestamp = 4;</code>
         * 
         * @return Whether the nextFireTimestamp field is set.
         */
        boolean hasNextFireTimestamp();

        /**
         * <code>int64 next_fire_timestamp = 4;</code>
         * 
         * @return The nextFireTimestamp.
         */
        long getNextFireTimestamp();
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.ExpireTimerData}
     */
    public static final class ExpireTimerData extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.ExpireTimerData)
            ExpireTimerDataOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use ExpireTimerData.newBuilder() to construct.
        private ExpireTimerData(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private ExpireTimerData() {
            entryPointId_ = "";
            className_ = "";
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new ExpireTimerData();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private ExpireTimerData(
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
                        case 8: {
                            bitField0_ |= 0x00000001;
                            handleId_ = input.readInt64();
                            break;
                        }
                        case 18: {
                            java.lang.String s = input.readStringRequireUtf8();
                            bitField0_ |= 0x00000002;
                            entryPointId_ = s;
                            break;
                        }
                        case 26: {
                            java.lang.String s = input.readStringRequireUtf8();
                            bitField0_ |= 0x00000004;
                            className_ = s;
                            break;
                        }
                        case 32: {
                            bitField0_ |= 0x00000008;
                            nextFireTimestamp_ = input.readInt64();
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
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData.Builder.class);
        }

        private int bitField0_;
        public static final int HANDLE_ID_FIELD_NUMBER = 1;
        private long handleId_;

        /**
         * <code>int64 handle_id = 1;</code>
         * 
         * @return Whether the handleId field is set.
         */
        @java.lang.Override
        public boolean hasHandleId() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <code>int64 handle_id = 1;</code>
         * 
         * @return The handleId.
         */
        @java.lang.Override
        public long getHandleId() {
            return handleId_;
        }

        public static final int ENTRY_POINT_ID_FIELD_NUMBER = 2;
        private volatile java.lang.Object entryPointId_;

        /**
         * <code>string entry_point_id = 2;</code>
         * 
         * @return Whether the entryPointId field is set.
         */
        @java.lang.Override
        public boolean hasEntryPointId() {
            return ((bitField0_ & 0x00000002) != 0);
        }

        /**
         * <code>string entry_point_id = 2;</code>
         * 
         * @return The entryPointId.
         */
        @java.lang.Override
        public java.lang.String getEntryPointId() {
            java.lang.Object ref = entryPointId_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                entryPointId_ = s;
                return s;
            }
        }

        /**
         * <code>string entry_point_id = 2;</code>
         * 
         * @return The bytes for entryPointId.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString
                getEntryPointIdBytes() {
            java.lang.Object ref = entryPointId_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                entryPointId_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int CLASS_NAME_FIELD_NUMBER = 3;
        private volatile java.lang.Object className_;

        /**
         * <code>string class_name = 3;</code>
         * 
         * @return Whether the className field is set.
         */
        @java.lang.Override
        public boolean hasClassName() {
            return ((bitField0_ & 0x00000004) != 0);
        }

        /**
         * <code>string class_name = 3;</code>
         * 
         * @return The className.
         */
        @java.lang.Override
        public java.lang.String getClassName() {
            java.lang.Object ref = className_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                className_ = s;
                return s;
            }
        }

        /**
         * <code>string class_name = 3;</code>
         * 
         * @return The bytes for className.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString
                getClassNameBytes() {
            java.lang.Object ref = className_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                className_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int NEXT_FIRE_TIMESTAMP_FIELD_NUMBER = 4;
        private long nextFireTimestamp_;

        /**
         * <code>int64 next_fire_timestamp = 4;</code>
         * 
         * @return Whether the nextFireTimestamp field is set.
         */
        @java.lang.Override
        public boolean hasNextFireTimestamp() {
            return ((bitField0_ & 0x00000008) != 0);
        }

        /**
         * <code>int64 next_fire_timestamp = 4;</code>
         * 
         * @return The nextFireTimestamp.
         */
        @java.lang.Override
        public long getNextFireTimestamp() {
            return nextFireTimestamp_;
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
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeInt64(1, handleId_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                com.google.protobuf.GeneratedMessageV3.writeString(output, 2, entryPointId_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                com.google.protobuf.GeneratedMessageV3.writeString(output, 3, className_);
            }
            if (((bitField0_ & 0x00000008) != 0)) {
                output.writeInt64(4, nextFireTimestamp_);
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(1, handleId_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, entryPointId_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, className_);
            }
            if (((bitField0_ & 0x00000008) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(4, nextFireTimestamp_);
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData) obj;

            if (hasHandleId() != other.hasHandleId())
                return false;
            if (hasHandleId()) {
                if (getHandleId() != other.getHandleId())
                    return false;
            }
            if (hasEntryPointId() != other.hasEntryPointId())
                return false;
            if (hasEntryPointId()) {
                if (!getEntryPointId()
                        .equals(other.getEntryPointId()))
                    return false;
            }
            if (hasClassName() != other.hasClassName())
                return false;
            if (hasClassName()) {
                if (!getClassName()
                        .equals(other.getClassName()))
                    return false;
            }
            if (hasNextFireTimestamp() != other.hasNextFireTimestamp())
                return false;
            if (hasNextFireTimestamp()) {
                if (getNextFireTimestamp() != other.getNextFireTimestamp())
                    return false;
            }
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
            if (hasHandleId()) {
                hash = (37 * hash) + HANDLE_ID_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getHandleId());
            }
            if (hasEntryPointId()) {
                hash = (37 * hash) + ENTRY_POINT_ID_FIELD_NUMBER;
                hash = (53 * hash) + getEntryPointId().hashCode();
            }
            if (hasClassName()) {
                hash = (37 * hash) + CLASS_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getClassName().hashCode();
            }
            if (hasNextFireTimestamp()) {
                hash = (37 * hash) + NEXT_FIRE_TIMESTAMP_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getNextFireTimestamp());
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.ExpireTimerData}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.ExpireTimerData)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerDataOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData.class,
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData.newBuilder()
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
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                handleId_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                entryPointId_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                className_ = "";
                bitField0_ = (bitField0_ & ~0x00000004);
                nextFireTimestamp_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000008);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) != 0)) {
                    result.handleId_ = handleId_;
                    to_bitField0_ |= 0x00000001;
                }
                if (((from_bitField0_ & 0x00000002) != 0)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.entryPointId_ = entryPointId_;
                if (((from_bitField0_ & 0x00000004) != 0)) {
                    to_bitField0_ |= 0x00000004;
                }
                result.className_ = className_;
                if (((from_bitField0_ & 0x00000008) != 0)) {
                    result.nextFireTimestamp_ = nextFireTimestamp_;
                    to_bitField0_ |= 0x00000008;
                }
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData.getDefaultInstance())
                    return this;
                if (other.hasHandleId()) {
                    setHandleId(other.getHandleId());
                }
                if (other.hasEntryPointId()) {
                    bitField0_ |= 0x00000002;
                    entryPointId_ = other.entryPointId_;
                    onChanged();
                }
                if (other.hasClassName()) {
                    bitField0_ |= 0x00000004;
                    className_ = other.className_;
                    onChanged();
                }
                if (other.hasNextFireTimestamp()) {
                    setNextFireTimestamp(other.getNextFireTimestamp());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private long handleId_;

            /**
             * <code>int64 handle_id = 1;</code>
             * 
             * @return Whether the handleId field is set.
             */
            @java.lang.Override
            public boolean hasHandleId() {
                return ((bitField0_ & 0x00000001) != 0);
            }

            /**
             * <code>int64 handle_id = 1;</code>
             * 
             * @return The handleId.
             */
            @java.lang.Override
            public long getHandleId() {
                return handleId_;
            }

            /**
             * <code>int64 handle_id = 1;</code>
             * 
             * @param value The handleId to set.
             * @return This builder for chaining.
             */
            public Builder setHandleId(long value) {
                bitField0_ |= 0x00000001;
                handleId_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 handle_id = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearHandleId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                handleId_ = 0L;
                onChanged();
                return this;
            }

            private java.lang.Object entryPointId_ = "";

            /**
             * <code>string entry_point_id = 2;</code>
             * 
             * @return Whether the entryPointId field is set.
             */
            public boolean hasEntryPointId() {
                return ((bitField0_ & 0x00000002) != 0);
            }

            /**
             * <code>string entry_point_id = 2;</code>
             * 
             * @return The entryPointId.
             */
            public java.lang.String getEntryPointId() {
                java.lang.Object ref = entryPointId_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    entryPointId_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            /**
             * <code>string entry_point_id = 2;</code>
             * 
             * @return The bytes for entryPointId.
             */
            public com.google.protobuf.ByteString
                    getEntryPointIdBytes() {
                java.lang.Object ref = entryPointId_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    entryPointId_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            /**
             * <code>string entry_point_id = 2;</code>
             * 
             * @param value The entryPointId to set.
             * @return This builder for chaining.
             */
            public Builder setEntryPointId(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                entryPointId_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>string entry_point_id = 2;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearEntryPointId() {
                bitField0_ = (bitField0_ & ~0x00000002);
                entryPointId_ = getDefaultInstance().getEntryPointId();
                onChanged();
                return this;
            }

            /**
             * <code>string entry_point_id = 2;</code>
             * 
             * @param value The bytes for entryPointId to set.
             * @return This builder for chaining.
             */
            public Builder setEntryPointIdBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                checkByteStringIsUtf8(value);
                bitField0_ |= 0x00000002;
                entryPointId_ = value;
                onChanged();
                return this;
            }

            private java.lang.Object className_ = "";

            /**
             * <code>string class_name = 3;</code>
             * 
             * @return Whether the className field is set.
             */
            public boolean hasClassName() {
                return ((bitField0_ & 0x00000004) != 0);
            }

            /**
             * <code>string class_name = 3;</code>
             * 
             * @return The className.
             */
            public java.lang.String getClassName() {
                java.lang.Object ref = className_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    className_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            /**
             * <code>string class_name = 3;</code>
             * 
             * @return The bytes for className.
             */
            public com.google.protobuf.ByteString
                    getClassNameBytes() {
                java.lang.Object ref = className_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    className_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            /**
             * <code>string class_name = 3;</code>
             * 
             * @param value The className to set.
             * @return This builder for chaining.
             */
            public Builder setClassName(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000004;
                className_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>string class_name = 3;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearClassName() {
                bitField0_ = (bitField0_ & ~0x00000004);
                className_ = getDefaultInstance().getClassName();
                onChanged();
                return this;
            }

            /**
             * <code>string class_name = 3;</code>
             * 
             * @param value The bytes for className to set.
             * @return This builder for chaining.
             */
            public Builder setClassNameBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                checkByteStringIsUtf8(value);
                bitField0_ |= 0x00000004;
                className_ = value;
                onChanged();
                return this;
            }

            private long nextFireTimestamp_;

            /**
             * <code>int64 next_fire_timestamp = 4;</code>
             * 
             * @return Whether the nextFireTimestamp field is set.
             */
            @java.lang.Override
            public boolean hasNextFireTimestamp() {
                return ((bitField0_ & 0x00000008) != 0);
            }

            /**
             * <code>int64 next_fire_timestamp = 4;</code>
             * 
             * @return The nextFireTimestamp.
             */
            @java.lang.Override
            public long getNextFireTimestamp() {
                return nextFireTimestamp_;
            }

            /**
             * <code>int64 next_fire_timestamp = 4;</code>
             * 
             * @param value The nextFireTimestamp to set.
             * @return This builder for chaining.
             */
            public Builder setNextFireTimestamp(long value) {
                bitField0_ |= 0x00000008;
                nextFireTimestamp_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 next_fire_timestamp = 4;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearNextFireTimestamp() {
                bitField0_ = (bitField0_ & ~0x00000008);
                nextFireTimestamp_ = 0L;
                onChanged();
                return this;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.ExpireTimerData)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.ExpireTimerData)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<ExpireTimerData> PARSER = new com.google.protobuf.AbstractParser<ExpireTimerData>() {
            @java.lang.Override
            public ExpireTimerData parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new ExpireTimerData(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<ExpireTimerData> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<ExpireTimerData> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ExpireTimerData getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface TimerNodeTimerDataOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.TimerNodeTimerData)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>int32 node_id = 1;</code>
         * 
         * @return Whether the nodeId field is set.
         */
        boolean hasNodeId();

        /**
         * <code>int32 node_id = 1;</code>
         * 
         * @return The nodeId.
         */
        int getNodeId();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
         * 
         * @return Whether the tuple field is set.
         */
        boolean hasTuple();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
         * 
         * @return The tuple.
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple getTuple();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TupleOrBuilder getTupleOrBuilder();
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.TimerNodeTimerData}
     */
    public static final class TimerNodeTimerData extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.TimerNodeTimerData)
            TimerNodeTimerDataOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use TimerNodeTimerData.newBuilder() to construct.
        private TimerNodeTimerData(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private TimerNodeTimerData() {
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new TimerNodeTimerData();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private TimerNodeTimerData(
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
                        case 8: {
                            bitField0_ |= 0x00000001;
                            nodeId_ = input.readInt32();
                            break;
                        }
                        case 18: {
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.Builder subBuilder = null;
                            if (((bitField0_ & 0x00000002) != 0)) {
                                subBuilder = tuple_.toBuilder();
                            }
                            tuple_ = input.readMessage(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.parser(), extensionRegistry);
                            if (subBuilder != null) {
                                subBuilder.mergeFrom(tuple_);
                                tuple_ = subBuilder.buildPartial();
                            }
                            bitField0_ |= 0x00000002;
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
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData.class,
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData.Builder.class);
        }

        private int bitField0_;
        public static final int NODE_ID_FIELD_NUMBER = 1;
        private int nodeId_;

        /**
         * <code>int32 node_id = 1;</code>
         * 
         * @return Whether the nodeId field is set.
         */
        @java.lang.Override
        public boolean hasNodeId() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <code>int32 node_id = 1;</code>
         * 
         * @return The nodeId.
         */
        @java.lang.Override
        public int getNodeId() {
            return nodeId_;
        }

        public static final int TUPLE_FIELD_NUMBER = 2;
        private org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple tuple_;

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
         * 
         * @return Whether the tuple field is set.
         */
        @java.lang.Override
        public boolean hasTuple() {
            return ((bitField0_ & 0x00000002) != 0);
        }

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
         * 
         * @return The tuple.
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple getTuple() {
            return tuple_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.getDefaultInstance() : tuple_;
        }

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TupleOrBuilder getTupleOrBuilder() {
            return tuple_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.getDefaultInstance() : tuple_;
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
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeInt32(1, nodeId_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                output.writeMessage(2, getTuple());
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(1, nodeId_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(2, getTuple());
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData) obj;

            if (hasNodeId() != other.hasNodeId())
                return false;
            if (hasNodeId()) {
                if (getNodeId() != other.getNodeId())
                    return false;
            }
            if (hasTuple() != other.hasTuple())
                return false;
            if (hasTuple()) {
                if (!getTuple()
                        .equals(other.getTuple()))
                    return false;
            }
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
            if (hasNodeId()) {
                hash = (37 * hash) + NODE_ID_FIELD_NUMBER;
                hash = (53 * hash) + getNodeId();
            }
            if (hasTuple()) {
                hash = (37 * hash) + TUPLE_FIELD_NUMBER;
                hash = (53 * hash) + getTuple().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.TimerNodeTimerData}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.TimerNodeTimerData)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerDataOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData.class,
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData.newBuilder()
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
                    getTupleFieldBuilder();
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                nodeId_ = 0;
                bitField0_ = (bitField0_ & ~0x00000001);
                if (tupleBuilder_ == null) {
                    tuple_ = null;
                } else {
                    tupleBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) != 0)) {
                    result.nodeId_ = nodeId_;
                    to_bitField0_ |= 0x00000001;
                }
                if (((from_bitField0_ & 0x00000002) != 0)) {
                    if (tupleBuilder_ == null) {
                        result.tuple_ = tuple_;
                    } else {
                        result.tuple_ = tupleBuilder_.build();
                    }
                    to_bitField0_ |= 0x00000002;
                }
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData.getDefaultInstance())
                    return this;
                if (other.hasNodeId()) {
                    setNodeId(other.getNodeId());
                }
                if (other.hasTuple()) {
                    mergeTuple(other.getTuple());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private int nodeId_;

            /**
             * <code>int32 node_id = 1;</code>
             * 
             * @return Whether the nodeId field is set.
             */
            @java.lang.Override
            public boolean hasNodeId() {
                return ((bitField0_ & 0x00000001) != 0);
            }

            /**
             * <code>int32 node_id = 1;</code>
             * 
             * @return The nodeId.
             */
            @java.lang.Override
            public int getNodeId() {
                return nodeId_;
            }

            /**
             * <code>int32 node_id = 1;</code>
             * 
             * @param value The nodeId to set.
             * @return This builder for chaining.
             */
            public Builder setNodeId(int value) {
                bitField0_ |= 0x00000001;
                nodeId_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int32 node_id = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearNodeId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                nodeId_ = 0;
                onChanged();
                return this;
            }

            private org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple tuple_;
            private com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TupleOrBuilder> tupleBuilder_;

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             * 
             * @return Whether the tuple field is set.
             */
            public boolean hasTuple() {
                return ((bitField0_ & 0x00000002) != 0);
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             * 
             * @return The tuple.
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple getTuple() {
                if (tupleBuilder_ == null) {
                    return tuple_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.getDefaultInstance() : tuple_;
                } else {
                    return tupleBuilder_.getMessage();
                }
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             */
            public Builder setTuple(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple value) {
                if (tupleBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    tuple_ = value;
                    onChanged();
                } else {
                    tupleBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             */
            public Builder setTuple(
                    org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.Builder builderForValue) {
                if (tupleBuilder_ == null) {
                    tuple_ = builderForValue.build();
                    onChanged();
                } else {
                    tupleBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             */
            public Builder mergeTuple(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple value) {
                if (tupleBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) != 0) &&
                            tuple_ != null &&
                            tuple_ != org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.getDefaultInstance()) {
                        tuple_ =
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.newBuilder(tuple_).mergeFrom(value).buildPartial();
                    } else {
                        tuple_ = value;
                    }
                    onChanged();
                } else {
                    tupleBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000002;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             */
            public Builder clearTuple() {
                if (tupleBuilder_ == null) {
                    tuple_ = null;
                    onChanged();
                } else {
                    tupleBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.Builder getTupleBuilder() {
                bitField0_ |= 0x00000002;
                onChanged();
                return getTupleFieldBuilder().getBuilder();
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TupleOrBuilder getTupleOrBuilder() {
                if (tupleBuilder_ != null) {
                    return tupleBuilder_.getMessageOrBuilder();
                } else {
                    return tuple_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.getDefaultInstance() : tuple_;
                }
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Tuple tuple = 2;</code>
             */
            private com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TupleOrBuilder>
                    getTupleFieldBuilder() {
                if (tupleBuilder_ == null) {
                    tupleBuilder_ =
                            new com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TupleOrBuilder>(
                                    getTuple(),
                                    getParentForChildren(),
                                    isClean());
                    tuple_ = null;
                }
                return tupleBuilder_;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.TimerNodeTimerData)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.TimerNodeTimerData)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<TimerNodeTimerData> PARSER = new com.google.protobuf.AbstractParser<TimerNodeTimerData>() {
            @java.lang.Override
            public TimerNodeTimerData parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new TimerNodeTimerData(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<TimerNodeTimerData> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<TimerNodeTimerData> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TimerNodeTimerData getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface ProcessTimerOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.ProcessTimer)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <pre>
         * timer instance
         * </pre>
         *
         * <code>int64 id = 1;</code>
         * 
         * @return Whether the id field is set.
         */
        boolean hasId();

        /**
         * <pre>
         * timer instance
         * </pre>
         *
         * <code>int64 id = 1;</code>
         * 
         * @return The id.
         */
        long getId();

        /**
         * <code>string timer_id = 2;</code>
         * 
         * @return Whether the timerId field is set.
         */
        boolean hasTimerId();

        /**
         * <code>string timer_id = 2;</code>
         * 
         * @return The timerId.
         */
        java.lang.String getTimerId();

        /**
         * <code>string timer_id = 2;</code>
         * 
         * @return The bytes for timerId.
         */
        com.google.protobuf.ByteString
                getTimerIdBytes();

        /**
         * <code>int64 delay = 3;</code>
         * 
         * @return Whether the delay field is set.
         */
        boolean hasDelay();

        /**
         * <code>int64 delay = 3;</code>
         * 
         * @return The delay.
         */
        long getDelay();

        /**
         * <code>int64 period = 4;</code>
         * 
         * @return Whether the period field is set.
         */
        boolean hasPeriod();

        /**
         * <code>int64 period = 4;</code>
         * 
         * @return The period.
         */
        long getPeriod();

        /**
         * <code>string process_instance_id = 5;</code>
         * 
         * @return Whether the processInstanceId field is set.
         */
        boolean hasProcessInstanceId();

        /**
         * <code>string process_instance_id = 5;</code>
         * 
         * @return The processInstanceId.
         */
        java.lang.String getProcessInstanceId();

        /**
         * <code>string process_instance_id = 5;</code>
         * 
         * @return The bytes for processInstanceId.
         */
        com.google.protobuf.ByteString
                getProcessInstanceIdBytes();

        /**
         * <code>int64 activated_time = 6;</code>
         * 
         * @return Whether the activatedTime field is set.
         */
        boolean hasActivatedTime();

        /**
         * <code>int64 activated_time = 6;</code>
         * 
         * @return The activatedTime.
         */
        long getActivatedTime();

        /**
         * <code>int64 last_triggered = 7;</code>
         * 
         * @return Whether the lastTriggered field is set.
         */
        boolean hasLastTriggered();

        /**
         * <code>int64 last_triggered = 7;</code>
         * 
         * @return The lastTriggered.
         */
        long getLastTriggered();

        /**
         * <code>int32 repeatLimit = 10;</code>
         * 
         * @return Whether the repeatLimit field is set.
         */
        boolean hasRepeatLimit();

        /**
         * <code>int32 repeatLimit = 10;</code>
         * 
         * @return The repeatLimit.
         */
        int getRepeatLimit();

        /**
         * <pre>
         * trigger
         * </pre>
         *
         * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
         * 
         * @return Whether the trigger field is set.
         */
        boolean hasTrigger();

        /**
         * <pre>
         * trigger
         * </pre>
         *
         * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
         * 
         * @return The trigger.
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getTrigger();

        /**
         * <pre>
         * trigger
         * </pre>
         *
         * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder getTriggerOrBuilder();
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.ProcessTimer}
     */
    public static final class ProcessTimer extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.ProcessTimer)
            ProcessTimerOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use ProcessTimer.newBuilder() to construct.
        private ProcessTimer(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private ProcessTimer() {
            timerId_ = "";
            processInstanceId_ = "";
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new ProcessTimer();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private ProcessTimer(
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
                        case 8: {
                            bitField0_ |= 0x00000001;
                            id_ = input.readInt64();
                            break;
                        }
                        case 18: {
                            java.lang.String s = input.readStringRequireUtf8();
                            bitField0_ |= 0x00000002;
                            timerId_ = s;
                            break;
                        }
                        case 24: {
                            bitField0_ |= 0x00000004;
                            delay_ = input.readInt64();
                            break;
                        }
                        case 32: {
                            bitField0_ |= 0x00000008;
                            period_ = input.readInt64();
                            break;
                        }
                        case 42: {
                            java.lang.String s = input.readStringRequireUtf8();
                            bitField0_ |= 0x00000010;
                            processInstanceId_ = s;
                            break;
                        }
                        case 48: {
                            bitField0_ |= 0x00000020;
                            activatedTime_ = input.readInt64();
                            break;
                        }
                        case 56: {
                            bitField0_ |= 0x00000040;
                            lastTriggered_ = input.readInt64();
                            break;
                        }
                        case 80: {
                            bitField0_ |= 0x00000080;
                            repeatLimit_ = input.readInt32();
                            break;
                        }
                        case 90: {
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder subBuilder = null;
                            if (((bitField0_ & 0x00000100) != 0)) {
                                subBuilder = trigger_.toBuilder();
                            }
                            trigger_ = input.readMessage(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.parser(), extensionRegistry);
                            if (subBuilder != null) {
                                subBuilder.mergeFrom(trigger_);
                                trigger_ = subBuilder.buildPartial();
                            }
                            bitField0_ |= 0x00000100;
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
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder.class);
        }

        private int bitField0_;
        public static final int ID_FIELD_NUMBER = 1;
        private long id_;

        /**
         * <pre>
         * timer instance
         * </pre>
         *
         * <code>int64 id = 1;</code>
         * 
         * @return Whether the id field is set.
         */
        @java.lang.Override
        public boolean hasId() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <pre>
         * timer instance
         * </pre>
         *
         * <code>int64 id = 1;</code>
         * 
         * @return The id.
         */
        @java.lang.Override
        public long getId() {
            return id_;
        }

        public static final int TIMER_ID_FIELD_NUMBER = 2;
        private volatile java.lang.Object timerId_;

        /**
         * <code>string timer_id = 2;</code>
         * 
         * @return Whether the timerId field is set.
         */
        @java.lang.Override
        public boolean hasTimerId() {
            return ((bitField0_ & 0x00000002) != 0);
        }

        /**
         * <code>string timer_id = 2;</code>
         * 
         * @return The timerId.
         */
        @java.lang.Override
        public java.lang.String getTimerId() {
            java.lang.Object ref = timerId_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                timerId_ = s;
                return s;
            }
        }

        /**
         * <code>string timer_id = 2;</code>
         * 
         * @return The bytes for timerId.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString
                getTimerIdBytes() {
            java.lang.Object ref = timerId_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                timerId_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int DELAY_FIELD_NUMBER = 3;
        private long delay_;

        /**
         * <code>int64 delay = 3;</code>
         * 
         * @return Whether the delay field is set.
         */
        @java.lang.Override
        public boolean hasDelay() {
            return ((bitField0_ & 0x00000004) != 0);
        }

        /**
         * <code>int64 delay = 3;</code>
         * 
         * @return The delay.
         */
        @java.lang.Override
        public long getDelay() {
            return delay_;
        }

        public static final int PERIOD_FIELD_NUMBER = 4;
        private long period_;

        /**
         * <code>int64 period = 4;</code>
         * 
         * @return Whether the period field is set.
         */
        @java.lang.Override
        public boolean hasPeriod() {
            return ((bitField0_ & 0x00000008) != 0);
        }

        /**
         * <code>int64 period = 4;</code>
         * 
         * @return The period.
         */
        @java.lang.Override
        public long getPeriod() {
            return period_;
        }

        public static final int PROCESS_INSTANCE_ID_FIELD_NUMBER = 5;
        private volatile java.lang.Object processInstanceId_;

        /**
         * <code>string process_instance_id = 5;</code>
         * 
         * @return Whether the processInstanceId field is set.
         */
        @java.lang.Override
        public boolean hasProcessInstanceId() {
            return ((bitField0_ & 0x00000010) != 0);
        }

        /**
         * <code>string process_instance_id = 5;</code>
         * 
         * @return The processInstanceId.
         */
        @java.lang.Override
        public java.lang.String getProcessInstanceId() {
            java.lang.Object ref = processInstanceId_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                processInstanceId_ = s;
                return s;
            }
        }

        /**
         * <code>string process_instance_id = 5;</code>
         * 
         * @return The bytes for processInstanceId.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString
                getProcessInstanceIdBytes() {
            java.lang.Object ref = processInstanceId_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                processInstanceId_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int ACTIVATED_TIME_FIELD_NUMBER = 6;
        private long activatedTime_;

        /**
         * <code>int64 activated_time = 6;</code>
         * 
         * @return Whether the activatedTime field is set.
         */
        @java.lang.Override
        public boolean hasActivatedTime() {
            return ((bitField0_ & 0x00000020) != 0);
        }

        /**
         * <code>int64 activated_time = 6;</code>
         * 
         * @return The activatedTime.
         */
        @java.lang.Override
        public long getActivatedTime() {
            return activatedTime_;
        }

        public static final int LAST_TRIGGERED_FIELD_NUMBER = 7;
        private long lastTriggered_;

        /**
         * <code>int64 last_triggered = 7;</code>
         * 
         * @return Whether the lastTriggered field is set.
         */
        @java.lang.Override
        public boolean hasLastTriggered() {
            return ((bitField0_ & 0x00000040) != 0);
        }

        /**
         * <code>int64 last_triggered = 7;</code>
         * 
         * @return The lastTriggered.
         */
        @java.lang.Override
        public long getLastTriggered() {
            return lastTriggered_;
        }

        public static final int REPEATLIMIT_FIELD_NUMBER = 10;
        private int repeatLimit_;

        /**
         * <code>int32 repeatLimit = 10;</code>
         * 
         * @return Whether the repeatLimit field is set.
         */
        @java.lang.Override
        public boolean hasRepeatLimit() {
            return ((bitField0_ & 0x00000080) != 0);
        }

        /**
         * <code>int32 repeatLimit = 10;</code>
         * 
         * @return The repeatLimit.
         */
        @java.lang.Override
        public int getRepeatLimit() {
            return repeatLimit_;
        }

        public static final int TRIGGER_FIELD_NUMBER = 11;
        private org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger trigger_;

        /**
         * <pre>
         * trigger
         * </pre>
         *
         * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
         * 
         * @return Whether the trigger field is set.
         */
        @java.lang.Override
        public boolean hasTrigger() {
            return ((bitField0_ & 0x00000100) != 0);
        }

        /**
         * <pre>
         * trigger
         * </pre>
         *
         * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
         * 
         * @return The trigger.
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getTrigger() {
            return trigger_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance() : trigger_;
        }

        /**
         * <pre>
         * trigger
         * </pre>
         *
         * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder getTriggerOrBuilder() {
            return trigger_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance() : trigger_;
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
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeInt64(1, id_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                com.google.protobuf.GeneratedMessageV3.writeString(output, 2, timerId_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                output.writeInt64(3, delay_);
            }
            if (((bitField0_ & 0x00000008) != 0)) {
                output.writeInt64(4, period_);
            }
            if (((bitField0_ & 0x00000010) != 0)) {
                com.google.protobuf.GeneratedMessageV3.writeString(output, 5, processInstanceId_);
            }
            if (((bitField0_ & 0x00000020) != 0)) {
                output.writeInt64(6, activatedTime_);
            }
            if (((bitField0_ & 0x00000040) != 0)) {
                output.writeInt64(7, lastTriggered_);
            }
            if (((bitField0_ & 0x00000080) != 0)) {
                output.writeInt32(10, repeatLimit_);
            }
            if (((bitField0_ & 0x00000100) != 0)) {
                output.writeMessage(11, getTrigger());
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(1, id_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, timerId_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(3, delay_);
            }
            if (((bitField0_ & 0x00000008) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(4, period_);
            }
            if (((bitField0_ & 0x00000010) != 0)) {
                size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, processInstanceId_);
            }
            if (((bitField0_ & 0x00000020) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(6, activatedTime_);
            }
            if (((bitField0_ & 0x00000040) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(7, lastTriggered_);
            }
            if (((bitField0_ & 0x00000080) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(10, repeatLimit_);
            }
            if (((bitField0_ & 0x00000100) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(11, getTrigger());
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer) obj;

            if (hasId() != other.hasId())
                return false;
            if (hasId()) {
                if (getId() != other.getId())
                    return false;
            }
            if (hasTimerId() != other.hasTimerId())
                return false;
            if (hasTimerId()) {
                if (!getTimerId()
                        .equals(other.getTimerId()))
                    return false;
            }
            if (hasDelay() != other.hasDelay())
                return false;
            if (hasDelay()) {
                if (getDelay() != other.getDelay())
                    return false;
            }
            if (hasPeriod() != other.hasPeriod())
                return false;
            if (hasPeriod()) {
                if (getPeriod() != other.getPeriod())
                    return false;
            }
            if (hasProcessInstanceId() != other.hasProcessInstanceId())
                return false;
            if (hasProcessInstanceId()) {
                if (!getProcessInstanceId()
                        .equals(other.getProcessInstanceId()))
                    return false;
            }
            if (hasActivatedTime() != other.hasActivatedTime())
                return false;
            if (hasActivatedTime()) {
                if (getActivatedTime() != other.getActivatedTime())
                    return false;
            }
            if (hasLastTriggered() != other.hasLastTriggered())
                return false;
            if (hasLastTriggered()) {
                if (getLastTriggered() != other.getLastTriggered())
                    return false;
            }
            if (hasRepeatLimit() != other.hasRepeatLimit())
                return false;
            if (hasRepeatLimit()) {
                if (getRepeatLimit() != other.getRepeatLimit())
                    return false;
            }
            if (hasTrigger() != other.hasTrigger())
                return false;
            if (hasTrigger()) {
                if (!getTrigger()
                        .equals(other.getTrigger()))
                    return false;
            }
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
            if (hasId()) {
                hash = (37 * hash) + ID_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getId());
            }
            if (hasTimerId()) {
                hash = (37 * hash) + TIMER_ID_FIELD_NUMBER;
                hash = (53 * hash) + getTimerId().hashCode();
            }
            if (hasDelay()) {
                hash = (37 * hash) + DELAY_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getDelay());
            }
            if (hasPeriod()) {
                hash = (37 * hash) + PERIOD_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getPeriod());
            }
            if (hasProcessInstanceId()) {
                hash = (37 * hash) + PROCESS_INSTANCE_ID_FIELD_NUMBER;
                hash = (53 * hash) + getProcessInstanceId().hashCode();
            }
            if (hasActivatedTime()) {
                hash = (37 * hash) + ACTIVATED_TIME_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getActivatedTime());
            }
            if (hasLastTriggered()) {
                hash = (37 * hash) + LAST_TRIGGERED_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getLastTriggered());
            }
            if (hasRepeatLimit()) {
                hash = (37 * hash) + REPEATLIMIT_FIELD_NUMBER;
                hash = (53 * hash) + getRepeatLimit();
            }
            if (hasTrigger()) {
                hash = (37 * hash) + TRIGGER_FIELD_NUMBER;
                hash = (53 * hash) + getTrigger().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.ProcessTimer}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.ProcessTimer)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimerOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.newBuilder()
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
                    getTriggerFieldBuilder();
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                id_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                timerId_ = "";
                bitField0_ = (bitField0_ & ~0x00000002);
                delay_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000004);
                period_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000008);
                processInstanceId_ = "";
                bitField0_ = (bitField0_ & ~0x00000010);
                activatedTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000020);
                lastTriggered_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000040);
                repeatLimit_ = 0;
                bitField0_ = (bitField0_ & ~0x00000080);
                if (triggerBuilder_ == null) {
                    trigger_ = null;
                } else {
                    triggerBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000100);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) != 0)) {
                    result.id_ = id_;
                    to_bitField0_ |= 0x00000001;
                }
                if (((from_bitField0_ & 0x00000002) != 0)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.timerId_ = timerId_;
                if (((from_bitField0_ & 0x00000004) != 0)) {
                    result.delay_ = delay_;
                    to_bitField0_ |= 0x00000004;
                }
                if (((from_bitField0_ & 0x00000008) != 0)) {
                    result.period_ = period_;
                    to_bitField0_ |= 0x00000008;
                }
                if (((from_bitField0_ & 0x00000010) != 0)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.processInstanceId_ = processInstanceId_;
                if (((from_bitField0_ & 0x00000020) != 0)) {
                    result.activatedTime_ = activatedTime_;
                    to_bitField0_ |= 0x00000020;
                }
                if (((from_bitField0_ & 0x00000040) != 0)) {
                    result.lastTriggered_ = lastTriggered_;
                    to_bitField0_ |= 0x00000040;
                }
                if (((from_bitField0_ & 0x00000080) != 0)) {
                    result.repeatLimit_ = repeatLimit_;
                    to_bitField0_ |= 0x00000080;
                }
                if (((from_bitField0_ & 0x00000100) != 0)) {
                    if (triggerBuilder_ == null) {
                        result.trigger_ = trigger_;
                    } else {
                        result.trigger_ = triggerBuilder_.build();
                    }
                    to_bitField0_ |= 0x00000100;
                }
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer.getDefaultInstance())
                    return this;
                if (other.hasId()) {
                    setId(other.getId());
                }
                if (other.hasTimerId()) {
                    bitField0_ |= 0x00000002;
                    timerId_ = other.timerId_;
                    onChanged();
                }
                if (other.hasDelay()) {
                    setDelay(other.getDelay());
                }
                if (other.hasPeriod()) {
                    setPeriod(other.getPeriod());
                }
                if (other.hasProcessInstanceId()) {
                    bitField0_ |= 0x00000010;
                    processInstanceId_ = other.processInstanceId_;
                    onChanged();
                }
                if (other.hasActivatedTime()) {
                    setActivatedTime(other.getActivatedTime());
                }
                if (other.hasLastTriggered()) {
                    setLastTriggered(other.getLastTriggered());
                }
                if (other.hasRepeatLimit()) {
                    setRepeatLimit(other.getRepeatLimit());
                }
                if (other.hasTrigger()) {
                    mergeTrigger(other.getTrigger());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private long id_;

            /**
             * <pre>
             * timer instance
             * </pre>
             *
             * <code>int64 id = 1;</code>
             * 
             * @return Whether the id field is set.
             */
            @java.lang.Override
            public boolean hasId() {
                return ((bitField0_ & 0x00000001) != 0);
            }

            /**
             * <pre>
             * timer instance
             * </pre>
             *
             * <code>int64 id = 1;</code>
             * 
             * @return The id.
             */
            @java.lang.Override
            public long getId() {
                return id_;
            }

            /**
             * <pre>
             * timer instance
             * </pre>
             *
             * <code>int64 id = 1;</code>
             * 
             * @param value The id to set.
             * @return This builder for chaining.
             */
            public Builder setId(long value) {
                bitField0_ |= 0x00000001;
                id_ = value;
                onChanged();
                return this;
            }

            /**
             * <pre>
             * timer instance
             * </pre>
             *
             * <code>int64 id = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearId() {
                bitField0_ = (bitField0_ & ~0x00000001);
                id_ = 0L;
                onChanged();
                return this;
            }

            private java.lang.Object timerId_ = "";

            /**
             * <code>string timer_id = 2;</code>
             * 
             * @return Whether the timerId field is set.
             */
            public boolean hasTimerId() {
                return ((bitField0_ & 0x00000002) != 0);
            }

            /**
             * <code>string timer_id = 2;</code>
             * 
             * @return The timerId.
             */
            public java.lang.String getTimerId() {
                java.lang.Object ref = timerId_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    timerId_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            /**
             * <code>string timer_id = 2;</code>
             * 
             * @return The bytes for timerId.
             */
            public com.google.protobuf.ByteString
                    getTimerIdBytes() {
                java.lang.Object ref = timerId_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    timerId_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            /**
             * <code>string timer_id = 2;</code>
             * 
             * @param value The timerId to set.
             * @return This builder for chaining.
             */
            public Builder setTimerId(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                timerId_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>string timer_id = 2;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearTimerId() {
                bitField0_ = (bitField0_ & ~0x00000002);
                timerId_ = getDefaultInstance().getTimerId();
                onChanged();
                return this;
            }

            /**
             * <code>string timer_id = 2;</code>
             * 
             * @param value The bytes for timerId to set.
             * @return This builder for chaining.
             */
            public Builder setTimerIdBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                checkByteStringIsUtf8(value);
                bitField0_ |= 0x00000002;
                timerId_ = value;
                onChanged();
                return this;
            }

            private long delay_;

            /**
             * <code>int64 delay = 3;</code>
             * 
             * @return Whether the delay field is set.
             */
            @java.lang.Override
            public boolean hasDelay() {
                return ((bitField0_ & 0x00000004) != 0);
            }

            /**
             * <code>int64 delay = 3;</code>
             * 
             * @return The delay.
             */
            @java.lang.Override
            public long getDelay() {
                return delay_;
            }

            /**
             * <code>int64 delay = 3;</code>
             * 
             * @param value The delay to set.
             * @return This builder for chaining.
             */
            public Builder setDelay(long value) {
                bitField0_ |= 0x00000004;
                delay_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 delay = 3;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearDelay() {
                bitField0_ = (bitField0_ & ~0x00000004);
                delay_ = 0L;
                onChanged();
                return this;
            }

            private long period_;

            /**
             * <code>int64 period = 4;</code>
             * 
             * @return Whether the period field is set.
             */
            @java.lang.Override
            public boolean hasPeriod() {
                return ((bitField0_ & 0x00000008) != 0);
            }

            /**
             * <code>int64 period = 4;</code>
             * 
             * @return The period.
             */
            @java.lang.Override
            public long getPeriod() {
                return period_;
            }

            /**
             * <code>int64 period = 4;</code>
             * 
             * @param value The period to set.
             * @return This builder for chaining.
             */
            public Builder setPeriod(long value) {
                bitField0_ |= 0x00000008;
                period_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 period = 4;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearPeriod() {
                bitField0_ = (bitField0_ & ~0x00000008);
                period_ = 0L;
                onChanged();
                return this;
            }

            private java.lang.Object processInstanceId_ = "";

            /**
             * <code>string process_instance_id = 5;</code>
             * 
             * @return Whether the processInstanceId field is set.
             */
            public boolean hasProcessInstanceId() {
                return ((bitField0_ & 0x00000010) != 0);
            }

            /**
             * <code>string process_instance_id = 5;</code>
             * 
             * @return The processInstanceId.
             */
            public java.lang.String getProcessInstanceId() {
                java.lang.Object ref = processInstanceId_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    processInstanceId_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            /**
             * <code>string process_instance_id = 5;</code>
             * 
             * @return The bytes for processInstanceId.
             */
            public com.google.protobuf.ByteString
                    getProcessInstanceIdBytes() {
                java.lang.Object ref = processInstanceId_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    processInstanceId_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            /**
             * <code>string process_instance_id = 5;</code>
             * 
             * @param value The processInstanceId to set.
             * @return This builder for chaining.
             */
            public Builder setProcessInstanceId(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000010;
                processInstanceId_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>string process_instance_id = 5;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearProcessInstanceId() {
                bitField0_ = (bitField0_ & ~0x00000010);
                processInstanceId_ = getDefaultInstance().getProcessInstanceId();
                onChanged();
                return this;
            }

            /**
             * <code>string process_instance_id = 5;</code>
             * 
             * @param value The bytes for processInstanceId to set.
             * @return This builder for chaining.
             */
            public Builder setProcessInstanceIdBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                checkByteStringIsUtf8(value);
                bitField0_ |= 0x00000010;
                processInstanceId_ = value;
                onChanged();
                return this;
            }

            private long activatedTime_;

            /**
             * <code>int64 activated_time = 6;</code>
             * 
             * @return Whether the activatedTime field is set.
             */
            @java.lang.Override
            public boolean hasActivatedTime() {
                return ((bitField0_ & 0x00000020) != 0);
            }

            /**
             * <code>int64 activated_time = 6;</code>
             * 
             * @return The activatedTime.
             */
            @java.lang.Override
            public long getActivatedTime() {
                return activatedTime_;
            }

            /**
             * <code>int64 activated_time = 6;</code>
             * 
             * @param value The activatedTime to set.
             * @return This builder for chaining.
             */
            public Builder setActivatedTime(long value) {
                bitField0_ |= 0x00000020;
                activatedTime_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 activated_time = 6;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearActivatedTime() {
                bitField0_ = (bitField0_ & ~0x00000020);
                activatedTime_ = 0L;
                onChanged();
                return this;
            }

            private long lastTriggered_;

            /**
             * <code>int64 last_triggered = 7;</code>
             * 
             * @return Whether the lastTriggered field is set.
             */
            @java.lang.Override
            public boolean hasLastTriggered() {
                return ((bitField0_ & 0x00000040) != 0);
            }

            /**
             * <code>int64 last_triggered = 7;</code>
             * 
             * @return The lastTriggered.
             */
            @java.lang.Override
            public long getLastTriggered() {
                return lastTriggered_;
            }

            /**
             * <code>int64 last_triggered = 7;</code>
             * 
             * @param value The lastTriggered to set.
             * @return This builder for chaining.
             */
            public Builder setLastTriggered(long value) {
                bitField0_ |= 0x00000040;
                lastTriggered_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 last_triggered = 7;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearLastTriggered() {
                bitField0_ = (bitField0_ & ~0x00000040);
                lastTriggered_ = 0L;
                onChanged();
                return this;
            }

            private int repeatLimit_;

            /**
             * <code>int32 repeatLimit = 10;</code>
             * 
             * @return Whether the repeatLimit field is set.
             */
            @java.lang.Override
            public boolean hasRepeatLimit() {
                return ((bitField0_ & 0x00000080) != 0);
            }

            /**
             * <code>int32 repeatLimit = 10;</code>
             * 
             * @return The repeatLimit.
             */
            @java.lang.Override
            public int getRepeatLimit() {
                return repeatLimit_;
            }

            /**
             * <code>int32 repeatLimit = 10;</code>
             * 
             * @param value The repeatLimit to set.
             * @return This builder for chaining.
             */
            public Builder setRepeatLimit(int value) {
                bitField0_ |= 0x00000080;
                repeatLimit_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int32 repeatLimit = 10;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearRepeatLimit() {
                bitField0_ = (bitField0_ & ~0x00000080);
                repeatLimit_ = 0;
                onChanged();
                return this;
            }

            private org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger trigger_;
            private com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder> triggerBuilder_;

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             * 
             * @return Whether the trigger field is set.
             */
            public boolean hasTrigger() {
                return ((bitField0_ & 0x00000100) != 0);
            }

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             * 
             * @return The trigger.
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getTrigger() {
                if (triggerBuilder_ == null) {
                    return trigger_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance() : trigger_;
                } else {
                    return triggerBuilder_.getMessage();
                }
            }

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             */
            public Builder setTrigger(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger value) {
                if (triggerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    trigger_ = value;
                    onChanged();
                } else {
                    triggerBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000100;
                return this;
            }

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             */
            public Builder setTrigger(
                    org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder builderForValue) {
                if (triggerBuilder_ == null) {
                    trigger_ = builderForValue.build();
                    onChanged();
                } else {
                    triggerBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000100;
                return this;
            }

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             */
            public Builder mergeTrigger(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger value) {
                if (triggerBuilder_ == null) {
                    if (((bitField0_ & 0x00000100) != 0) &&
                            trigger_ != null &&
                            trigger_ != org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance()) {
                        trigger_ =
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.newBuilder(trigger_).mergeFrom(value).buildPartial();
                    } else {
                        trigger_ = value;
                    }
                    onChanged();
                } else {
                    triggerBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000100;
                return this;
            }

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             */
            public Builder clearTrigger() {
                if (triggerBuilder_ == null) {
                    trigger_ = null;
                    onChanged();
                } else {
                    triggerBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000100);
                return this;
            }

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder getTriggerBuilder() {
                bitField0_ |= 0x00000100;
                onChanged();
                return getTriggerFieldBuilder().getBuilder();
            }

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder getTriggerOrBuilder() {
                if (triggerBuilder_ != null) {
                    return triggerBuilder_.getMessageOrBuilder();
                } else {
                    return trigger_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance() : trigger_;
                }
            }

            /**
             * <pre>
             * trigger
             * </pre>
             *
             * <code>.org.kie.kogito.serialization.protobuf.Trigger trigger = 11;</code>
             */
            private com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder>
                    getTriggerFieldBuilder() {
                if (triggerBuilder_ == null) {
                    triggerBuilder_ =
                            new com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder>(
                                    getTrigger(),
                                    getParentForChildren(),
                                    isClean());
                    trigger_ = null;
                }
                return triggerBuilder_;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.ProcessTimer)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.ProcessTimer)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<ProcessTimer> PARSER = new com.google.protobuf.AbstractParser<ProcessTimer>() {
            @java.lang.Override
            public ProcessTimer parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new ProcessTimer(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<ProcessTimer> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<ProcessTimer> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.ProcessTimer getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface TriggerOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.Trigger)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
         * 
         * @return The enum numeric value on the wire for type.
         */
        int getTypeValue();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
         * 
         * @return The type.
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType getType();

        /**
         * <code>.google.protobuf.Any trigger = 12;</code>
         * 
         * @return Whether the trigger field is set.
         */
        boolean hasTrigger();

        /**
         * <code>.google.protobuf.Any trigger = 12;</code>
         * 
         * @return The trigger.
         */
        com.google.protobuf.Any getTrigger();

        /**
         * <code>.google.protobuf.Any trigger = 12;</code>
         */
        com.google.protobuf.AnyOrBuilder getTriggerOrBuilder();
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.Trigger}
     */
    public static final class Trigger extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.Trigger)
            TriggerOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use Trigger.newBuilder() to construct.
        private Trigger(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private Trigger() {
            type_ = 0;
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new Trigger();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private Trigger(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
                throw new java.lang.NullPointerException();
            }
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
                        case 88: {
                            int rawValue = input.readEnum();

                            type_ = rawValue;
                            break;
                        }
                        case 98: {
                            com.google.protobuf.Any.Builder subBuilder = null;
                            if (trigger_ != null) {
                                subBuilder = trigger_.toBuilder();
                            }
                            trigger_ = input.readMessage(com.google.protobuf.Any.parser(), extensionRegistry);
                            if (subBuilder != null) {
                                subBuilder.mergeFrom(trigger_);
                                trigger_ = subBuilder.buildPartial();
                            }

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
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Trigger_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Trigger_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder.class);
        }

        /**
         * Protobuf enum {@code org.kie.kogito.serialization.protobuf.Trigger.TriggerType}
         */
        public enum TriggerType
                implements com.google.protobuf.ProtocolMessageEnum {
            /**
             * <code>CRON = 0;</code>
             */
            CRON(0),
            /**
             * <code>INTERVAL = 1;</code>
             */
            INTERVAL(1),
            /**
             * <code>POINT_IN_TIME = 2;</code>
             */
            POINT_IN_TIME(2),
            /**
             * <code>COMPOSITE_MAX_DURATION = 3;</code>
             */
            COMPOSITE_MAX_DURATION(3),
            UNRECOGNIZED(-1),
            ;

            /**
             * <code>CRON = 0;</code>
             */
            public static final int CRON_VALUE = 0;
            /**
             * <code>INTERVAL = 1;</code>
             */
            public static final int INTERVAL_VALUE = 1;
            /**
             * <code>POINT_IN_TIME = 2;</code>
             */
            public static final int POINT_IN_TIME_VALUE = 2;
            /**
             * <code>COMPOSITE_MAX_DURATION = 3;</code>
             */
            public static final int COMPOSITE_MAX_DURATION_VALUE = 3;

            public final int getNumber() {
                if (this == UNRECOGNIZED) {
                    throw new java.lang.IllegalArgumentException(
                            "Can't get the number of an unknown enum value.");
                }
                return value;
            }

            /**
             * @param value The numeric wire value of the corresponding enum entry.
             * @return The enum associated with the given numeric wire value.
             * @deprecated Use {@link #forNumber(int)} instead.
             */
            @java.lang.Deprecated
            public static TriggerType valueOf(int value) {
                return forNumber(value);
            }

            /**
             * @param value The numeric wire value of the corresponding enum entry.
             * @return The enum associated with the given numeric wire value.
             */
            public static TriggerType forNumber(int value) {
                switch (value) {
                    case 0:
                        return CRON;
                    case 1:
                        return INTERVAL;
                    case 2:
                        return POINT_IN_TIME;
                    case 3:
                        return COMPOSITE_MAX_DURATION;
                    default:
                        return null;
                }
            }

            public static com.google.protobuf.Internal.EnumLiteMap<TriggerType>
                    internalGetValueMap() {
                return internalValueMap;
            }

            private static final com.google.protobuf.Internal.EnumLiteMap<TriggerType> internalValueMap =
                    new com.google.protobuf.Internal.EnumLiteMap<TriggerType>() {
                        public TriggerType findValueByNumber(int number) {
                            return TriggerType.forNumber(number);
                        }
                    };

            public final com.google.protobuf.Descriptors.EnumValueDescriptor
                    getValueDescriptor() {
                if (this == UNRECOGNIZED) {
                    throw new java.lang.IllegalStateException(
                            "Can't get the descriptor of an unrecognized enum value.");
                }
                return getDescriptor().getValues().get(ordinal());
            }

            public final com.google.protobuf.Descriptors.EnumDescriptor
                    getDescriptorForType() {
                return getDescriptor();
            }

            public static final com.google.protobuf.Descriptors.EnumDescriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDescriptor().getEnumTypes().get(0);
            }

            private static final TriggerType[] VALUES = values();

            public static TriggerType valueOf(
                    com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
                if (desc.getType() != getDescriptor()) {
                    throw new java.lang.IllegalArgumentException(
                            "EnumValueDescriptor is not for this type.");
                }
                if (desc.getIndex() == -1) {
                    return UNRECOGNIZED;
                }
                return VALUES[desc.getIndex()];
            }

            private final int value;

            private TriggerType(int value) {
                this.value = value;
            }

            // @@protoc_insertion_point(enum_scope:org.kie.kogito.serialization.protobuf.Trigger.TriggerType)
        }

        public static final int TYPE_FIELD_NUMBER = 11;
        private int type_;

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
         * 
         * @return The enum numeric value on the wire for type.
         */
        @java.lang.Override
        public int getTypeValue() {
            return type_;
        }

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
         * 
         * @return The type.
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType getType() {
            @SuppressWarnings("deprecation")
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType result = org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType.valueOf(type_);
            return result == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType.UNRECOGNIZED : result;
        }

        public static final int TRIGGER_FIELD_NUMBER = 12;
        private com.google.protobuf.Any trigger_;

        /**
         * <code>.google.protobuf.Any trigger = 12;</code>
         * 
         * @return Whether the trigger field is set.
         */
        @java.lang.Override
        public boolean hasTrigger() {
            return trigger_ != null;
        }

        /**
         * <code>.google.protobuf.Any trigger = 12;</code>
         * 
         * @return The trigger.
         */
        @java.lang.Override
        public com.google.protobuf.Any getTrigger() {
            return trigger_ == null ? com.google.protobuf.Any.getDefaultInstance() : trigger_;
        }

        /**
         * <code>.google.protobuf.Any trigger = 12;</code>
         */
        @java.lang.Override
        public com.google.protobuf.AnyOrBuilder getTriggerOrBuilder() {
            return getTrigger();
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
            if (type_ != org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType.CRON.getNumber()) {
                output.writeEnum(11, type_);
            }
            if (trigger_ != null) {
                output.writeMessage(12, getTrigger());
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (type_ != org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType.CRON.getNumber()) {
                size += com.google.protobuf.CodedOutputStream
                        .computeEnumSize(11, type_);
            }
            if (trigger_ != null) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(12, getTrigger());
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger) obj;

            if (type_ != other.type_)
                return false;
            if (hasTrigger() != other.hasTrigger())
                return false;
            if (hasTrigger()) {
                if (!getTrigger()
                        .equals(other.getTrigger()))
                    return false;
            }
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
            hash = (37 * hash) + TYPE_FIELD_NUMBER;
            hash = (53 * hash) + type_;
            if (hasTrigger()) {
                hash = (37 * hash) + TRIGGER_FIELD_NUMBER;
                hash = (53 * hash) + getTrigger().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.Trigger}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.Trigger)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Trigger_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Trigger_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.newBuilder()
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
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                type_ = 0;

                if (triggerBuilder_ == null) {
                    trigger_ = null;
                } else {
                    trigger_ = null;
                    triggerBuilder_ = null;
                }
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Trigger_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger(this);
                result.type_ = type_;
                if (triggerBuilder_ == null) {
                    result.trigger_ = trigger_;
                } else {
                    result.trigger_ = triggerBuilder_.build();
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance())
                    return this;
                if (other.type_ != 0) {
                    setTypeValue(other.getTypeValue());
                }
                if (other.hasTrigger()) {
                    mergeTrigger(other.getTrigger());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int type_ = 0;

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
             * 
             * @return The enum numeric value on the wire for type.
             */
            @java.lang.Override
            public int getTypeValue() {
                return type_;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
             * 
             * @param value The enum numeric value on the wire for type to set.
             * @return This builder for chaining.
             */
            public Builder setTypeValue(int value) {

                type_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
             * 
             * @return The type.
             */
            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType getType() {
                @SuppressWarnings("deprecation")
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType result = org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType.valueOf(type_);
                return result == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType.UNRECOGNIZED : result;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
             * 
             * @param value The type to set.
             * @return This builder for chaining.
             */
            public Builder setType(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.TriggerType value) {
                if (value == null) {
                    throw new NullPointerException();
                }

                type_ = value.getNumber();
                onChanged();
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger.TriggerType type = 11;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearType() {

                type_ = 0;
                onChanged();
                return this;
            }

            private com.google.protobuf.Any trigger_;
            private com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder> triggerBuilder_;

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             * 
             * @return Whether the trigger field is set.
             */
            public boolean hasTrigger() {
                return triggerBuilder_ != null || trigger_ != null;
            }

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             * 
             * @return The trigger.
             */
            public com.google.protobuf.Any getTrigger() {
                if (triggerBuilder_ == null) {
                    return trigger_ == null ? com.google.protobuf.Any.getDefaultInstance() : trigger_;
                } else {
                    return triggerBuilder_.getMessage();
                }
            }

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             */
            public Builder setTrigger(com.google.protobuf.Any value) {
                if (triggerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    trigger_ = value;
                    onChanged();
                } else {
                    triggerBuilder_.setMessage(value);
                }

                return this;
            }

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             */
            public Builder setTrigger(
                    com.google.protobuf.Any.Builder builderForValue) {
                if (triggerBuilder_ == null) {
                    trigger_ = builderForValue.build();
                    onChanged();
                } else {
                    triggerBuilder_.setMessage(builderForValue.build());
                }

                return this;
            }

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             */
            public Builder mergeTrigger(com.google.protobuf.Any value) {
                if (triggerBuilder_ == null) {
                    if (trigger_ != null) {
                        trigger_ =
                                com.google.protobuf.Any.newBuilder(trigger_).mergeFrom(value).buildPartial();
                    } else {
                        trigger_ = value;
                    }
                    onChanged();
                } else {
                    triggerBuilder_.mergeFrom(value);
                }

                return this;
            }

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             */
            public Builder clearTrigger() {
                if (triggerBuilder_ == null) {
                    trigger_ = null;
                    onChanged();
                } else {
                    trigger_ = null;
                    triggerBuilder_ = null;
                }

                return this;
            }

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             */
            public com.google.protobuf.Any.Builder getTriggerBuilder() {

                onChanged();
                return getTriggerFieldBuilder().getBuilder();
            }

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             */
            public com.google.protobuf.AnyOrBuilder getTriggerOrBuilder() {
                if (triggerBuilder_ != null) {
                    return triggerBuilder_.getMessageOrBuilder();
                } else {
                    return trigger_ == null ? com.google.protobuf.Any.getDefaultInstance() : trigger_;
                }
            }

            /**
             * <code>.google.protobuf.Any trigger = 12;</code>
             */
            private com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>
                    getTriggerFieldBuilder() {
                if (triggerBuilder_ == null) {
                    triggerBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<com.google.protobuf.Any, com.google.protobuf.Any.Builder, com.google.protobuf.AnyOrBuilder>(
                            getTrigger(),
                            getParentForChildren(),
                            isClean());
                    trigger_ = null;
                }
                return triggerBuilder_;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.Trigger)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.Trigger)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<Trigger> PARSER = new com.google.protobuf.AbstractParser<Trigger>() {
            @java.lang.Override
            public Trigger parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new Trigger(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<Trigger> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<Trigger> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface CronTriggerOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.CronTrigger)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>int64 start_time = 1;</code>
         * 
         * @return Whether the startTime field is set.
         */
        boolean hasStartTime();

        /**
         * <code>int64 start_time = 1;</code>
         * 
         * @return The startTime.
         */
        long getStartTime();

        /**
         * <code>int64 end_time = 2;</code>
         * 
         * @return Whether the endTime field is set.
         */
        boolean hasEndTime();

        /**
         * <code>int64 end_time = 2;</code>
         * 
         * @return The endTime.
         */
        long getEndTime();

        /**
         * <code>int32 repeat_limit = 3;</code>
         * 
         * @return Whether the repeatLimit field is set.
         */
        boolean hasRepeatLimit();

        /**
         * <code>int32 repeat_limit = 3;</code>
         * 
         * @return The repeatLimit.
         */
        int getRepeatLimit();

        /**
         * <code>int32 repeat_count = 4;</code>
         * 
         * @return Whether the repeatCount field is set.
         */
        boolean hasRepeatCount();

        /**
         * <code>int32 repeat_count = 4;</code>
         * 
         * @return The repeatCount.
         */
        int getRepeatCount();

        /**
         * <code>string cron_expression = 5;</code>
         * 
         * @return Whether the cronExpression field is set.
         */
        boolean hasCronExpression();

        /**
         * <code>string cron_expression = 5;</code>
         * 
         * @return The cronExpression.
         */
        java.lang.String getCronExpression();

        /**
         * <code>string cron_expression = 5;</code>
         * 
         * @return The bytes for cronExpression.
         */
        com.google.protobuf.ByteString
                getCronExpressionBytes();

        /**
         * <code>int64 next_fire_time = 6;</code>
         * 
         * @return Whether the nextFireTime field is set.
         */
        boolean hasNextFireTime();

        /**
         * <code>int64 next_fire_time = 6;</code>
         * 
         * @return The nextFireTime.
         */
        long getNextFireTime();

        /**
         * <code>repeated string calendar_name = 7;</code>
         * 
         * @return A list containing the calendarName.
         */
        java.util.List<java.lang.String>
                getCalendarNameList();

        /**
         * <code>repeated string calendar_name = 7;</code>
         * 
         * @return The count of calendarName.
         */
        int getCalendarNameCount();

        /**
         * <code>repeated string calendar_name = 7;</code>
         * 
         * @param index The index of the element to return.
         * @return The calendarName at the given index.
         */
        java.lang.String getCalendarName(int index);

        /**
         * <code>repeated string calendar_name = 7;</code>
         * 
         * @param index The index of the value to return.
         * @return The bytes of the calendarName at the given index.
         */
        com.google.protobuf.ByteString
                getCalendarNameBytes(int index);
    }

    /**
     * <pre>
     * trigger types types
     * </pre>
     *
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.CronTrigger}
     */
    public static final class CronTrigger extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.CronTrigger)
            CronTriggerOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use CronTrigger.newBuilder() to construct.
        private CronTrigger(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private CronTrigger() {
            cronExpression_ = "";
            calendarName_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new CronTrigger();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private CronTrigger(
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
                        case 8: {
                            bitField0_ |= 0x00000001;
                            startTime_ = input.readInt64();
                            break;
                        }
                        case 16: {
                            bitField0_ |= 0x00000002;
                            endTime_ = input.readInt64();
                            break;
                        }
                        case 24: {
                            bitField0_ |= 0x00000004;
                            repeatLimit_ = input.readInt32();
                            break;
                        }
                        case 32: {
                            bitField0_ |= 0x00000008;
                            repeatCount_ = input.readInt32();
                            break;
                        }
                        case 42: {
                            java.lang.String s = input.readStringRequireUtf8();
                            bitField0_ |= 0x00000010;
                            cronExpression_ = s;
                            break;
                        }
                        case 48: {
                            bitField0_ |= 0x00000020;
                            nextFireTime_ = input.readInt64();
                            break;
                        }
                        case 58: {
                            java.lang.String s = input.readStringRequireUtf8();
                            if (!((mutable_bitField0_ & 0x00000040) != 0)) {
                                calendarName_ = new com.google.protobuf.LazyStringArrayList();
                                mutable_bitField0_ |= 0x00000040;
                            }
                            calendarName_.add(s);
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
                if (((mutable_bitField0_ & 0x00000040) != 0)) {
                    calendarName_ = calendarName_.getUnmodifiableView();
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger.Builder.class);
        }

        private int bitField0_;
        public static final int START_TIME_FIELD_NUMBER = 1;
        private long startTime_;

        /**
         * <code>int64 start_time = 1;</code>
         * 
         * @return Whether the startTime field is set.
         */
        @java.lang.Override
        public boolean hasStartTime() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <code>int64 start_time = 1;</code>
         * 
         * @return The startTime.
         */
        @java.lang.Override
        public long getStartTime() {
            return startTime_;
        }

        public static final int END_TIME_FIELD_NUMBER = 2;
        private long endTime_;

        /**
         * <code>int64 end_time = 2;</code>
         * 
         * @return Whether the endTime field is set.
         */
        @java.lang.Override
        public boolean hasEndTime() {
            return ((bitField0_ & 0x00000002) != 0);
        }

        /**
         * <code>int64 end_time = 2;</code>
         * 
         * @return The endTime.
         */
        @java.lang.Override
        public long getEndTime() {
            return endTime_;
        }

        public static final int REPEAT_LIMIT_FIELD_NUMBER = 3;
        private int repeatLimit_;

        /**
         * <code>int32 repeat_limit = 3;</code>
         * 
         * @return Whether the repeatLimit field is set.
         */
        @java.lang.Override
        public boolean hasRepeatLimit() {
            return ((bitField0_ & 0x00000004) != 0);
        }

        /**
         * <code>int32 repeat_limit = 3;</code>
         * 
         * @return The repeatLimit.
         */
        @java.lang.Override
        public int getRepeatLimit() {
            return repeatLimit_;
        }

        public static final int REPEAT_COUNT_FIELD_NUMBER = 4;
        private int repeatCount_;

        /**
         * <code>int32 repeat_count = 4;</code>
         * 
         * @return Whether the repeatCount field is set.
         */
        @java.lang.Override
        public boolean hasRepeatCount() {
            return ((bitField0_ & 0x00000008) != 0);
        }

        /**
         * <code>int32 repeat_count = 4;</code>
         * 
         * @return The repeatCount.
         */
        @java.lang.Override
        public int getRepeatCount() {
            return repeatCount_;
        }

        public static final int CRON_EXPRESSION_FIELD_NUMBER = 5;
        private volatile java.lang.Object cronExpression_;

        /**
         * <code>string cron_expression = 5;</code>
         * 
         * @return Whether the cronExpression field is set.
         */
        @java.lang.Override
        public boolean hasCronExpression() {
            return ((bitField0_ & 0x00000010) != 0);
        }

        /**
         * <code>string cron_expression = 5;</code>
         * 
         * @return The cronExpression.
         */
        @java.lang.Override
        public java.lang.String getCronExpression() {
            java.lang.Object ref = cronExpression_;
            if (ref instanceof java.lang.String) {
                return (java.lang.String) ref;
            } else {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                cronExpression_ = s;
                return s;
            }
        }

        /**
         * <code>string cron_expression = 5;</code>
         * 
         * @return The bytes for cronExpression.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString
                getCronExpressionBytes() {
            java.lang.Object ref = cronExpression_;
            if (ref instanceof java.lang.String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                cronExpression_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        public static final int NEXT_FIRE_TIME_FIELD_NUMBER = 6;
        private long nextFireTime_;

        /**
         * <code>int64 next_fire_time = 6;</code>
         * 
         * @return Whether the nextFireTime field is set.
         */
        @java.lang.Override
        public boolean hasNextFireTime() {
            return ((bitField0_ & 0x00000020) != 0);
        }

        /**
         * <code>int64 next_fire_time = 6;</code>
         * 
         * @return The nextFireTime.
         */
        @java.lang.Override
        public long getNextFireTime() {
            return nextFireTime_;
        }

        public static final int CALENDAR_NAME_FIELD_NUMBER = 7;
        private com.google.protobuf.LazyStringList calendarName_;

        /**
         * <code>repeated string calendar_name = 7;</code>
         * 
         * @return A list containing the calendarName.
         */
        public com.google.protobuf.ProtocolStringList
                getCalendarNameList() {
            return calendarName_;
        }

        /**
         * <code>repeated string calendar_name = 7;</code>
         * 
         * @return The count of calendarName.
         */
        public int getCalendarNameCount() {
            return calendarName_.size();
        }

        /**
         * <code>repeated string calendar_name = 7;</code>
         * 
         * @param index The index of the element to return.
         * @return The calendarName at the given index.
         */
        public java.lang.String getCalendarName(int index) {
            return calendarName_.get(index);
        }

        /**
         * <code>repeated string calendar_name = 7;</code>
         * 
         * @param index The index of the value to return.
         * @return The bytes of the calendarName at the given index.
         */
        public com.google.protobuf.ByteString
                getCalendarNameBytes(int index) {
            return calendarName_.getByteString(index);
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
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeInt64(1, startTime_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                output.writeInt64(2, endTime_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                output.writeInt32(3, repeatLimit_);
            }
            if (((bitField0_ & 0x00000008) != 0)) {
                output.writeInt32(4, repeatCount_);
            }
            if (((bitField0_ & 0x00000010) != 0)) {
                com.google.protobuf.GeneratedMessageV3.writeString(output, 5, cronExpression_);
            }
            if (((bitField0_ & 0x00000020) != 0)) {
                output.writeInt64(6, nextFireTime_);
            }
            for (int i = 0; i < calendarName_.size(); i++) {
                com.google.protobuf.GeneratedMessageV3.writeString(output, 7, calendarName_.getRaw(i));
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(1, startTime_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(2, endTime_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(3, repeatLimit_);
            }
            if (((bitField0_ & 0x00000008) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(4, repeatCount_);
            }
            if (((bitField0_ & 0x00000010) != 0)) {
                size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, cronExpression_);
            }
            if (((bitField0_ & 0x00000020) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(6, nextFireTime_);
            }
            {
                int dataSize = 0;
                for (int i = 0; i < calendarName_.size(); i++) {
                    dataSize += computeStringSizeNoTag(calendarName_.getRaw(i));
                }
                size += dataSize;
                size += 1 * getCalendarNameList().size();
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger) obj;

            if (hasStartTime() != other.hasStartTime())
                return false;
            if (hasStartTime()) {
                if (getStartTime() != other.getStartTime())
                    return false;
            }
            if (hasEndTime() != other.hasEndTime())
                return false;
            if (hasEndTime()) {
                if (getEndTime() != other.getEndTime())
                    return false;
            }
            if (hasRepeatLimit() != other.hasRepeatLimit())
                return false;
            if (hasRepeatLimit()) {
                if (getRepeatLimit() != other.getRepeatLimit())
                    return false;
            }
            if (hasRepeatCount() != other.hasRepeatCount())
                return false;
            if (hasRepeatCount()) {
                if (getRepeatCount() != other.getRepeatCount())
                    return false;
            }
            if (hasCronExpression() != other.hasCronExpression())
                return false;
            if (hasCronExpression()) {
                if (!getCronExpression()
                        .equals(other.getCronExpression()))
                    return false;
            }
            if (hasNextFireTime() != other.hasNextFireTime())
                return false;
            if (hasNextFireTime()) {
                if (getNextFireTime() != other.getNextFireTime())
                    return false;
            }
            if (!getCalendarNameList()
                    .equals(other.getCalendarNameList()))
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
            if (hasStartTime()) {
                hash = (37 * hash) + START_TIME_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getStartTime());
            }
            if (hasEndTime()) {
                hash = (37 * hash) + END_TIME_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getEndTime());
            }
            if (hasRepeatLimit()) {
                hash = (37 * hash) + REPEAT_LIMIT_FIELD_NUMBER;
                hash = (53 * hash) + getRepeatLimit();
            }
            if (hasRepeatCount()) {
                hash = (37 * hash) + REPEAT_COUNT_FIELD_NUMBER;
                hash = (53 * hash) + getRepeatCount();
            }
            if (hasCronExpression()) {
                hash = (37 * hash) + CRON_EXPRESSION_FIELD_NUMBER;
                hash = (53 * hash) + getCronExpression().hashCode();
            }
            if (hasNextFireTime()) {
                hash = (37 * hash) + NEXT_FIRE_TIME_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getNextFireTime());
            }
            if (getCalendarNameCount() > 0) {
                hash = (37 * hash) + CALENDAR_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getCalendarNameList().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger prototype) {
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
         * <pre>
         * trigger types types
         * </pre>
         *
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.CronTrigger}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.CronTrigger)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTriggerOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger.newBuilder()
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
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                startTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                endTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000002);
                repeatLimit_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                repeatCount_ = 0;
                bitField0_ = (bitField0_ & ~0x00000008);
                cronExpression_ = "";
                bitField0_ = (bitField0_ & ~0x00000010);
                nextFireTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000020);
                calendarName_ = com.google.protobuf.LazyStringArrayList.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000040);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) != 0)) {
                    result.startTime_ = startTime_;
                    to_bitField0_ |= 0x00000001;
                }
                if (((from_bitField0_ & 0x00000002) != 0)) {
                    result.endTime_ = endTime_;
                    to_bitField0_ |= 0x00000002;
                }
                if (((from_bitField0_ & 0x00000004) != 0)) {
                    result.repeatLimit_ = repeatLimit_;
                    to_bitField0_ |= 0x00000004;
                }
                if (((from_bitField0_ & 0x00000008) != 0)) {
                    result.repeatCount_ = repeatCount_;
                    to_bitField0_ |= 0x00000008;
                }
                if (((from_bitField0_ & 0x00000010) != 0)) {
                    to_bitField0_ |= 0x00000010;
                }
                result.cronExpression_ = cronExpression_;
                if (((from_bitField0_ & 0x00000020) != 0)) {
                    result.nextFireTime_ = nextFireTime_;
                    to_bitField0_ |= 0x00000020;
                }
                if (((bitField0_ & 0x00000040) != 0)) {
                    calendarName_ = calendarName_.getUnmodifiableView();
                    bitField0_ = (bitField0_ & ~0x00000040);
                }
                result.calendarName_ = calendarName_;
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger.getDefaultInstance())
                    return this;
                if (other.hasStartTime()) {
                    setStartTime(other.getStartTime());
                }
                if (other.hasEndTime()) {
                    setEndTime(other.getEndTime());
                }
                if (other.hasRepeatLimit()) {
                    setRepeatLimit(other.getRepeatLimit());
                }
                if (other.hasRepeatCount()) {
                    setRepeatCount(other.getRepeatCount());
                }
                if (other.hasCronExpression()) {
                    bitField0_ |= 0x00000010;
                    cronExpression_ = other.cronExpression_;
                    onChanged();
                }
                if (other.hasNextFireTime()) {
                    setNextFireTime(other.getNextFireTime());
                }
                if (!other.calendarName_.isEmpty()) {
                    if (calendarName_.isEmpty()) {
                        calendarName_ = other.calendarName_;
                        bitField0_ = (bitField0_ & ~0x00000040);
                    } else {
                        ensureCalendarNameIsMutable();
                        calendarName_.addAll(other.calendarName_);
                    }
                    onChanged();
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private long startTime_;

            /**
             * <code>int64 start_time = 1;</code>
             * 
             * @return Whether the startTime field is set.
             */
            @java.lang.Override
            public boolean hasStartTime() {
                return ((bitField0_ & 0x00000001) != 0);
            }

            /**
             * <code>int64 start_time = 1;</code>
             * 
             * @return The startTime.
             */
            @java.lang.Override
            public long getStartTime() {
                return startTime_;
            }

            /**
             * <code>int64 start_time = 1;</code>
             * 
             * @param value The startTime to set.
             * @return This builder for chaining.
             */
            public Builder setStartTime(long value) {
                bitField0_ |= 0x00000001;
                startTime_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 start_time = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearStartTime() {
                bitField0_ = (bitField0_ & ~0x00000001);
                startTime_ = 0L;
                onChanged();
                return this;
            }

            private long endTime_;

            /**
             * <code>int64 end_time = 2;</code>
             * 
             * @return Whether the endTime field is set.
             */
            @java.lang.Override
            public boolean hasEndTime() {
                return ((bitField0_ & 0x00000002) != 0);
            }

            /**
             * <code>int64 end_time = 2;</code>
             * 
             * @return The endTime.
             */
            @java.lang.Override
            public long getEndTime() {
                return endTime_;
            }

            /**
             * <code>int64 end_time = 2;</code>
             * 
             * @param value The endTime to set.
             * @return This builder for chaining.
             */
            public Builder setEndTime(long value) {
                bitField0_ |= 0x00000002;
                endTime_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 end_time = 2;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearEndTime() {
                bitField0_ = (bitField0_ & ~0x00000002);
                endTime_ = 0L;
                onChanged();
                return this;
            }

            private int repeatLimit_;

            /**
             * <code>int32 repeat_limit = 3;</code>
             * 
             * @return Whether the repeatLimit field is set.
             */
            @java.lang.Override
            public boolean hasRepeatLimit() {
                return ((bitField0_ & 0x00000004) != 0);
            }

            /**
             * <code>int32 repeat_limit = 3;</code>
             * 
             * @return The repeatLimit.
             */
            @java.lang.Override
            public int getRepeatLimit() {
                return repeatLimit_;
            }

            /**
             * <code>int32 repeat_limit = 3;</code>
             * 
             * @param value The repeatLimit to set.
             * @return This builder for chaining.
             */
            public Builder setRepeatLimit(int value) {
                bitField0_ |= 0x00000004;
                repeatLimit_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int32 repeat_limit = 3;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearRepeatLimit() {
                bitField0_ = (bitField0_ & ~0x00000004);
                repeatLimit_ = 0;
                onChanged();
                return this;
            }

            private int repeatCount_;

            /**
             * <code>int32 repeat_count = 4;</code>
             * 
             * @return Whether the repeatCount field is set.
             */
            @java.lang.Override
            public boolean hasRepeatCount() {
                return ((bitField0_ & 0x00000008) != 0);
            }

            /**
             * <code>int32 repeat_count = 4;</code>
             * 
             * @return The repeatCount.
             */
            @java.lang.Override
            public int getRepeatCount() {
                return repeatCount_;
            }

            /**
             * <code>int32 repeat_count = 4;</code>
             * 
             * @param value The repeatCount to set.
             * @return This builder for chaining.
             */
            public Builder setRepeatCount(int value) {
                bitField0_ |= 0x00000008;
                repeatCount_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int32 repeat_count = 4;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearRepeatCount() {
                bitField0_ = (bitField0_ & ~0x00000008);
                repeatCount_ = 0;
                onChanged();
                return this;
            }

            private java.lang.Object cronExpression_ = "";

            /**
             * <code>string cron_expression = 5;</code>
             * 
             * @return Whether the cronExpression field is set.
             */
            public boolean hasCronExpression() {
                return ((bitField0_ & 0x00000010) != 0);
            }

            /**
             * <code>string cron_expression = 5;</code>
             * 
             * @return The cronExpression.
             */
            public java.lang.String getCronExpression() {
                java.lang.Object ref = cronExpression_;
                if (!(ref instanceof java.lang.String)) {
                    com.google.protobuf.ByteString bs =
                            (com.google.protobuf.ByteString) ref;
                    java.lang.String s = bs.toStringUtf8();
                    cronExpression_ = s;
                    return s;
                } else {
                    return (java.lang.String) ref;
                }
            }

            /**
             * <code>string cron_expression = 5;</code>
             * 
             * @return The bytes for cronExpression.
             */
            public com.google.protobuf.ByteString
                    getCronExpressionBytes() {
                java.lang.Object ref = cronExpression_;
                if (ref instanceof String) {
                    com.google.protobuf.ByteString b =
                            com.google.protobuf.ByteString.copyFromUtf8(
                                    (java.lang.String) ref);
                    cronExpression_ = b;
                    return b;
                } else {
                    return (com.google.protobuf.ByteString) ref;
                }
            }

            /**
             * <code>string cron_expression = 5;</code>
             * 
             * @param value The cronExpression to set.
             * @return This builder for chaining.
             */
            public Builder setCronExpression(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000010;
                cronExpression_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>string cron_expression = 5;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearCronExpression() {
                bitField0_ = (bitField0_ & ~0x00000010);
                cronExpression_ = getDefaultInstance().getCronExpression();
                onChanged();
                return this;
            }

            /**
             * <code>string cron_expression = 5;</code>
             * 
             * @param value The bytes for cronExpression to set.
             * @return This builder for chaining.
             */
            public Builder setCronExpressionBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                checkByteStringIsUtf8(value);
                bitField0_ |= 0x00000010;
                cronExpression_ = value;
                onChanged();
                return this;
            }

            private long nextFireTime_;

            /**
             * <code>int64 next_fire_time = 6;</code>
             * 
             * @return Whether the nextFireTime field is set.
             */
            @java.lang.Override
            public boolean hasNextFireTime() {
                return ((bitField0_ & 0x00000020) != 0);
            }

            /**
             * <code>int64 next_fire_time = 6;</code>
             * 
             * @return The nextFireTime.
             */
            @java.lang.Override
            public long getNextFireTime() {
                return nextFireTime_;
            }

            /**
             * <code>int64 next_fire_time = 6;</code>
             * 
             * @param value The nextFireTime to set.
             * @return This builder for chaining.
             */
            public Builder setNextFireTime(long value) {
                bitField0_ |= 0x00000020;
                nextFireTime_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 next_fire_time = 6;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearNextFireTime() {
                bitField0_ = (bitField0_ & ~0x00000020);
                nextFireTime_ = 0L;
                onChanged();
                return this;
            }

            private com.google.protobuf.LazyStringList calendarName_ = com.google.protobuf.LazyStringArrayList.EMPTY;

            private void ensureCalendarNameIsMutable() {
                if (!((bitField0_ & 0x00000040) != 0)) {
                    calendarName_ = new com.google.protobuf.LazyStringArrayList(calendarName_);
                    bitField0_ |= 0x00000040;
                }
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @return A list containing the calendarName.
             */
            public com.google.protobuf.ProtocolStringList
                    getCalendarNameList() {
                return calendarName_.getUnmodifiableView();
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @return The count of calendarName.
             */
            public int getCalendarNameCount() {
                return calendarName_.size();
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @param index The index of the element to return.
             * @return The calendarName at the given index.
             */
            public java.lang.String getCalendarName(int index) {
                return calendarName_.get(index);
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @param index The index of the value to return.
             * @return The bytes of the calendarName at the given index.
             */
            public com.google.protobuf.ByteString
                    getCalendarNameBytes(int index) {
                return calendarName_.getByteString(index);
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @param index The index to set the value at.
             * @param value The calendarName to set.
             * @return This builder for chaining.
             */
            public Builder setCalendarName(
                    int index, java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureCalendarNameIsMutable();
                calendarName_.set(index, value);
                onChanged();
                return this;
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @param value The calendarName to add.
             * @return This builder for chaining.
             */
            public Builder addCalendarName(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureCalendarNameIsMutable();
                calendarName_.add(value);
                onChanged();
                return this;
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @param values The calendarName to add.
             * @return This builder for chaining.
             */
            public Builder addAllCalendarName(
                    java.lang.Iterable<java.lang.String> values) {
                ensureCalendarNameIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                        values, calendarName_);
                onChanged();
                return this;
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearCalendarName() {
                calendarName_ = com.google.protobuf.LazyStringArrayList.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000040);
                onChanged();
                return this;
            }

            /**
             * <code>repeated string calendar_name = 7;</code>
             * 
             * @param value The bytes of the calendarName to add.
             * @return This builder for chaining.
             */
            public Builder addCalendarNameBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                checkByteStringIsUtf8(value);
                ensureCalendarNameIsMutable();
                calendarName_.add(value);
                onChanged();
                return this;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.CronTrigger)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.CronTrigger)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<CronTrigger> PARSER = new com.google.protobuf.AbstractParser<CronTrigger>() {
            @java.lang.Override
            public CronTrigger parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new CronTrigger(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<CronTrigger> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<CronTrigger> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CronTrigger getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface IntervalTriggerOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.IntervalTrigger)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>int64 start_time = 1;</code>
         * 
         * @return Whether the startTime field is set.
         */
        boolean hasStartTime();

        /**
         * <code>int64 start_time = 1;</code>
         * 
         * @return The startTime.
         */
        long getStartTime();

        /**
         * <code>int64 end_time = 2;</code>
         * 
         * @return Whether the endTime field is set.
         */
        boolean hasEndTime();

        /**
         * <code>int64 end_time = 2;</code>
         * 
         * @return The endTime.
         */
        long getEndTime();

        /**
         * <code>int32 repeat_limit = 3;</code>
         * 
         * @return Whether the repeatLimit field is set.
         */
        boolean hasRepeatLimit();

        /**
         * <code>int32 repeat_limit = 3;</code>
         * 
         * @return The repeatLimit.
         */
        int getRepeatLimit();

        /**
         * <code>int32 repeat_count = 4;</code>
         * 
         * @return Whether the repeatCount field is set.
         */
        boolean hasRepeatCount();

        /**
         * <code>int32 repeat_count = 4;</code>
         * 
         * @return The repeatCount.
         */
        int getRepeatCount();

        /**
         * <code>int64 next_fire_time = 6;</code>
         * 
         * @return Whether the nextFireTime field is set.
         */
        boolean hasNextFireTime();

        /**
         * <code>int64 next_fire_time = 6;</code>
         * 
         * @return The nextFireTime.
         */
        long getNextFireTime();

        /**
         * <code>int64 period = 7;</code>
         * 
         * @return Whether the period field is set.
         */
        boolean hasPeriod();

        /**
         * <code>int64 period = 7;</code>
         * 
         * @return The period.
         */
        long getPeriod();

        /**
         * <code>repeated string calendar_name = 8;</code>
         * 
         * @return A list containing the calendarName.
         */
        java.util.List<java.lang.String>
                getCalendarNameList();

        /**
         * <code>repeated string calendar_name = 8;</code>
         * 
         * @return The count of calendarName.
         */
        int getCalendarNameCount();

        /**
         * <code>repeated string calendar_name = 8;</code>
         * 
         * @param index The index of the element to return.
         * @return The calendarName at the given index.
         */
        java.lang.String getCalendarName(int index);

        /**
         * <code>repeated string calendar_name = 8;</code>
         * 
         * @param index The index of the value to return.
         * @return The bytes of the calendarName at the given index.
         */
        com.google.protobuf.ByteString
                getCalendarNameBytes(int index);
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.IntervalTrigger}
     */
    public static final class IntervalTrigger extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.IntervalTrigger)
            IntervalTriggerOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use IntervalTrigger.newBuilder() to construct.
        private IntervalTrigger(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private IntervalTrigger() {
            calendarName_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new IntervalTrigger();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private IntervalTrigger(
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
                        case 8: {
                            bitField0_ |= 0x00000001;
                            startTime_ = input.readInt64();
                            break;
                        }
                        case 16: {
                            bitField0_ |= 0x00000002;
                            endTime_ = input.readInt64();
                            break;
                        }
                        case 24: {
                            bitField0_ |= 0x00000004;
                            repeatLimit_ = input.readInt32();
                            break;
                        }
                        case 32: {
                            bitField0_ |= 0x00000008;
                            repeatCount_ = input.readInt32();
                            break;
                        }
                        case 48: {
                            bitField0_ |= 0x00000010;
                            nextFireTime_ = input.readInt64();
                            break;
                        }
                        case 56: {
                            bitField0_ |= 0x00000020;
                            period_ = input.readInt64();
                            break;
                        }
                        case 66: {
                            java.lang.String s = input.readStringRequireUtf8();
                            if (!((mutable_bitField0_ & 0x00000040) != 0)) {
                                calendarName_ = new com.google.protobuf.LazyStringArrayList();
                                mutable_bitField0_ |= 0x00000040;
                            }
                            calendarName_.add(s);
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
                if (((mutable_bitField0_ & 0x00000040) != 0)) {
                    calendarName_ = calendarName_.getUnmodifiableView();
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger.Builder.class);
        }

        private int bitField0_;
        public static final int START_TIME_FIELD_NUMBER = 1;
        private long startTime_;

        /**
         * <code>int64 start_time = 1;</code>
         * 
         * @return Whether the startTime field is set.
         */
        @java.lang.Override
        public boolean hasStartTime() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <code>int64 start_time = 1;</code>
         * 
         * @return The startTime.
         */
        @java.lang.Override
        public long getStartTime() {
            return startTime_;
        }

        public static final int END_TIME_FIELD_NUMBER = 2;
        private long endTime_;

        /**
         * <code>int64 end_time = 2;</code>
         * 
         * @return Whether the endTime field is set.
         */
        @java.lang.Override
        public boolean hasEndTime() {
            return ((bitField0_ & 0x00000002) != 0);
        }

        /**
         * <code>int64 end_time = 2;</code>
         * 
         * @return The endTime.
         */
        @java.lang.Override
        public long getEndTime() {
            return endTime_;
        }

        public static final int REPEAT_LIMIT_FIELD_NUMBER = 3;
        private int repeatLimit_;

        /**
         * <code>int32 repeat_limit = 3;</code>
         * 
         * @return Whether the repeatLimit field is set.
         */
        @java.lang.Override
        public boolean hasRepeatLimit() {
            return ((bitField0_ & 0x00000004) != 0);
        }

        /**
         * <code>int32 repeat_limit = 3;</code>
         * 
         * @return The repeatLimit.
         */
        @java.lang.Override
        public int getRepeatLimit() {
            return repeatLimit_;
        }

        public static final int REPEAT_COUNT_FIELD_NUMBER = 4;
        private int repeatCount_;

        /**
         * <code>int32 repeat_count = 4;</code>
         * 
         * @return Whether the repeatCount field is set.
         */
        @java.lang.Override
        public boolean hasRepeatCount() {
            return ((bitField0_ & 0x00000008) != 0);
        }

        /**
         * <code>int32 repeat_count = 4;</code>
         * 
         * @return The repeatCount.
         */
        @java.lang.Override
        public int getRepeatCount() {
            return repeatCount_;
        }

        public static final int NEXT_FIRE_TIME_FIELD_NUMBER = 6;
        private long nextFireTime_;

        /**
         * <code>int64 next_fire_time = 6;</code>
         * 
         * @return Whether the nextFireTime field is set.
         */
        @java.lang.Override
        public boolean hasNextFireTime() {
            return ((bitField0_ & 0x00000010) != 0);
        }

        /**
         * <code>int64 next_fire_time = 6;</code>
         * 
         * @return The nextFireTime.
         */
        @java.lang.Override
        public long getNextFireTime() {
            return nextFireTime_;
        }

        public static final int PERIOD_FIELD_NUMBER = 7;
        private long period_;

        /**
         * <code>int64 period = 7;</code>
         * 
         * @return Whether the period field is set.
         */
        @java.lang.Override
        public boolean hasPeriod() {
            return ((bitField0_ & 0x00000020) != 0);
        }

        /**
         * <code>int64 period = 7;</code>
         * 
         * @return The period.
         */
        @java.lang.Override
        public long getPeriod() {
            return period_;
        }

        public static final int CALENDAR_NAME_FIELD_NUMBER = 8;
        private com.google.protobuf.LazyStringList calendarName_;

        /**
         * <code>repeated string calendar_name = 8;</code>
         * 
         * @return A list containing the calendarName.
         */
        public com.google.protobuf.ProtocolStringList
                getCalendarNameList() {
            return calendarName_;
        }

        /**
         * <code>repeated string calendar_name = 8;</code>
         * 
         * @return The count of calendarName.
         */
        public int getCalendarNameCount() {
            return calendarName_.size();
        }

        /**
         * <code>repeated string calendar_name = 8;</code>
         * 
         * @param index The index of the element to return.
         * @return The calendarName at the given index.
         */
        public java.lang.String getCalendarName(int index) {
            return calendarName_.get(index);
        }

        /**
         * <code>repeated string calendar_name = 8;</code>
         * 
         * @param index The index of the value to return.
         * @return The bytes of the calendarName at the given index.
         */
        public com.google.protobuf.ByteString
                getCalendarNameBytes(int index) {
            return calendarName_.getByteString(index);
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
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeInt64(1, startTime_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                output.writeInt64(2, endTime_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                output.writeInt32(3, repeatLimit_);
            }
            if (((bitField0_ & 0x00000008) != 0)) {
                output.writeInt32(4, repeatCount_);
            }
            if (((bitField0_ & 0x00000010) != 0)) {
                output.writeInt64(6, nextFireTime_);
            }
            if (((bitField0_ & 0x00000020) != 0)) {
                output.writeInt64(7, period_);
            }
            for (int i = 0; i < calendarName_.size(); i++) {
                com.google.protobuf.GeneratedMessageV3.writeString(output, 8, calendarName_.getRaw(i));
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(1, startTime_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(2, endTime_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(3, repeatLimit_);
            }
            if (((bitField0_ & 0x00000008) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(4, repeatCount_);
            }
            if (((bitField0_ & 0x00000010) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(6, nextFireTime_);
            }
            if (((bitField0_ & 0x00000020) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(7, period_);
            }
            {
                int dataSize = 0;
                for (int i = 0; i < calendarName_.size(); i++) {
                    dataSize += computeStringSizeNoTag(calendarName_.getRaw(i));
                }
                size += dataSize;
                size += 1 * getCalendarNameList().size();
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger) obj;

            if (hasStartTime() != other.hasStartTime())
                return false;
            if (hasStartTime()) {
                if (getStartTime() != other.getStartTime())
                    return false;
            }
            if (hasEndTime() != other.hasEndTime())
                return false;
            if (hasEndTime()) {
                if (getEndTime() != other.getEndTime())
                    return false;
            }
            if (hasRepeatLimit() != other.hasRepeatLimit())
                return false;
            if (hasRepeatLimit()) {
                if (getRepeatLimit() != other.getRepeatLimit())
                    return false;
            }
            if (hasRepeatCount() != other.hasRepeatCount())
                return false;
            if (hasRepeatCount()) {
                if (getRepeatCount() != other.getRepeatCount())
                    return false;
            }
            if (hasNextFireTime() != other.hasNextFireTime())
                return false;
            if (hasNextFireTime()) {
                if (getNextFireTime() != other.getNextFireTime())
                    return false;
            }
            if (hasPeriod() != other.hasPeriod())
                return false;
            if (hasPeriod()) {
                if (getPeriod() != other.getPeriod())
                    return false;
            }
            if (!getCalendarNameList()
                    .equals(other.getCalendarNameList()))
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
            if (hasStartTime()) {
                hash = (37 * hash) + START_TIME_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getStartTime());
            }
            if (hasEndTime()) {
                hash = (37 * hash) + END_TIME_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getEndTime());
            }
            if (hasRepeatLimit()) {
                hash = (37 * hash) + REPEAT_LIMIT_FIELD_NUMBER;
                hash = (53 * hash) + getRepeatLimit();
            }
            if (hasRepeatCount()) {
                hash = (37 * hash) + REPEAT_COUNT_FIELD_NUMBER;
                hash = (53 * hash) + getRepeatCount();
            }
            if (hasNextFireTime()) {
                hash = (37 * hash) + NEXT_FIRE_TIME_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getNextFireTime());
            }
            if (hasPeriod()) {
                hash = (37 * hash) + PERIOD_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getPeriod());
            }
            if (getCalendarNameCount() > 0) {
                hash = (37 * hash) + CALENDAR_NAME_FIELD_NUMBER;
                hash = (53 * hash) + getCalendarNameList().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.IntervalTrigger}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.IntervalTrigger)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTriggerOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger.class,
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger.newBuilder()
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
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                startTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                endTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000002);
                repeatLimit_ = 0;
                bitField0_ = (bitField0_ & ~0x00000004);
                repeatCount_ = 0;
                bitField0_ = (bitField0_ & ~0x00000008);
                nextFireTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000010);
                period_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000020);
                calendarName_ = com.google.protobuf.LazyStringArrayList.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000040);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) != 0)) {
                    result.startTime_ = startTime_;
                    to_bitField0_ |= 0x00000001;
                }
                if (((from_bitField0_ & 0x00000002) != 0)) {
                    result.endTime_ = endTime_;
                    to_bitField0_ |= 0x00000002;
                }
                if (((from_bitField0_ & 0x00000004) != 0)) {
                    result.repeatLimit_ = repeatLimit_;
                    to_bitField0_ |= 0x00000004;
                }
                if (((from_bitField0_ & 0x00000008) != 0)) {
                    result.repeatCount_ = repeatCount_;
                    to_bitField0_ |= 0x00000008;
                }
                if (((from_bitField0_ & 0x00000010) != 0)) {
                    result.nextFireTime_ = nextFireTime_;
                    to_bitField0_ |= 0x00000010;
                }
                if (((from_bitField0_ & 0x00000020) != 0)) {
                    result.period_ = period_;
                    to_bitField0_ |= 0x00000020;
                }
                if (((bitField0_ & 0x00000040) != 0)) {
                    calendarName_ = calendarName_.getUnmodifiableView();
                    bitField0_ = (bitField0_ & ~0x00000040);
                }
                result.calendarName_ = calendarName_;
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger.getDefaultInstance())
                    return this;
                if (other.hasStartTime()) {
                    setStartTime(other.getStartTime());
                }
                if (other.hasEndTime()) {
                    setEndTime(other.getEndTime());
                }
                if (other.hasRepeatLimit()) {
                    setRepeatLimit(other.getRepeatLimit());
                }
                if (other.hasRepeatCount()) {
                    setRepeatCount(other.getRepeatCount());
                }
                if (other.hasNextFireTime()) {
                    setNextFireTime(other.getNextFireTime());
                }
                if (other.hasPeriod()) {
                    setPeriod(other.getPeriod());
                }
                if (!other.calendarName_.isEmpty()) {
                    if (calendarName_.isEmpty()) {
                        calendarName_ = other.calendarName_;
                        bitField0_ = (bitField0_ & ~0x00000040);
                    } else {
                        ensureCalendarNameIsMutable();
                        calendarName_.addAll(other.calendarName_);
                    }
                    onChanged();
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private long startTime_;

            /**
             * <code>int64 start_time = 1;</code>
             * 
             * @return Whether the startTime field is set.
             */
            @java.lang.Override
            public boolean hasStartTime() {
                return ((bitField0_ & 0x00000001) != 0);
            }

            /**
             * <code>int64 start_time = 1;</code>
             * 
             * @return The startTime.
             */
            @java.lang.Override
            public long getStartTime() {
                return startTime_;
            }

            /**
             * <code>int64 start_time = 1;</code>
             * 
             * @param value The startTime to set.
             * @return This builder for chaining.
             */
            public Builder setStartTime(long value) {
                bitField0_ |= 0x00000001;
                startTime_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 start_time = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearStartTime() {
                bitField0_ = (bitField0_ & ~0x00000001);
                startTime_ = 0L;
                onChanged();
                return this;
            }

            private long endTime_;

            /**
             * <code>int64 end_time = 2;</code>
             * 
             * @return Whether the endTime field is set.
             */
            @java.lang.Override
            public boolean hasEndTime() {
                return ((bitField0_ & 0x00000002) != 0);
            }

            /**
             * <code>int64 end_time = 2;</code>
             * 
             * @return The endTime.
             */
            @java.lang.Override
            public long getEndTime() {
                return endTime_;
            }

            /**
             * <code>int64 end_time = 2;</code>
             * 
             * @param value The endTime to set.
             * @return This builder for chaining.
             */
            public Builder setEndTime(long value) {
                bitField0_ |= 0x00000002;
                endTime_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 end_time = 2;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearEndTime() {
                bitField0_ = (bitField0_ & ~0x00000002);
                endTime_ = 0L;
                onChanged();
                return this;
            }

            private int repeatLimit_;

            /**
             * <code>int32 repeat_limit = 3;</code>
             * 
             * @return Whether the repeatLimit field is set.
             */
            @java.lang.Override
            public boolean hasRepeatLimit() {
                return ((bitField0_ & 0x00000004) != 0);
            }

            /**
             * <code>int32 repeat_limit = 3;</code>
             * 
             * @return The repeatLimit.
             */
            @java.lang.Override
            public int getRepeatLimit() {
                return repeatLimit_;
            }

            /**
             * <code>int32 repeat_limit = 3;</code>
             * 
             * @param value The repeatLimit to set.
             * @return This builder for chaining.
             */
            public Builder setRepeatLimit(int value) {
                bitField0_ |= 0x00000004;
                repeatLimit_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int32 repeat_limit = 3;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearRepeatLimit() {
                bitField0_ = (bitField0_ & ~0x00000004);
                repeatLimit_ = 0;
                onChanged();
                return this;
            }

            private int repeatCount_;

            /**
             * <code>int32 repeat_count = 4;</code>
             * 
             * @return Whether the repeatCount field is set.
             */
            @java.lang.Override
            public boolean hasRepeatCount() {
                return ((bitField0_ & 0x00000008) != 0);
            }

            /**
             * <code>int32 repeat_count = 4;</code>
             * 
             * @return The repeatCount.
             */
            @java.lang.Override
            public int getRepeatCount() {
                return repeatCount_;
            }

            /**
             * <code>int32 repeat_count = 4;</code>
             * 
             * @param value The repeatCount to set.
             * @return This builder for chaining.
             */
            public Builder setRepeatCount(int value) {
                bitField0_ |= 0x00000008;
                repeatCount_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int32 repeat_count = 4;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearRepeatCount() {
                bitField0_ = (bitField0_ & ~0x00000008);
                repeatCount_ = 0;
                onChanged();
                return this;
            }

            private long nextFireTime_;

            /**
             * <code>int64 next_fire_time = 6;</code>
             * 
             * @return Whether the nextFireTime field is set.
             */
            @java.lang.Override
            public boolean hasNextFireTime() {
                return ((bitField0_ & 0x00000010) != 0);
            }

            /**
             * <code>int64 next_fire_time = 6;</code>
             * 
             * @return The nextFireTime.
             */
            @java.lang.Override
            public long getNextFireTime() {
                return nextFireTime_;
            }

            /**
             * <code>int64 next_fire_time = 6;</code>
             * 
             * @param value The nextFireTime to set.
             * @return This builder for chaining.
             */
            public Builder setNextFireTime(long value) {
                bitField0_ |= 0x00000010;
                nextFireTime_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 next_fire_time = 6;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearNextFireTime() {
                bitField0_ = (bitField0_ & ~0x00000010);
                nextFireTime_ = 0L;
                onChanged();
                return this;
            }

            private long period_;

            /**
             * <code>int64 period = 7;</code>
             * 
             * @return Whether the period field is set.
             */
            @java.lang.Override
            public boolean hasPeriod() {
                return ((bitField0_ & 0x00000020) != 0);
            }

            /**
             * <code>int64 period = 7;</code>
             * 
             * @return The period.
             */
            @java.lang.Override
            public long getPeriod() {
                return period_;
            }

            /**
             * <code>int64 period = 7;</code>
             * 
             * @param value The period to set.
             * @return This builder for chaining.
             */
            public Builder setPeriod(long value) {
                bitField0_ |= 0x00000020;
                period_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 period = 7;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearPeriod() {
                bitField0_ = (bitField0_ & ~0x00000020);
                period_ = 0L;
                onChanged();
                return this;
            }

            private com.google.protobuf.LazyStringList calendarName_ = com.google.protobuf.LazyStringArrayList.EMPTY;

            private void ensureCalendarNameIsMutable() {
                if (!((bitField0_ & 0x00000040) != 0)) {
                    calendarName_ = new com.google.protobuf.LazyStringArrayList(calendarName_);
                    bitField0_ |= 0x00000040;
                }
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @return A list containing the calendarName.
             */
            public com.google.protobuf.ProtocolStringList
                    getCalendarNameList() {
                return calendarName_.getUnmodifiableView();
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @return The count of calendarName.
             */
            public int getCalendarNameCount() {
                return calendarName_.size();
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @param index The index of the element to return.
             * @return The calendarName at the given index.
             */
            public java.lang.String getCalendarName(int index) {
                return calendarName_.get(index);
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @param index The index of the value to return.
             * @return The bytes of the calendarName at the given index.
             */
            public com.google.protobuf.ByteString
                    getCalendarNameBytes(int index) {
                return calendarName_.getByteString(index);
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @param index The index to set the value at.
             * @param value The calendarName to set.
             * @return This builder for chaining.
             */
            public Builder setCalendarName(
                    int index, java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureCalendarNameIsMutable();
                calendarName_.set(index, value);
                onChanged();
                return this;
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @param value The calendarName to add.
             * @return This builder for chaining.
             */
            public Builder addCalendarName(
                    java.lang.String value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                ensureCalendarNameIsMutable();
                calendarName_.add(value);
                onChanged();
                return this;
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @param values The calendarName to add.
             * @return This builder for chaining.
             */
            public Builder addAllCalendarName(
                    java.lang.Iterable<java.lang.String> values) {
                ensureCalendarNameIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                        values, calendarName_);
                onChanged();
                return this;
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearCalendarName() {
                calendarName_ = com.google.protobuf.LazyStringArrayList.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000040);
                onChanged();
                return this;
            }

            /**
             * <code>repeated string calendar_name = 8;</code>
             * 
             * @param value The bytes of the calendarName to add.
             * @return This builder for chaining.
             */
            public Builder addCalendarNameBytes(
                    com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                checkByteStringIsUtf8(value);
                ensureCalendarNameIsMutable();
                calendarName_.add(value);
                onChanged();
                return this;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.IntervalTrigger)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.IntervalTrigger)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<IntervalTrigger> PARSER = new com.google.protobuf.AbstractParser<IntervalTrigger>() {
            @java.lang.Override
            public IntervalTrigger parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new IntervalTrigger(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<IntervalTrigger> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<IntervalTrigger> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.IntervalTrigger getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface PointInTimeTriggerOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.PointInTimeTrigger)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>int64 next_fire_time = 1;</code>
         * 
         * @return Whether the nextFireTime field is set.
         */
        boolean hasNextFireTime();

        /**
         * <code>int64 next_fire_time = 1;</code>
         * 
         * @return The nextFireTime.
         */
        long getNextFireTime();
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.PointInTimeTrigger}
     */
    public static final class PointInTimeTrigger extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.PointInTimeTrigger)
            PointInTimeTriggerOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use PointInTimeTrigger.newBuilder() to construct.
        private PointInTimeTrigger(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private PointInTimeTrigger() {
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new PointInTimeTrigger();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private PointInTimeTrigger(
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
                        case 8: {
                            bitField0_ |= 0x00000001;
                            nextFireTime_ = input.readInt64();
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
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger.class,
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger.Builder.class);
        }

        private int bitField0_;
        public static final int NEXT_FIRE_TIME_FIELD_NUMBER = 1;
        private long nextFireTime_;

        /**
         * <code>int64 next_fire_time = 1;</code>
         * 
         * @return Whether the nextFireTime field is set.
         */
        @java.lang.Override
        public boolean hasNextFireTime() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <code>int64 next_fire_time = 1;</code>
         * 
         * @return The nextFireTime.
         */
        @java.lang.Override
        public long getNextFireTime() {
            return nextFireTime_;
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
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeInt64(1, nextFireTime_);
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(1, nextFireTime_);
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger) obj;

            if (hasNextFireTime() != other.hasNextFireTime())
                return false;
            if (hasNextFireTime()) {
                if (getNextFireTime() != other.getNextFireTime())
                    return false;
            }
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
            if (hasNextFireTime()) {
                hash = (37 * hash) + NEXT_FIRE_TIME_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getNextFireTime());
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.PointInTimeTrigger}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.PointInTimeTrigger)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTriggerOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger.class,
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger.newBuilder()
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
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                nextFireTime_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) != 0)) {
                    result.nextFireTime_ = nextFireTime_;
                    to_bitField0_ |= 0x00000001;
                }
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger.getDefaultInstance())
                    return this;
                if (other.hasNextFireTime()) {
                    setNextFireTime(other.getNextFireTime());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private long nextFireTime_;

            /**
             * <code>int64 next_fire_time = 1;</code>
             * 
             * @return Whether the nextFireTime field is set.
             */
            @java.lang.Override
            public boolean hasNextFireTime() {
                return ((bitField0_ & 0x00000001) != 0);
            }

            /**
             * <code>int64 next_fire_time = 1;</code>
             * 
             * @return The nextFireTime.
             */
            @java.lang.Override
            public long getNextFireTime() {
                return nextFireTime_;
            }

            /**
             * <code>int64 next_fire_time = 1;</code>
             * 
             * @param value The nextFireTime to set.
             * @return This builder for chaining.
             */
            public Builder setNextFireTime(long value) {
                bitField0_ |= 0x00000001;
                nextFireTime_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 next_fire_time = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearNextFireTime() {
                bitField0_ = (bitField0_ & ~0x00000001);
                nextFireTime_ = 0L;
                onChanged();
                return this;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.PointInTimeTrigger)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.PointInTimeTrigger)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<PointInTimeTrigger> PARSER = new com.google.protobuf.AbstractParser<PointInTimeTrigger>() {
            @java.lang.Override
            public PointInTimeTrigger parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new PointInTimeTrigger(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<PointInTimeTrigger> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<PointInTimeTrigger> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.PointInTimeTrigger getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface CompositeMaxDurationTriggerOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.CompositeMaxDurationTrigger)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>int64 maxDurationTimestamp = 1;</code>
         * 
         * @return Whether the maxDurationTimestamp field is set.
         */
        boolean hasMaxDurationTimestamp();

        /**
         * <code>int64 maxDurationTimestamp = 1;</code>
         * 
         * @return The maxDurationTimestamp.
         */
        long getMaxDurationTimestamp();

        /**
         * <code>int64 timerCurrentDate = 2;</code>
         * 
         * @return Whether the timerCurrentDate field is set.
         */
        boolean hasTimerCurrentDate();

        /**
         * <code>int64 timerCurrentDate = 2;</code>
         * 
         * @return The timerCurrentDate.
         */
        long getTimerCurrentDate();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
         * 
         * @return Whether the timerTrigger field is set.
         */
        boolean hasTimerTrigger();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
         * 
         * @return The timerTrigger.
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getTimerTrigger();

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder getTimerTriggerOrBuilder();
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.CompositeMaxDurationTrigger}
     */
    public static final class CompositeMaxDurationTrigger extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.CompositeMaxDurationTrigger)
            CompositeMaxDurationTriggerOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use CompositeMaxDurationTrigger.newBuilder() to construct.
        private CompositeMaxDurationTrigger(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private CompositeMaxDurationTrigger() {
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new CompositeMaxDurationTrigger();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private CompositeMaxDurationTrigger(
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
                        case 8: {
                            bitField0_ |= 0x00000001;
                            maxDurationTimestamp_ = input.readInt64();
                            break;
                        }
                        case 16: {
                            bitField0_ |= 0x00000002;
                            timerCurrentDate_ = input.readInt64();
                            break;
                        }
                        case 26: {
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder subBuilder = null;
                            if (((bitField0_ & 0x00000004) != 0)) {
                                subBuilder = timerTrigger_.toBuilder();
                            }
                            timerTrigger_ = input.readMessage(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.parser(), extensionRegistry);
                            if (subBuilder != null) {
                                subBuilder.mergeFrom(timerTrigger_);
                                timerTrigger_ = subBuilder.buildPartial();
                            }
                            bitField0_ |= 0x00000004;
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
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger.class,
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger.Builder.class);
        }

        private int bitField0_;
        public static final int MAXDURATIONTIMESTAMP_FIELD_NUMBER = 1;
        private long maxDurationTimestamp_;

        /**
         * <code>int64 maxDurationTimestamp = 1;</code>
         * 
         * @return Whether the maxDurationTimestamp field is set.
         */
        @java.lang.Override
        public boolean hasMaxDurationTimestamp() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <code>int64 maxDurationTimestamp = 1;</code>
         * 
         * @return The maxDurationTimestamp.
         */
        @java.lang.Override
        public long getMaxDurationTimestamp() {
            return maxDurationTimestamp_;
        }

        public static final int TIMERCURRENTDATE_FIELD_NUMBER = 2;
        private long timerCurrentDate_;

        /**
         * <code>int64 timerCurrentDate = 2;</code>
         * 
         * @return Whether the timerCurrentDate field is set.
         */
        @java.lang.Override
        public boolean hasTimerCurrentDate() {
            return ((bitField0_ & 0x00000002) != 0);
        }

        /**
         * <code>int64 timerCurrentDate = 2;</code>
         * 
         * @return The timerCurrentDate.
         */
        @java.lang.Override
        public long getTimerCurrentDate() {
            return timerCurrentDate_;
        }

        public static final int TIMERTRIGGER_FIELD_NUMBER = 3;
        private org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger timerTrigger_;

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
         * 
         * @return Whether the timerTrigger field is set.
         */
        @java.lang.Override
        public boolean hasTimerTrigger() {
            return ((bitField0_ & 0x00000004) != 0);
        }

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
         * 
         * @return The timerTrigger.
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getTimerTrigger() {
            return timerTrigger_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance() : timerTrigger_;
        }

        /**
         * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder getTimerTriggerOrBuilder() {
            return timerTrigger_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance() : timerTrigger_;
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
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeInt64(1, maxDurationTimestamp_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                output.writeInt64(2, timerCurrentDate_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                output.writeMessage(3, getTimerTrigger());
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(1, maxDurationTimestamp_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(2, timerCurrentDate_);
            }
            if (((bitField0_ & 0x00000004) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(3, getTimerTrigger());
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger) obj;

            if (hasMaxDurationTimestamp() != other.hasMaxDurationTimestamp())
                return false;
            if (hasMaxDurationTimestamp()) {
                if (getMaxDurationTimestamp() != other.getMaxDurationTimestamp())
                    return false;
            }
            if (hasTimerCurrentDate() != other.hasTimerCurrentDate())
                return false;
            if (hasTimerCurrentDate()) {
                if (getTimerCurrentDate() != other.getTimerCurrentDate())
                    return false;
            }
            if (hasTimerTrigger() != other.hasTimerTrigger())
                return false;
            if (hasTimerTrigger()) {
                if (!getTimerTrigger()
                        .equals(other.getTimerTrigger()))
                    return false;
            }
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
            if (hasMaxDurationTimestamp()) {
                hash = (37 * hash) + MAXDURATIONTIMESTAMP_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getMaxDurationTimestamp());
            }
            if (hasTimerCurrentDate()) {
                hash = (37 * hash) + TIMERCURRENTDATE_FIELD_NUMBER;
                hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                        getTimerCurrentDate());
            }
            if (hasTimerTrigger()) {
                hash = (37 * hash) + TIMERTRIGGER_FIELD_NUMBER;
                hash = (53 * hash) + getTimerTrigger().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.CompositeMaxDurationTrigger}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.CompositeMaxDurationTrigger)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTriggerOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger.class,
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger.newBuilder()
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
                    getTimerTriggerFieldBuilder();
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                maxDurationTimestamp_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000001);
                timerCurrentDate_ = 0L;
                bitField0_ = (bitField0_ & ~0x00000002);
                if (timerTriggerBuilder_ == null) {
                    timerTrigger_ = null;
                } else {
                    timerTriggerBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger result =
                        new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) != 0)) {
                    result.maxDurationTimestamp_ = maxDurationTimestamp_;
                    to_bitField0_ |= 0x00000001;
                }
                if (((from_bitField0_ & 0x00000002) != 0)) {
                    result.timerCurrentDate_ = timerCurrentDate_;
                    to_bitField0_ |= 0x00000002;
                }
                if (((from_bitField0_ & 0x00000004) != 0)) {
                    if (timerTriggerBuilder_ == null) {
                        result.timerTrigger_ = timerTrigger_;
                    } else {
                        result.timerTrigger_ = timerTriggerBuilder_.build();
                    }
                    to_bitField0_ |= 0x00000004;
                }
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger.getDefaultInstance())
                    return this;
                if (other.hasMaxDurationTimestamp()) {
                    setMaxDurationTimestamp(other.getMaxDurationTimestamp());
                }
                if (other.hasTimerCurrentDate()) {
                    setTimerCurrentDate(other.getTimerCurrentDate());
                }
                if (other.hasTimerTrigger()) {
                    mergeTimerTrigger(other.getTimerTrigger());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private long maxDurationTimestamp_;

            /**
             * <code>int64 maxDurationTimestamp = 1;</code>
             * 
             * @return Whether the maxDurationTimestamp field is set.
             */
            @java.lang.Override
            public boolean hasMaxDurationTimestamp() {
                return ((bitField0_ & 0x00000001) != 0);
            }

            /**
             * <code>int64 maxDurationTimestamp = 1;</code>
             * 
             * @return The maxDurationTimestamp.
             */
            @java.lang.Override
            public long getMaxDurationTimestamp() {
                return maxDurationTimestamp_;
            }

            /**
             * <code>int64 maxDurationTimestamp = 1;</code>
             * 
             * @param value The maxDurationTimestamp to set.
             * @return This builder for chaining.
             */
            public Builder setMaxDurationTimestamp(long value) {
                bitField0_ |= 0x00000001;
                maxDurationTimestamp_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 maxDurationTimestamp = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearMaxDurationTimestamp() {
                bitField0_ = (bitField0_ & ~0x00000001);
                maxDurationTimestamp_ = 0L;
                onChanged();
                return this;
            }

            private long timerCurrentDate_;

            /**
             * <code>int64 timerCurrentDate = 2;</code>
             * 
             * @return Whether the timerCurrentDate field is set.
             */
            @java.lang.Override
            public boolean hasTimerCurrentDate() {
                return ((bitField0_ & 0x00000002) != 0);
            }

            /**
             * <code>int64 timerCurrentDate = 2;</code>
             * 
             * @return The timerCurrentDate.
             */
            @java.lang.Override
            public long getTimerCurrentDate() {
                return timerCurrentDate_;
            }

            /**
             * <code>int64 timerCurrentDate = 2;</code>
             * 
             * @param value The timerCurrentDate to set.
             * @return This builder for chaining.
             */
            public Builder setTimerCurrentDate(long value) {
                bitField0_ |= 0x00000002;
                timerCurrentDate_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 timerCurrentDate = 2;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearTimerCurrentDate() {
                bitField0_ = (bitField0_ & ~0x00000002);
                timerCurrentDate_ = 0L;
                onChanged();
                return this;
            }

            private org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger timerTrigger_;
            private com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder> timerTriggerBuilder_;

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             * 
             * @return Whether the timerTrigger field is set.
             */
            public boolean hasTimerTrigger() {
                return ((bitField0_ & 0x00000004) != 0);
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             * 
             * @return The timerTrigger.
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger getTimerTrigger() {
                if (timerTriggerBuilder_ == null) {
                    return timerTrigger_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance() : timerTrigger_;
                } else {
                    return timerTriggerBuilder_.getMessage();
                }
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             */
            public Builder setTimerTrigger(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger value) {
                if (timerTriggerBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    timerTrigger_ = value;
                    onChanged();
                } else {
                    timerTriggerBuilder_.setMessage(value);
                }
                bitField0_ |= 0x00000004;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             */
            public Builder setTimerTrigger(
                    org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder builderForValue) {
                if (timerTriggerBuilder_ == null) {
                    timerTrigger_ = builderForValue.build();
                    onChanged();
                } else {
                    timerTriggerBuilder_.setMessage(builderForValue.build());
                }
                bitField0_ |= 0x00000004;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             */
            public Builder mergeTimerTrigger(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger value) {
                if (timerTriggerBuilder_ == null) {
                    if (((bitField0_ & 0x00000004) != 0) &&
                            timerTrigger_ != null &&
                            timerTrigger_ != org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance()) {
                        timerTrigger_ =
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.newBuilder(timerTrigger_).mergeFrom(value).buildPartial();
                    } else {
                        timerTrigger_ = value;
                    }
                    onChanged();
                } else {
                    timerTriggerBuilder_.mergeFrom(value);
                }
                bitField0_ |= 0x00000004;
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             */
            public Builder clearTimerTrigger() {
                if (timerTriggerBuilder_ == null) {
                    timerTrigger_ = null;
                    onChanged();
                } else {
                    timerTriggerBuilder_.clear();
                }
                bitField0_ = (bitField0_ & ~0x00000004);
                return this;
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder getTimerTriggerBuilder() {
                bitField0_ |= 0x00000004;
                onChanged();
                return getTimerTriggerFieldBuilder().getBuilder();
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder getTimerTriggerOrBuilder() {
                if (timerTriggerBuilder_ != null) {
                    return timerTriggerBuilder_.getMessageOrBuilder();
                } else {
                    return timerTrigger_ == null ? org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.getDefaultInstance() : timerTrigger_;
                }
            }

            /**
             * <code>.org.kie.kogito.serialization.protobuf.Trigger timerTrigger = 3;</code>
             */
            private com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder>
                    getTimerTriggerFieldBuilder() {
                if (timerTriggerBuilder_ == null) {
                    timerTriggerBuilder_ =
                            new com.google.protobuf.SingleFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Trigger.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TriggerOrBuilder>(
                                    getTimerTrigger(),
                                    getParentForChildren(),
                                    isClean());
                    timerTrigger_ = null;
                }
                return timerTriggerBuilder_;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.CompositeMaxDurationTrigger)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.CompositeMaxDurationTrigger)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<CompositeMaxDurationTrigger> PARSER = new com.google.protobuf.AbstractParser<CompositeMaxDurationTrigger>() {
            @java.lang.Override
            public CompositeMaxDurationTrigger parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new CompositeMaxDurationTrigger(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<CompositeMaxDurationTrigger> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<CompositeMaxDurationTrigger> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.CompositeMaxDurationTrigger getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface TupleOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.Tuple)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>repeated int64 handle_id = 1;</code>
         * 
         * @return A list containing the handleId.
         */
        java.util.List<java.lang.Long> getHandleIdList();

        /**
         * <code>repeated int64 handle_id = 1;</code>
         * 
         * @return The count of handleId.
         */
        int getHandleIdCount();

        /**
         * <code>repeated int64 handle_id = 1;</code>
         * 
         * @param index The index of the element to return.
         * @return The handleId at the given index.
         */
        long getHandleId(int index);

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject>
                getObjectList();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject getObject(int index);

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        int getObjectCount();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder>
                getObjectOrBuilderList();

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder getObjectOrBuilder(
                int index);
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.Tuple}
     */
    public static final class Tuple extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.Tuple)
            TupleOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use Tuple.newBuilder() to construct.
        private Tuple(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private Tuple() {
            handleId_ = emptyLongList();
            object_ = java.util.Collections.emptyList();
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new Tuple();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private Tuple(
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
                        case 8: {
                            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
                                handleId_ = newLongList();
                                mutable_bitField0_ |= 0x00000001;
                            }
                            handleId_.addLong(input.readInt64());
                            break;
                        }
                        case 10: {
                            int length = input.readRawVarint32();
                            int limit = input.pushLimit(length);
                            if (!((mutable_bitField0_ & 0x00000001) != 0) && input.getBytesUntilLimit() > 0) {
                                handleId_ = newLongList();
                                mutable_bitField0_ |= 0x00000001;
                            }
                            while (input.getBytesUntilLimit() > 0) {
                                handleId_.addLong(input.readInt64());
                            }
                            input.popLimit(limit);
                            break;
                        }
                        case 18: {
                            if (!((mutable_bitField0_ & 0x00000002) != 0)) {
                                object_ = new java.util.ArrayList<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject>();
                                mutable_bitField0_ |= 0x00000002;
                            }
                            object_.add(
                                    input.readMessage(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.parser(), extensionRegistry));
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
                    handleId_.makeImmutable(); // C
                }
                if (((mutable_bitField0_ & 0x00000002) != 0)) {
                    object_ = java.util.Collections.unmodifiableList(object_);
                }
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Tuple_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Tuple_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.Builder.class);
        }

        public static final int HANDLE_ID_FIELD_NUMBER = 1;
        private com.google.protobuf.Internal.LongList handleId_;

        /**
         * <code>repeated int64 handle_id = 1;</code>
         * 
         * @return A list containing the handleId.
         */
        @java.lang.Override
        public java.util.List<java.lang.Long>
                getHandleIdList() {
            return handleId_;
        }

        /**
         * <code>repeated int64 handle_id = 1;</code>
         * 
         * @return The count of handleId.
         */
        public int getHandleIdCount() {
            return handleId_.size();
        }

        /**
         * <code>repeated int64 handle_id = 1;</code>
         * 
         * @param index The index of the element to return.
         * @return The handleId at the given index.
         */
        public long getHandleId(int index) {
            return handleId_.getLong(index);
        }

        private int handleIdMemoizedSerializedSize = -1;

        public static final int OBJECT_FIELD_NUMBER = 2;
        private java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject> object_;

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        @java.lang.Override
        public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject> getObjectList() {
            return object_;
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        @java.lang.Override
        public java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder>
                getObjectOrBuilderList() {
            return object_;
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        @java.lang.Override
        public int getObjectCount() {
            return object_.size();
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject getObject(int index) {
            return object_.get(index);
        }

        /**
         * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
         */
        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder getObjectOrBuilder(
                int index) {
            return object_.get(index);
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
            getSerializedSize();
            if (getHandleIdList().size() > 0) {
                output.writeUInt32NoTag(10);
                output.writeUInt32NoTag(handleIdMemoizedSerializedSize);
            }
            for (int i = 0; i < handleId_.size(); i++) {
                output.writeInt64NoTag(handleId_.getLong(i));
            }
            for (int i = 0; i < object_.size(); i++) {
                output.writeMessage(2, object_.get(i));
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            {
                int dataSize = 0;
                for (int i = 0; i < handleId_.size(); i++) {
                    dataSize += com.google.protobuf.CodedOutputStream
                            .computeInt64SizeNoTag(handleId_.getLong(i));
                }
                size += dataSize;
                if (!getHandleIdList().isEmpty()) {
                    size += 1;
                    size += com.google.protobuf.CodedOutputStream
                            .computeInt32SizeNoTag(dataSize);
                }
                handleIdMemoizedSerializedSize = dataSize;
            }
            for (int i = 0; i < object_.size(); i++) {
                size += com.google.protobuf.CodedOutputStream
                        .computeMessageSize(2, object_.get(i));
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple) obj;

            if (!getHandleIdList()
                    .equals(other.getHandleIdList()))
                return false;
            if (!getObjectList()
                    .equals(other.getObjectList()))
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
            if (getHandleIdCount() > 0) {
                hash = (37 * hash) + HANDLE_ID_FIELD_NUMBER;
                hash = (53 * hash) + getHandleIdList().hashCode();
            }
            if (getObjectCount() > 0) {
                hash = (37 * hash) + OBJECT_FIELD_NUMBER;
                hash = (53 * hash) + getObjectList().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.Tuple}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.Tuple)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.TupleOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Tuple_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Tuple_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.class, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.newBuilder()
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
                    getObjectFieldBuilder();
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                handleId_ = emptyLongList();
                bitField0_ = (bitField0_ & ~0x00000001);
                if (objectBuilder_ == null) {
                    object_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000002);
                } else {
                    objectBuilder_.clear();
                }
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_Tuple_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple(this);
                int from_bitField0_ = bitField0_;
                if (((bitField0_ & 0x00000001) != 0)) {
                    handleId_.makeImmutable();
                    bitField0_ = (bitField0_ & ~0x00000001);
                }
                result.handleId_ = handleId_;
                if (objectBuilder_ == null) {
                    if (((bitField0_ & 0x00000002) != 0)) {
                        object_ = java.util.Collections.unmodifiableList(object_);
                        bitField0_ = (bitField0_ & ~0x00000002);
                    }
                    result.object_ = object_;
                } else {
                    result.object_ = objectBuilder_.build();
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple.getDefaultInstance())
                    return this;
                if (!other.handleId_.isEmpty()) {
                    if (handleId_.isEmpty()) {
                        handleId_ = other.handleId_;
                        bitField0_ = (bitField0_ & ~0x00000001);
                    } else {
                        ensureHandleIdIsMutable();
                        handleId_.addAll(other.handleId_);
                    }
                    onChanged();
                }
                if (objectBuilder_ == null) {
                    if (!other.object_.isEmpty()) {
                        if (object_.isEmpty()) {
                            object_ = other.object_;
                            bitField0_ = (bitField0_ & ~0x00000002);
                        } else {
                            ensureObjectIsMutable();
                            object_.addAll(other.object_);
                        }
                        onChanged();
                    }
                } else {
                    if (!other.object_.isEmpty()) {
                        if (objectBuilder_.isEmpty()) {
                            objectBuilder_.dispose();
                            objectBuilder_ = null;
                            object_ = other.object_;
                            bitField0_ = (bitField0_ & ~0x00000002);
                            objectBuilder_ =
                                    com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ? getObjectFieldBuilder() : null;
                        } else {
                            objectBuilder_.addAllMessages(other.object_);
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private com.google.protobuf.Internal.LongList handleId_ = emptyLongList();

            private void ensureHandleIdIsMutable() {
                if (!((bitField0_ & 0x00000001) != 0)) {
                    handleId_ = mutableCopy(handleId_);
                    bitField0_ |= 0x00000001;
                }
            }

            /**
             * <code>repeated int64 handle_id = 1;</code>
             * 
             * @return A list containing the handleId.
             */
            public java.util.List<java.lang.Long>
                    getHandleIdList() {
                return ((bitField0_ & 0x00000001) != 0) ? java.util.Collections.unmodifiableList(handleId_) : handleId_;
            }

            /**
             * <code>repeated int64 handle_id = 1;</code>
             * 
             * @return The count of handleId.
             */
            public int getHandleIdCount() {
                return handleId_.size();
            }

            /**
             * <code>repeated int64 handle_id = 1;</code>
             * 
             * @param index The index of the element to return.
             * @return The handleId at the given index.
             */
            public long getHandleId(int index) {
                return handleId_.getLong(index);
            }

            /**
             * <code>repeated int64 handle_id = 1;</code>
             * 
             * @param index The index to set the value at.
             * @param value The handleId to set.
             * @return This builder for chaining.
             */
            public Builder setHandleId(
                    int index, long value) {
                ensureHandleIdIsMutable();
                handleId_.setLong(index, value);
                onChanged();
                return this;
            }

            /**
             * <code>repeated int64 handle_id = 1;</code>
             * 
             * @param value The handleId to add.
             * @return This builder for chaining.
             */
            public Builder addHandleId(long value) {
                ensureHandleIdIsMutable();
                handleId_.addLong(value);
                onChanged();
                return this;
            }

            /**
             * <code>repeated int64 handle_id = 1;</code>
             * 
             * @param values The handleId to add.
             * @return This builder for chaining.
             */
            public Builder addAllHandleId(
                    java.lang.Iterable<? extends java.lang.Long> values) {
                ensureHandleIdIsMutable();
                com.google.protobuf.AbstractMessageLite.Builder.addAll(
                        values, handleId_);
                onChanged();
                return this;
            }

            /**
             * <code>repeated int64 handle_id = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearHandleId() {
                handleId_ = emptyLongList();
                bitField0_ = (bitField0_ & ~0x00000001);
                onChanged();
                return this;
            }

            private java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject> object_ =
                    java.util.Collections.emptyList();

            private void ensureObjectIsMutable() {
                if (!((bitField0_ & 0x00000002) != 0)) {
                    object_ = new java.util.ArrayList<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject>(object_);
                    bitField0_ |= 0x00000002;
                }
            }

            private com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder> objectBuilder_;

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject> getObjectList() {
                if (objectBuilder_ == null) {
                    return java.util.Collections.unmodifiableList(object_);
                } else {
                    return objectBuilder_.getMessageList();
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public int getObjectCount() {
                if (objectBuilder_ == null) {
                    return object_.size();
                } else {
                    return objectBuilder_.getCount();
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject getObject(int index) {
                if (objectBuilder_ == null) {
                    return object_.get(index);
                } else {
                    return objectBuilder_.getMessage(index);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder setObject(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject value) {
                if (objectBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureObjectIsMutable();
                    object_.set(index, value);
                    onChanged();
                } else {
                    objectBuilder_.setMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder setObject(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder builderForValue) {
                if (objectBuilder_ == null) {
                    ensureObjectIsMutable();
                    object_.set(index, builderForValue.build());
                    onChanged();
                } else {
                    objectBuilder_.setMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder addObject(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject value) {
                if (objectBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureObjectIsMutable();
                    object_.add(value);
                    onChanged();
                } else {
                    objectBuilder_.addMessage(value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder addObject(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject value) {
                if (objectBuilder_ == null) {
                    if (value == null) {
                        throw new NullPointerException();
                    }
                    ensureObjectIsMutable();
                    object_.add(index, value);
                    onChanged();
                } else {
                    objectBuilder_.addMessage(index, value);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder addObject(
                    org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder builderForValue) {
                if (objectBuilder_ == null) {
                    ensureObjectIsMutable();
                    object_.add(builderForValue.build());
                    onChanged();
                } else {
                    objectBuilder_.addMessage(builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder addObject(
                    int index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder builderForValue) {
                if (objectBuilder_ == null) {
                    ensureObjectIsMutable();
                    object_.add(index, builderForValue.build());
                    onChanged();
                } else {
                    objectBuilder_.addMessage(index, builderForValue.build());
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder addAllObject(
                    java.lang.Iterable<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject> values) {
                if (objectBuilder_ == null) {
                    ensureObjectIsMutable();
                    com.google.protobuf.AbstractMessageLite.Builder.addAll(
                            values, object_);
                    onChanged();
                } else {
                    objectBuilder_.addAllMessages(values);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder clearObject() {
                if (objectBuilder_ == null) {
                    object_ = java.util.Collections.emptyList();
                    bitField0_ = (bitField0_ & ~0x00000002);
                    onChanged();
                } else {
                    objectBuilder_.clear();
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public Builder removeObject(int index) {
                if (objectBuilder_ == null) {
                    ensureObjectIsMutable();
                    object_.remove(index);
                    onChanged();
                } else {
                    objectBuilder_.remove(index);
                }
                return this;
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder getObjectBuilder(
                    int index) {
                return getObjectFieldBuilder().getBuilder(index);
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder getObjectOrBuilder(
                    int index) {
                if (objectBuilder_ == null) {
                    return object_.get(index);
                } else {
                    return objectBuilder_.getMessageOrBuilder(index);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public java.util.List<? extends org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder>
                    getObjectOrBuilderList() {
                if (objectBuilder_ != null) {
                    return objectBuilder_.getMessageOrBuilderList();
                } else {
                    return java.util.Collections.unmodifiableList(object_);
                }
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder addObjectBuilder() {
                return getObjectFieldBuilder().addBuilder(
                        org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.getDefaultInstance());
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder addObjectBuilder(
                    int index) {
                return getObjectFieldBuilder().addBuilder(
                        index, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.getDefaultInstance());
            }

            /**
             * <code>repeated .org.kie.kogito.serialization.protobuf.SerializedObject object = 2;</code>
             */
            public java.util.List<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder>
                    getObjectBuilderList() {
                return getObjectFieldBuilder().getBuilderList();
            }

            private com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder>
                    getObjectFieldBuilder() {
                if (objectBuilder_ == null) {
                    objectBuilder_ =
                            new com.google.protobuf.RepeatedFieldBuilderV3<org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder, org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder>(
                                    object_,
                                    ((bitField0_ & 0x00000002) != 0),
                                    getParentForChildren(),
                                    isClean());
                    object_ = null;
                }
                return objectBuilder_;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.Tuple)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.Tuple)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<Tuple> PARSER = new com.google.protobuf.AbstractParser<Tuple>() {
            @java.lang.Override
            public Tuple parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new Tuple(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<Tuple> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<Tuple> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.Tuple getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    public interface SerializedObjectOrBuilder extends
            // @@protoc_insertion_point(interface_extends:org.kie.kogito.serialization.protobuf.SerializedObject)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>int32 strategy_index = 1;</code>
         * 
         * @return Whether the strategyIndex field is set.
         */
        boolean hasStrategyIndex();

        /**
         * <code>int32 strategy_index = 1;</code>
         * 
         * @return The strategyIndex.
         */
        int getStrategyIndex();

        /**
         * <code>bytes object = 2;</code>
         * 
         * @return Whether the object field is set.
         */
        boolean hasObject();

        /**
         * <code>bytes object = 2;</code>
         * 
         * @return The object.
         */
        com.google.protobuf.ByteString getObject();
    }

    /**
     * Protobuf type {@code org.kie.kogito.serialization.protobuf.SerializedObject}
     */
    public static final class SerializedObject extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:org.kie.kogito.serialization.protobuf.SerializedObject)
            SerializedObjectOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use SerializedObject.newBuilder() to construct.
        private SerializedObject(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private SerializedObject() {
            object_ = com.google.protobuf.ByteString.EMPTY;
        }

        @java.lang.Override
        @SuppressWarnings({ "unused" })
        protected java.lang.Object newInstance(
                UnusedPrivateParameter unused) {
            return new SerializedObject();
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
                getUnknownFields() {
            return this.unknownFields;
        }

        private SerializedObject(
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
                        case 8: {
                            bitField0_ |= 0x00000001;
                            strategyIndex_ = input.readInt32();
                            break;
                        }
                        case 18: {
                            bitField0_ |= 0x00000002;
                            object_ = input.readBytes();
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
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
                getDescriptor() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                internalGetFieldAccessorTable() {
            return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.class,
                            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder.class);
        }

        private int bitField0_;
        public static final int STRATEGY_INDEX_FIELD_NUMBER = 1;
        private int strategyIndex_;

        /**
         * <code>int32 strategy_index = 1;</code>
         * 
         * @return Whether the strategyIndex field is set.
         */
        @java.lang.Override
        public boolean hasStrategyIndex() {
            return ((bitField0_ & 0x00000001) != 0);
        }

        /**
         * <code>int32 strategy_index = 1;</code>
         * 
         * @return The strategyIndex.
         */
        @java.lang.Override
        public int getStrategyIndex() {
            return strategyIndex_;
        }

        public static final int OBJECT_FIELD_NUMBER = 2;
        private com.google.protobuf.ByteString object_;

        /**
         * <code>bytes object = 2;</code>
         * 
         * @return Whether the object field is set.
         */
        @java.lang.Override
        public boolean hasObject() {
            return ((bitField0_ & 0x00000002) != 0);
        }

        /**
         * <code>bytes object = 2;</code>
         * 
         * @return The object.
         */
        @java.lang.Override
        public com.google.protobuf.ByteString getObject() {
            return object_;
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
            if (((bitField0_ & 0x00000001) != 0)) {
                output.writeInt32(1, strategyIndex_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                output.writeBytes(2, object_);
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1)
                return size;

            size = 0;
            if (((bitField0_ & 0x00000001) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(1, strategyIndex_);
            }
            if (((bitField0_ & 0x00000002) != 0)) {
                size += com.google.protobuf.CodedOutputStream
                        .computeBytesSize(2, object_);
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
            if (!(obj instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject)) {
                return super.equals(obj);
            }
            org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject other = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject) obj;

            if (hasStrategyIndex() != other.hasStrategyIndex())
                return false;
            if (hasStrategyIndex()) {
                if (getStrategyIndex() != other.getStrategyIndex())
                    return false;
            }
            if (hasObject() != other.hasObject())
                return false;
            if (hasObject()) {
                if (!getObject()
                        .equals(other.getObject()))
                    return false;
            }
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
            if (hasStrategyIndex()) {
                hash = (37 * hash) + STRATEGY_INDEX_FIELD_NUMBER;
                hash = (53 * hash) + getStrategyIndex();
            }
            if (hasObject()) {
                hash = (37 * hash) + OBJECT_FIELD_NUMBER;
                hash = (53 * hash) + getObject().hashCode();
            }
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parseFrom(
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

        public static Builder newBuilder(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject prototype) {
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
         * Protobuf type {@code org.kie.kogito.serialization.protobuf.SerializedObject}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:org.kie.kogito.serialization.protobuf.SerializedObject)
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObjectOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
                    getDescriptor() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                    internalGetFieldAccessorTable() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.class,
                                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.Builder.class);
            }

            // Construct using org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.newBuilder()
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
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                strategyIndex_ = 0;
                bitField0_ = (bitField0_ & ~0x00000001);
                object_ = com.google.protobuf.ByteString.EMPTY;
                bitField0_ = (bitField0_ & ~0x00000002);
                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
                    getDescriptorForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_descriptor;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject getDefaultInstanceForType() {
                return org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.getDefaultInstance();
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject build() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject buildPartial() {
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject result = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject(this);
                int from_bitField0_ = bitField0_;
                int to_bitField0_ = 0;
                if (((from_bitField0_ & 0x00000001) != 0)) {
                    result.strategyIndex_ = strategyIndex_;
                    to_bitField0_ |= 0x00000001;
                }
                if (((from_bitField0_ & 0x00000002) != 0)) {
                    to_bitField0_ |= 0x00000002;
                }
                result.object_ = object_;
                result.bitField0_ = to_bitField0_;
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
                if (other instanceof org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject) {
                    return mergeFrom((org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject other) {
                if (other == org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject.getDefaultInstance())
                    return this;
                if (other.hasStrategyIndex()) {
                    setStrategyIndex(other.getStrategyIndex());
                }
                if (other.hasObject()) {
                    setObject(other.getObject());
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
                org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private int bitField0_;

            private int strategyIndex_;

            /**
             * <code>int32 strategy_index = 1;</code>
             * 
             * @return Whether the strategyIndex field is set.
             */
            @java.lang.Override
            public boolean hasStrategyIndex() {
                return ((bitField0_ & 0x00000001) != 0);
            }

            /**
             * <code>int32 strategy_index = 1;</code>
             * 
             * @return The strategyIndex.
             */
            @java.lang.Override
            public int getStrategyIndex() {
                return strategyIndex_;
            }

            /**
             * <code>int32 strategy_index = 1;</code>
             * 
             * @param value The strategyIndex to set.
             * @return This builder for chaining.
             */
            public Builder setStrategyIndex(int value) {
                bitField0_ |= 0x00000001;
                strategyIndex_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int32 strategy_index = 1;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearStrategyIndex() {
                bitField0_ = (bitField0_ & ~0x00000001);
                strategyIndex_ = 0;
                onChanged();
                return this;
            }

            private com.google.protobuf.ByteString object_ = com.google.protobuf.ByteString.EMPTY;

            /**
             * <code>bytes object = 2;</code>
             * 
             * @return Whether the object field is set.
             */
            @java.lang.Override
            public boolean hasObject() {
                return ((bitField0_ & 0x00000002) != 0);
            }

            /**
             * <code>bytes object = 2;</code>
             * 
             * @return The object.
             */
            @java.lang.Override
            public com.google.protobuf.ByteString getObject() {
                return object_;
            }

            /**
             * <code>bytes object = 2;</code>
             * 
             * @param value The object to set.
             * @return This builder for chaining.
             */
            public Builder setObject(com.google.protobuf.ByteString value) {
                if (value == null) {
                    throw new NullPointerException();
                }
                bitField0_ |= 0x00000002;
                object_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>bytes object = 2;</code>
             * 
             * @return This builder for chaining.
             */
            public Builder clearObject() {
                bitField0_ = (bitField0_ & ~0x00000002);
                object_ = getDefaultInstance().getObject();
                onChanged();
                return this;
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

            // @@protoc_insertion_point(builder_scope:org.kie.kogito.serialization.protobuf.SerializedObject)
        }

        // @@protoc_insertion_point(class_scope:org.kie.kogito.serialization.protobuf.SerializedObject)
        private static final org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject DEFAULT_INSTANCE;
        static {
            DEFAULT_INSTANCE = new org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject();
        }

        public static org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<SerializedObject> PARSER = new com.google.protobuf.AbstractParser<SerializedObject>() {
            @java.lang.Override
            public SerializedObject parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new SerializedObject(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<SerializedObject> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<SerializedObject> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public org.kie.kogito.serialization.protobuf.KogitoTimersProtobuf.SerializedObject getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_Timers_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_Timers_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_Timer_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_Timer_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_Trigger_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_Trigger_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_Tuple_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_Tuple_fieldAccessorTable;
    private static final com.google.protobuf.Descriptors.Descriptor internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_descriptor;
    private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor
            getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
    static {
        java.lang.String[] descriptorData = {
                "\n(org/jbpm/marshalling/kogito_timers.pro" +
                        "to\022%org.kie.kogito.serialization.protobu" +
                        "f\032\031google/protobuf/any.proto\"\242\001\n\006Timers\022" +
                        ";\n\005timer\030\001 \003(\0132,.org.kie.kogito.serializ" +
                        "ation.protobuf.Timer\022L\n\nproc_timer\030\002 \001(\013" +
                        "23.org.kie.kogito.serialization.protobuf" +
                        ".ProcessTimerH\000\210\001\001B\r\n\013_proc_timer\"\305\001\n\005Ti" +
                        "mer\022D\n\004type\030\001 \001(\01626.org.kie.kogito.seria" +
                        "lization.protobuf.Timer.TimerType\022\"\n\004dat" +
                        "a\030\002 \001(\0132\024.google.protobuf.Any\"R\n\tTimerTy" +
                        "pe\022\n\n\006EXPIRE\020\000\022\016\n\nACTIVATION\020\001\022\014\n\010BEHAVI" +
                        "OR\020\002\022\013\n\007PROCESS\020\003\022\016\n\nTIMER_NODE\020\004\"\311\001\n\017Ex" +
                        "pireTimerData\022\026\n\thandle_id\030\001 \001(\003H\000\210\001\001\022\033\n" +
                        "\016entry_point_id\030\002 \001(\tH\001\210\001\001\022\027\n\nclass_name" +
                        "\030\003 \001(\tH\002\210\001\001\022 \n\023next_fire_timestamp\030\004 \001(\003" +
                        "H\003\210\001\001B\014\n\n_handle_idB\021\n\017_entry_point_idB\r" +
                        "\n\013_class_nameB\026\n\024_next_fire_timestamp\"\202\001" +
                        "\n\022TimerNodeTimerData\022\024\n\007node_id\030\001 \001(\005H\000\210" +
                        "\001\001\022@\n\005tuple\030\002 \001(\0132,.org.kie.kogito.seria" +
                        "lization.protobuf.TupleH\001\210\001\001B\n\n\010_node_id" +
                        "B\010\n\006_tuple\"\236\003\n\014ProcessTimer\022\017\n\002id\030\001 \001(\003H" +
                        "\000\210\001\001\022\025\n\010timer_id\030\002 \001(\tH\001\210\001\001\022\022\n\005delay\030\003 \001" +
                        "(\003H\002\210\001\001\022\023\n\006period\030\004 \001(\003H\003\210\001\001\022 \n\023process_" +
                        "instance_id\030\005 \001(\tH\004\210\001\001\022\033\n\016activated_time" +
                        "\030\006 \001(\003H\005\210\001\001\022\033\n\016last_triggered\030\007 \001(\003H\006\210\001\001" +
                        "\022\030\n\013repeatLimit\030\n \001(\005H\007\210\001\001\022D\n\007trigger\030\013 " +
                        "\001(\0132..org.kie.kogito.serialization.proto" +
                        "buf.TriggerH\010\210\001\001B\005\n\003_idB\013\n\t_timer_idB\010\n\006" +
                        "_delayB\t\n\007_periodB\026\n\024_process_instance_i" +
                        "dB\021\n\017_activated_timeB\021\n\017_last_triggeredB" +
                        "\016\n\014_repeatLimitB\n\n\010_trigger\"\320\001\n\007Trigger\022" +
                        "H\n\004type\030\013 \001(\0162:.org.kie.kogito.serializa" +
                        "tion.protobuf.Trigger.TriggerType\022%\n\007tri" +
                        "gger\030\014 \001(\0132\024.google.protobuf.Any\"T\n\013Trig" +
                        "gerType\022\010\n\004CRON\020\000\022\014\n\010INTERVAL\020\001\022\021\n\rPOINT" +
                        "_IN_TIME\020\002\022\032\n\026COMPOSITE_MAX_DURATION\020\003\"\252" +
                        "\002\n\013CronTrigger\022\027\n\nstart_time\030\001 \001(\003H\000\210\001\001\022" +
                        "\025\n\010end_time\030\002 \001(\003H\001\210\001\001\022\031\n\014repeat_limit\030\003" +
                        " \001(\005H\002\210\001\001\022\031\n\014repeat_count\030\004 \001(\005H\003\210\001\001\022\034\n\017" +
                        "cron_expression\030\005 \001(\tH\004\210\001\001\022\033\n\016next_fire_" +
                        "time\030\006 \001(\003H\005\210\001\001\022\025\n\rcalendar_name\030\007 \003(\tB\r" +
                        "\n\013_start_timeB\013\n\t_end_timeB\017\n\r_repeat_li" +
                        "mitB\017\n\r_repeat_countB\022\n\020_cron_expression" +
                        "B\021\n\017_next_fire_time\"\234\002\n\017IntervalTrigger\022" +
                        "\027\n\nstart_time\030\001 \001(\003H\000\210\001\001\022\025\n\010end_time\030\002 \001" +
                        "(\003H\001\210\001\001\022\031\n\014repeat_limit\030\003 \001(\005H\002\210\001\001\022\031\n\014re" +
                        "peat_count\030\004 \001(\005H\003\210\001\001\022\033\n\016next_fire_time\030" +
                        "\006 \001(\003H\004\210\001\001\022\023\n\006period\030\007 \001(\003H\005\210\001\001\022\025\n\rcalen" +
                        "dar_name\030\010 \003(\tB\r\n\013_start_timeB\013\n\t_end_ti" +
                        "meB\017\n\r_repeat_limitB\017\n\r_repeat_countB\021\n\017" +
                        "_next_fire_timeB\t\n\007_period\"D\n\022PointInTim" +
                        "eTrigger\022\033\n\016next_fire_time\030\001 \001(\003H\000\210\001\001B\021\n" +
                        "\017_next_fire_time\"\351\001\n\033CompositeMaxDuratio" +
                        "nTrigger\022!\n\024maxDurationTimestamp\030\001 \001(\003H\000" +
                        "\210\001\001\022\035\n\020timerCurrentDate\030\002 \001(\003H\001\210\001\001\022I\n\014ti" +
                        "merTrigger\030\003 \001(\0132..org.kie.kogito.serial" +
                        "ization.protobuf.TriggerH\002\210\001\001B\027\n\025_maxDur" +
                        "ationTimestampB\023\n\021_timerCurrentDateB\017\n\r_" +
                        "timerTrigger\"c\n\005Tuple\022\021\n\thandle_id\030\001 \003(\003" +
                        "\022G\n\006object\030\002 \003(\01327.org.kie.kogito.serial" +
                        "ization.protobuf.SerializedObject\"b\n\020Ser" +
                        "ializedObject\022\033\n\016strategy_index\030\001 \001(\005H\000\210" +
                        "\001\001\022\023\n\006object\030\002 \001(\014H\001\210\001\001B\021\n\017_strategy_ind" +
                        "exB\t\n\007_objectB=\n%org.kie.kogito.serializ" +
                        "ation.protobufB\024KogitoTimersProtobufb\006pr" +
                        "oto3"
        };
        descriptor = com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[] {
                                com.google.protobuf.AnyProto.getDescriptor(),
                        });
        internal_static_org_kie_kogito_serialization_protobuf_Timers_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_org_kie_kogito_serialization_protobuf_Timers_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_Timers_descriptor,
                new java.lang.String[] { "Timer", "ProcTimer", "ProcTimer", });
        internal_static_org_kie_kogito_serialization_protobuf_Timer_descriptor =
                getDescriptor().getMessageTypes().get(1);
        internal_static_org_kie_kogito_serialization_protobuf_Timer_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_Timer_descriptor,
                new java.lang.String[] { "Type", "Data", });
        internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_descriptor =
                getDescriptor().getMessageTypes().get(2);
        internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_ExpireTimerData_descriptor,
                new java.lang.String[] { "HandleId", "EntryPointId", "ClassName", "NextFireTimestamp", "HandleId", "EntryPointId", "ClassName", "NextFireTimestamp", });
        internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_descriptor =
                getDescriptor().getMessageTypes().get(3);
        internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_TimerNodeTimerData_descriptor,
                new java.lang.String[] { "NodeId", "Tuple", "NodeId", "Tuple", });
        internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_descriptor =
                getDescriptor().getMessageTypes().get(4);
        internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_ProcessTimer_descriptor,
                new java.lang.String[] { "Id", "TimerId", "Delay", "Period", "ProcessInstanceId", "ActivatedTime", "LastTriggered", "RepeatLimit", "Trigger", "Id", "TimerId", "Delay", "Period",
                        "ProcessInstanceId", "ActivatedTime", "LastTriggered", "RepeatLimit", "Trigger", });
        internal_static_org_kie_kogito_serialization_protobuf_Trigger_descriptor =
                getDescriptor().getMessageTypes().get(5);
        internal_static_org_kie_kogito_serialization_protobuf_Trigger_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_Trigger_descriptor,
                new java.lang.String[] { "Type", "Trigger", });
        internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_descriptor =
                getDescriptor().getMessageTypes().get(6);
        internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_CronTrigger_descriptor,
                new java.lang.String[] { "StartTime", "EndTime", "RepeatLimit", "RepeatCount", "CronExpression", "NextFireTime", "CalendarName", "StartTime", "EndTime", "RepeatLimit", "RepeatCount",
                        "CronExpression", "NextFireTime", });
        internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_descriptor =
                getDescriptor().getMessageTypes().get(7);
        internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_IntervalTrigger_descriptor,
                new java.lang.String[] { "StartTime", "EndTime", "RepeatLimit", "RepeatCount", "NextFireTime", "Period", "CalendarName", "StartTime", "EndTime", "RepeatLimit", "RepeatCount",
                        "NextFireTime", "Period", });
        internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_descriptor =
                getDescriptor().getMessageTypes().get(8);
        internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_PointInTimeTrigger_descriptor,
                new java.lang.String[] { "NextFireTime", "NextFireTime", });
        internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_descriptor =
                getDescriptor().getMessageTypes().get(9);
        internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_CompositeMaxDurationTrigger_descriptor,
                new java.lang.String[] { "MaxDurationTimestamp", "TimerCurrentDate", "TimerTrigger", "MaxDurationTimestamp", "TimerCurrentDate", "TimerTrigger", });
        internal_static_org_kie_kogito_serialization_protobuf_Tuple_descriptor =
                getDescriptor().getMessageTypes().get(10);
        internal_static_org_kie_kogito_serialization_protobuf_Tuple_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_Tuple_descriptor,
                new java.lang.String[] { "HandleId", "Object", });
        internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_descriptor =
                getDescriptor().getMessageTypes().get(11);
        internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_org_kie_kogito_serialization_protobuf_SerializedObject_descriptor,
                new java.lang.String[] { "StrategyIndex", "Object", "StrategyIndex", "Object", });
        com.google.protobuf.AnyProto.getDescriptor();
    }

    // @@protoc_insertion_point(outer_class_scope)
}
