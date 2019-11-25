/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.index.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class ProcessInstance extends ProcessInstanceMeta {

    private JsonNode variables;
    @JsonProperty("nodeInstances")
    private List<NodeInstance> nodes;
    private Set<String> addons;

    private ProcessInstanceError error;

    public JsonNode getVariables() {
        return variables;
    }

    public void setVariables(JsonNode variables) {
        this.variables = variables;
    }

    public List<NodeInstance> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeInstance> nodes) {
        this.nodes = nodes;
    }

    public ProcessInstanceError getError() {
        return error;
    }

    public void setError(ProcessInstanceError error) {
        this.error = error;
    }

    public Set<String> getAddons() {
        return addons;
    }

    public void setAddons(Set<String> addons) {
        this.addons = addons;
    }

    @Override
    public String toString() {
        return "ProcessInstance{" +
                "variables=" + variables +
                ", nodes=" + nodes +
                ", addons=" + addons +
                ", error=" + error +
                "} " + super.toString();
    }
}
