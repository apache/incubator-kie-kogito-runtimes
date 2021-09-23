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
package org.kie.kogito.tracing.decision.event.model;

import org.kie.kogito.KogitoGAV;
import org.kie.kogito.decision.DecisionModelMetadata;
import org.kie.kogito.tracing.event.model.ModelEvent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("org.kie.kogito.tracing.decision.event.model.DecisionModelEvent") // Needed for inheritance
public class DecisionModelEvent extends ModelEvent {

    private final String namespace;

    private final DecisionModelMetadata decisionModelMetadata;

    private final String definition;

    @JsonCreator
    public DecisionModelEvent(final @JsonProperty("gav") KogitoGAV gav,
            final @JsonProperty("name") String name,
            final @JsonProperty("namespace") String namespace,
            final @JsonProperty("decisionModelMetadata") DecisionModelMetadata decisionModelMetadata,
            final @JsonProperty("definition") String definition) {
        super(gav, name);
        this.namespace = namespace;
        this.decisionModelMetadata = decisionModelMetadata;
        this.definition = definition;
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
