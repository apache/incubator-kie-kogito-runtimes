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
package org.kie.kogito.jackson.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CommonObjectModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public CommonObjectModule() {
        this.addDeserializer(URI.class, new JsonDeserializer<URI>() {
            @Override
            public URI deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return URI.create(fromNode(p));
            }
        });

        this.addDeserializer(File.class, new JsonDeserializer<File>() {
            @Override
            public File deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new File(fromNode(p));
            }
        });
    }

    private static String fromNode(JsonParser p) throws IOException {
        JsonNode node = p.readValueAsTree();
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isObject()) {
            return ((ObjectNode) node).iterator().next().asText();
        } else if (node.isArray()) {
            return node.get(0).asText();
        } else {
            throw new IOException("Cannot extract string from node " + node);
        }
    }
}
