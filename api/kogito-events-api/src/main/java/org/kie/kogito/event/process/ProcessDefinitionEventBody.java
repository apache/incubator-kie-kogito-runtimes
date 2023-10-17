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

import java.util.List;
import java.util.Set;

public class ProcessDefinitionEventBody {

    private String id;
    private String name;
    private String version;
    private String type;
    private Set<String> roles;
    private Set<String> addons;
    private String endpoint;
    private String source;
    private List<NodeDefinition> nodes;

    public ProcessDefinitionEventBody() {
    }

    public ProcessDefinitionEventBody(String id, String name, String version, String type, Set<String> roles, Set<String> addons, String endpoint, String source, List<NodeDefinition> nodes) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.type = type;
        this.roles = roles;
        this.addons = addons;
        this.endpoint = endpoint;
        this.source = source;
        this.nodes = nodes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getAddons() {
        return addons;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getSource() {
        return source;
    }

    public List<NodeDefinition> getNodes() {
        return nodes;
    }

    public static ProcessDefinitionEventBodyBuilder builder() {
        return new ProcessDefinitionEventBodyBuilder();
    }

    public static class ProcessDefinitionEventBodyBuilder {
        private String id;
        private String name;
        private String version;
        private String type;
        private Set<String> roles;
        private Set<String> addons;
        private String endpoint;
        private String source;
        private List<NodeDefinition> nodes;

        public ProcessDefinitionEventBodyBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public ProcessDefinitionEventBodyBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ProcessDefinitionEventBodyBuilder setVersion(String version) {
            this.version = version;
            return this;
        }

        public ProcessDefinitionEventBodyBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public ProcessDefinitionEventBodyBuilder setRoles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public ProcessDefinitionEventBodyBuilder setAddons(Set<String> addons) {
            this.addons = addons;
            return this;
        }

        public ProcessDefinitionEventBodyBuilder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public ProcessDefinitionEventBodyBuilder setSource(String source) {
            this.source = source;
            return this;
        }

        public ProcessDefinitionEventBodyBuilder setNodes(List<NodeDefinition> nodes) {
            this.nodes = nodes;
            return this;
        }

        public ProcessDefinitionEventBody build() {
            return new ProcessDefinitionEventBody(id, name, version, type, roles, addons, endpoint, source, nodes);
        }
    }
}
