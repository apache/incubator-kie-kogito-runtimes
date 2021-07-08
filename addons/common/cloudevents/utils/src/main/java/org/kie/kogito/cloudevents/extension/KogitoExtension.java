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
package org.kie.kogito.cloudevents.extension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import io.cloudevents.CloudEventExtensions;
import io.cloudevents.Extension;
import io.cloudevents.core.extensions.impl.ExtensionUtils;
import io.cloudevents.core.provider.ExtensionProvider;

public class KogitoExtension implements Extension {

    public static final String KOGITO_EXECUTION_ID = "kogitoexecutionid";
    public static final String KOGITO_DMN_MODEL_NAME = "kogitodmnmodelname";
    public static final String KOGITO_DMN_MODEL_NAMESPACE = "kogitodmnmodelnamespace";
    public static final String KOGITO_DMN_EVALUATE_DECISION = "kogitodmnevaldecision";
    public static final String KOGITO_DMN_FULL_RESULT = "kogitodmnfullresult";
    public static final String KOGITO_DMN_FILTERED_CTX = "kogitodmnfilteredctx";

    private static final Set<String> KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            KOGITO_EXECUTION_ID,
            KOGITO_DMN_MODEL_NAME,
            KOGITO_DMN_MODEL_NAMESPACE,
            KOGITO_DMN_EVALUATE_DECISION,
            KOGITO_DMN_FULL_RESULT,
            KOGITO_DMN_FILTERED_CTX)));

    private String executionId;
    private String dmnModelName;
    private String dmnModelNamespace;
    private String dmnEvaluateDecision;
    private Boolean dmnFullResult;
    private Boolean dmnFilteredCtx;

    public static void register() {
        ExtensionProvider.getInstance().registerExtension(KogitoExtension.class, KogitoExtension::new);
    }

    @Override
    public void readFrom(CloudEventExtensions extensions) {
        readStringExtension(extensions, KOGITO_EXECUTION_ID, this::setExecutionId);
        readStringExtension(extensions, KOGITO_DMN_MODEL_NAME, this::setDmnModelName);
        readStringExtension(extensions, KOGITO_DMN_MODEL_NAMESPACE, this::setDmnModelNamespace);
        readStringExtension(extensions, KOGITO_DMN_EVALUATE_DECISION, this::setDmnEvaluateDecision);
        readBooleanExtension(extensions, KOGITO_DMN_FULL_RESULT, this::setDmnFullResult);
        readBooleanExtension(extensions, KOGITO_DMN_FILTERED_CTX, this::setDmnFilteredCtx);
    }

    private static void readStringExtension(CloudEventExtensions extensions, String key, Consumer<String> consumer) {
        Optional.ofNullable(extensions.getExtension(key))
                // there seems to be a bug in the cloudevents sdk so that, when a extension attributes is null,
                // it returns a "null" String instead of a real null object
                .filter(obj -> !("null".equals(obj)))
                .map(Object::toString)
                .ifPresent(consumer);
    }

    private static void readBooleanExtension(CloudEventExtensions extensions, String key, Consumer<Boolean> consumer) {
        Optional.ofNullable(extensions.getExtension(key))
                .filter(Boolean.class::isInstance)
                .map(Boolean.class::cast)
                .ifPresent(consumer);
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
            case KOGITO_DMN_FULL_RESULT:
                return isDmnFullResult();
            case KOGITO_DMN_FILTERED_CTX:
                return isDmnFilteredCtx();
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

    public Boolean isDmnFullResult() {
        return dmnFullResult;
    }

    public void setDmnFullResult(Boolean dmnFullResult) {
        this.dmnFullResult = dmnFullResult;
    }

    public Boolean isDmnFilteredCtx() {
        return dmnFilteredCtx;
    }

    public void setDmnFilteredCtx(Boolean dmnFilteredCtx) {
        this.dmnFilteredCtx = dmnFilteredCtx;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KogitoExtension that = (KogitoExtension) o;
        return Objects.equals(executionId, that.executionId) &&
                Objects.equals(dmnModelName, that.dmnModelName) &&
                Objects.equals(dmnModelNamespace, that.dmnModelNamespace) &&
                Objects.equals(dmnEvaluateDecision, that.dmnEvaluateDecision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, dmnModelName, dmnModelNamespace, dmnEvaluateDecision);
    }

    @Override
    public String toString() {
        return "KogitoExtension{" +
                "executionId='" + executionId + '\'' +
                ", dmnModelName='" + dmnModelName + '\'' +
                ", dmnModelNamespace='" + dmnModelNamespace + '\'' +
                ", dmnEvaluateDecision='" + dmnEvaluateDecision + '\'' +
                ", dmnFullResult=" + dmnFullResult +
                ", dmnFilteredCtx=" + dmnFilteredCtx +
                '}';
    }
}
