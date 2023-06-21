/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow.fluent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.kie.kogito.serverless.workflow.SWFConstants;
import org.kie.kogito.serverless.workflow.actions.WorkflowLogLevel;
import org.kie.kogito.serverless.workflow.functions.FunctionDefinitionEx;
import org.kie.kogito.serverless.workflow.parser.types.ServiceTypeHandler;

import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.functions.FunctionDefinition.Type;

import static org.kie.kogito.serverless.workflow.parser.FunctionTypeHandlerFactory.CUSTOM_TYPE_SEPARATOR;
import static org.kie.kogito.serverless.workflow.parser.types.SysOutTypeHandler.SYSOUT_TYPE;

public class FunctionBuilder {

    private FunctionDefinition functionDefinition;

    @SuppressWarnings("squid:S115")
    public enum HttpMethod {
        post,
        get,
        put,
        patch,
        delete,
        head,
        trace,
        options,
        connect
    }

    public static FunctionBuilder def(String name, FunctionDefinition.Type type, String operation) {
        return new FunctionBuilder(new FunctionDefinition(name).withType(type).withOperation(operation));
    }

    public static FunctionBuilder rest(String name, HttpMethod method, String uri) {
        return new FunctionBuilder(new FunctionDefinition(name).withType(Type.CUSTOM).withOperation("rest:" + method + CUSTOM_TYPE_SEPARATOR + uri));
    }

    public static FunctionBuilder expr(String name, String expression) {
        return new FunctionBuilder(new FunctionDefinition(name).withType(Type.EXPRESSION).withOperation(expression));
    }

    public static <T, V> FunctionBuilder java(String funcName, Function<T, V> function) {
        return new FunctionBuilder(new FunctionDefinitionEx<T, V>(funcName).withFunction(function).withType(Type.CUSTOM).withOperation("java"));
    }

    public static FunctionBuilder java(String funcName, String className, String methodName) {
        return service(funcName, SWFConstants.JAVA, className, methodName);
    }

    public static FunctionBuilder python(String funcName, String moduleName, String methodName) {
        return service(funcName, SWFConstants.PYTHON, moduleName, methodName);
    }

    private static FunctionBuilder service(String name, String langName, String moduleName, String methodName) {
        return new FunctionBuilder(new FunctionDefinition(name).withType(Type.CUSTOM)
                .withOperation(ServiceTypeHandler.SERVICE_TYPE + CUSTOM_TYPE_SEPARATOR + langName + CUSTOM_TYPE_SEPARATOR + moduleName + ServiceTypeHandler.INTFC_SEPARATOR + methodName));
    }

    public static FunctionBuilder log(String name, WorkflowLogLevel level) {
        return new FunctionBuilder(new FunctionDefinition(name).withType(Type.CUSTOM).withOperation(SYSOUT_TYPE + CUSTOM_TYPE_SEPARATOR + level));
    }

    private FunctionBuilder(FunctionDefinition functionDefinition) {
        this.functionDefinition = functionDefinition;
    }

    public FunctionBuilder metadata(String key, String value) {
        Map<String, String> metadata = functionDefinition.getMetadata();

        if (metadata == null) {
            metadata = new HashMap<>();
            functionDefinition.withMetadata(metadata);
        }
        metadata.put(key, value);
        return this;
    }

    public FunctionDefinition build() {
        return functionDefinition;
    }

    String getName() {
        return functionDefinition.getName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionDefinition.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FunctionBuilder other = (FunctionBuilder) obj;
        return Objects.equals(functionDefinition.getName(), other.functionDefinition.getName());
    }
}
