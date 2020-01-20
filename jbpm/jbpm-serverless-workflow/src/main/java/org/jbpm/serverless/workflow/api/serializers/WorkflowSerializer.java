/*
 *
 *   Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.jbpm.serverless.workflow.api.serializers;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.jbpm.serverless.workflow.api.functions.Function;
import org.jbpm.serverless.workflow.api.interfaces.Extension;
import org.jbpm.serverless.workflow.api.interfaces.State;
import org.jbpm.serverless.workflow.api.Workflow;
import org.jbpm.serverless.workflow.api.events.TriggerEvent;

public class WorkflowSerializer extends StdSerializer<Workflow> {

    public WorkflowSerializer() {
        this(Workflow.class);
    }

    protected WorkflowSerializer(Class<Workflow> t) {
        super(t);
    }

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    public void serialize(Workflow workflow,
                          JsonGenerator gen,
                          SerializerProvider provider) throws IOException {

        gen.writeStartObject();

        gen.writeStringField("name",
                             workflow.getName());

        if(workflow.getId() !=null && !workflow.getId().isEmpty()) {
            gen.writeStringField("id",
                                 workflow.getId());
        } else {
            gen.writeStringField("id",
                                 generateUniqueId());
        }

        if(workflow.getDescription() != null && !workflow.getDescription().isEmpty()) {
            gen.writeStringField("description",
                                 workflow.getDescription());
        }

        if(workflow.getVersion() != null && !workflow.getVersion().isEmpty()) {
            gen.writeStringField("version",
                                 workflow.getVersion());
        }

        if(workflow.getSchemaVersion() != null && !workflow.getSchemaVersion().isEmpty()) {
            gen.writeStringField("schemaVersion",
                                 workflow.getSchemaVersion());
        }

        if(workflow.getOwner() != null && !workflow.getOwner().isEmpty()) {
            gen.writeStringField("owner",
                                 workflow.getOwner());
        }

        if (workflow.getStartsAt() != null && !workflow.getStartsAt().isEmpty()) {
            gen.writeObjectField("startsAt",
                                 workflow.getStartsAt());
        } else {
            gen.writeObjectField("startsAt",
                                 "");
        }

        if (workflow.getExecStatus() != null) {
            gen.writeObjectField("execStatus",
                                 workflow.getExecStatus().value());
        }

        if (workflow.getMetadata() != null && !workflow.getMetadata().isEmpty()) {
            gen.writeObjectField("metadata",
                                 workflow.getMetadata());
        }

        if (workflow.getExpressionLanguage() != null && !workflow.getExpressionLanguage().isEmpty()) {
            gen.writeObjectField("expressionLanguage",
                                 workflow.getExpressionLanguage());
        }

        if (workflow.getTriggers() != null && !workflow.getTriggers().isEmpty()) {
            gen.writeArrayFieldStart("triggers");
            for (TriggerEvent triggerEvent : workflow.getTriggers()) {
                gen.writeObject(triggerEvent);
            }
            gen.writeEndArray();
        }

        if (workflow.getFunctions() != null && !workflow.getFunctions().isEmpty()) {
            gen.writeArrayFieldStart("functions");
            for (Function function : workflow.getFunctions()) {
                gen.writeObject(function);
            }
            gen.writeEndArray();
        }

        if (workflow.getStates() != null && !workflow.getStates().isEmpty()) {
            gen.writeArrayFieldStart("states");
            for (State state : workflow.getStates()) {
                gen.writeObject(state);
            }
            gen.writeEndArray();
        } else {
            gen.writeArrayFieldStart("states");
            gen.writeEndArray();
        }

        if(workflow.getExtensions() != null && !workflow.getExtensions().isEmpty()) {
            gen.writeArrayFieldStart("extensions");
            for (Extension extension : workflow.getExtensions()) {
                gen.writeObject(extension);
            }
            gen.writeEndArray();
        }

        gen.writeEndObject();
    }

    public static String generateUniqueId() {
        try {
            MessageDigest salt = MessageDigest.getInstance("SHA-256");

            salt.update(UUID.randomUUID()
                                .toString()
                                .getBytes("UTF-8"));
            String digest = bytesToHex(salt.digest());
            return digest;
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}