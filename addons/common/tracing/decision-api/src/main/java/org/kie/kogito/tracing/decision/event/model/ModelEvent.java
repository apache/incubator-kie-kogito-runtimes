/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.tracing.decision.event.model;

import org.kie.kogito.KogitoGAV;
import org.kie.kogito.decision.DecisionModelMetadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelEvent {

    @JsonProperty("gav")
    private KogitoGAV gav;

    @JsonProperty("name")
    private String name;

    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("decisionModelMetadata")
    private DecisionModelMetadata decisionModelMetadata;

    @JsonProperty("definition")
    private String definition;

    public ModelEvent() {
    }

    public ModelEvent(KogitoGAV gav,
            String name,
            String namespace,
            DecisionModelMetadata decisionModelMetadata,
            String definition) {
        this.gav = gav;
        this.name = name;
        this.namespace = namespace;
        this.decisionModelMetadata = decisionModelMetadata;
        this.definition = definition;
    }

    public KogitoGAV getGav() {
        return gav;
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public DecisionModelMetadata getDecisionModelMetadata() {
        return decisionModelMetadata;
    }

    public String getDefinition() {
        return definition;
    }
}
