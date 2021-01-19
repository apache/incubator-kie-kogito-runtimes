/*
 *  Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.cloudevents.extension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.cloudevents.CloudEventExtensions;
import io.cloudevents.Extension;
import io.cloudevents.core.extensions.impl.ExtensionUtils;

public class KogitoExtension implements Extension {

    public static final String KOGITO_EXECUTION_ID = "kogitoexecutionid";
    public static final String KOGITO_DMN_MODEL_NAME = "kogitodmnmodelname";
    public static final String KOGITO_DMN_MODEL_NAMESPACE = "kogitodmnmodelnamespace";
    public static final String KOGITO_DMN_EVALUATE_DECISION = "kogitodmnevaldecision";

    private static final Set<String> KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            KOGITO_EXECUTION_ID,
            KOGITO_DMN_MODEL_NAME,
            KOGITO_DMN_MODEL_NAMESPACE,
            KOGITO_DMN_EVALUATE_DECISION
    )));

    private String executionId;
    private String dmnModelName;
    private String dmnModelNamespace;
    private String dmnEvaluateDecision;

    @Override
    public void readFrom(CloudEventExtensions extensions) {
        Optional.ofNullable(extensions.getExtension(KOGITO_EXECUTION_ID))
                .map(Object::toString).ifPresent(this::setExecutionId);
        Optional.ofNullable(extensions.getExtension(KOGITO_DMN_MODEL_NAME))
                .map(Object::toString).ifPresent(this::setDmnModelName);
        Optional.ofNullable(extensions.getExtension(KOGITO_DMN_MODEL_NAMESPACE))
                .map(Object::toString).ifPresent(this::setDmnModelNamespace);
        Optional.ofNullable(extensions.getExtension(KOGITO_DMN_EVALUATE_DECISION))
                .map(Object::toString).ifPresent(this::setDmnEvaluateDecision);
    }

    @Override
    public Object getValue(String key) throws IllegalArgumentException {
        switch (key) {
            case KOGITO_EXECUTION_ID:
                return getExecutionId();
            case KOGITO_DMN_MODEL_NAME:
                return getDmnModelName();
            case KOGITO_DMN_MODEL_NAMESPACE:
                return getDmnModelNamespace();
            case KOGITO_DMN_EVALUATE_DECISION:
                return getDmnEvaluateDecision();
        }
        throw ExtensionUtils.generateInvalidKeyException(this.getClass().getSimpleName(), key);
    }

    @Override
    public Set<String> getKeys() {
        return KEYS;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getDmnModelName() {
        return dmnModelName;
    }

    public void setDmnModelName(String dmnModelName) {
        this.dmnModelName = dmnModelName;
    }

    public String getDmnModelNamespace() {
        return dmnModelNamespace;
    }

    public void setDmnModelNamespace(String dmnModelNamespace) {
        this.dmnModelNamespace = dmnModelNamespace;
    }

    public String getDmnEvaluateDecision() {
        return dmnEvaluateDecision;
    }

    public void setDmnEvaluateDecision(String dmnEvaluateDecision) {
        this.dmnEvaluateDecision = dmnEvaluateDecision;
    }
}
