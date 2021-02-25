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
package org.kie.kogito.codegen.openapi.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents an OpenApi Operation
 */
public class OpenApiClientOperation {

    private String operationId;
    private String api;
    private List<String> parameters = new ArrayList<>();
    private String generatedClass;
    private String methodName;

    public OpenApiClientOperation(final String operationId) {
        this.operationId = operationId;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public List<String> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    public void setParameters(List<String> parameters) {
        if (parameters == null) {
            this.parameters = Collections.emptyList();
        }
        this.parameters = parameters;
    }

    public String getGeneratedClass() {
        return generatedClass;
    }

    public void setGeneratedClass(String generatedClass) {
        this.generatedClass = generatedClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenApiClientOperation that = (OpenApiClientOperation) o;
        return Objects.equals(operationId, that.operationId) && Objects.equals(api, that.api) && Objects.equals(parameters, that.parameters) && Objects.equals(generatedClass, that.generatedClass)
                && Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId, api, parameters, generatedClass, methodName);
    }

    @Override
    public String toString() {
        return "OpenApiClientOperation{" +
                "operationId='" + operationId + '\'' +
                ", api='" + api + '\'' +
                ", generatedClass='" + generatedClass + '\'' +
                '}';
    }
}
