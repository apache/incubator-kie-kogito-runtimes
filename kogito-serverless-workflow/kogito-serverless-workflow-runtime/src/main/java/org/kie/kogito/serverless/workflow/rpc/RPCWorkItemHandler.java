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
package org.kie.kogito.serverless.workflow.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.jackson.utils.JsonObjectUtils;
import org.kie.kogito.serverless.workflow.WorkflowWorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.DynamicMessage.Builder;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;

public abstract class RPCWorkItemHandler extends WorkflowWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(RPCWorkItemHandler.class);

    public final static String NAME = "gRPC";
    public final static String DESCRIPTOR_PATH = "protobuf/descriptor-sets/output.protobin";

    public final static String SERVICE_PROP = "serviceName";
    public final static String FILE_PROP = "fileName";
    public final static String METHOD_PROP = "methodName";

    private FileDescriptorSet fdSet;

    public RPCWorkItemHandler() {
        fdSet = loadFileDescriptorSet().orElseThrow(() -> new IllegalStateException("Cannot initialize RPC workitem handler. Missing descriptor file " + DESCRIPTOR_PATH));
    }

    @Override
    protected Object internalExecute(KogitoWorkItem workItem, Map<String, Object> parameters) {
        Map<String, Object> metadata = workItem.getNodeInstance().getNode().getMetaData();
        String file = (String) metadata.get(FILE_PROP);
        String service = (String) metadata.get(SERVICE_PROP);
        String method = (String) metadata.get(METHOD_PROP);
        return getObject(doCall(fdSet, parameters, getChannel(file, service), file, service, method));
    }

    protected abstract Channel getChannel(String file, String service);

    public static Optional<FileDescriptorSet> loadFileDescriptorSet() {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(DESCRIPTOR_PATH)) {
            return inputStream != null ? Optional.of(FileDescriptorSet.newBuilder().mergeFrom(inputStream.readAllBytes()).build()) : Optional.empty();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize RPC workitem handler. Failure loading descriptor file", e);
        }
    }

    public static DynamicMessage doCall(FileDescriptorSet fdSet, Map<String, Object> parameters, Channel channel, String fileName, String serviceName, String methodName) {
        try {
            FileDescriptor descriptor = FileDescriptor.buildFrom(fdSet.getFileList().stream().filter(f -> f.getName().equals(fileName))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Cannot find file name " + fileName)), new FileDescriptor[0], true);
            ServiceDescriptor serviceDesc = Objects.requireNonNull(descriptor.findServiceByName(serviceName), "Cannot find service name " + serviceName);
            MethodDescriptor methodDesc = Objects.requireNonNull(serviceDesc.findMethodByName(methodName), "Cannot find method name " + methodName);
            DynamicMessage message = buildMessage(methodDesc, parameters);
            ClientCall<DynamicMessage, DynamicMessage> call = channel.newCall(io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage> newBuilder()
                    .setType(getMethodType(methodDesc))
                    .setFullMethodName(io.grpc.MethodDescriptor.generateFullMethodName(
                            serviceDesc.getFullName(), methodDesc.getName()))
                    .setRequestMarshaller(ProtoUtils.marshaller(
                            DynamicMessage.newBuilder(methodDesc.getInputType()).buildPartial()))
                    .setResponseMarshaller(ProtoUtils.marshaller(
                            DynamicMessage.newBuilder(methodDesc.getOutputType()).buildPartial()))
                    .build(), CallOptions.DEFAULT.withWaitForReady());
            return ClientCalls.blockingUnaryCall(call, message);

        } catch (DescriptorValidationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static DynamicMessage buildMessage(MethodDescriptor methodDesc, Map<String, Object> parameters) {
        Descriptor descriptor = methodDesc.getInputType();
        Builder builder = DynamicMessage.newBuilder(descriptor);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                FieldDescriptor fieldDescriptor = descriptor.findFieldByName(entry.getKey());
                if (fieldDescriptor != null) {
                    builder.setField(fieldDescriptor, entry.getValue());
                } else {
                    logger.info("Unrecognized parameter " + entry.getKey());
                }
            }
        }
        return builder.build();
    }

    public static JsonNode getObject(DynamicMessage message) {
        return JsonObjectUtils.fromValue(message.getAllFields().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getJsonName(), Map.Entry::getValue)));
    }

    private static MethodType getMethodType(MethodDescriptor methodDesc) {
        MethodDescriptorProto methodDescProto = methodDesc.toProto();
        if (methodDescProto.getClientStreaming()) {
            return MethodType.CLIENT_STREAMING;
        } else if (methodDescProto.getServerStreaming()) {
            return MethodType.SERVER_STREAMING;
        } else {
            return MethodType.UNKNOWN;
        }
    }
}
