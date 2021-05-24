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
package org.jbpm.ruleflow.core.factory;

import org.jbpm.process.core.context.variable.Mappable;

public interface MappableNodeFactory<T extends NodeFactory<T, ?>> {

    String METHOD_IN_MAPPING = "inMapping";
    String METHOD_OUT_MAPPING = "outMapping";

    Mappable getMappableNode();

    default T inMapping(String parameterName, String variableName) {
        getMappableNode().addInMapping(parameterName, variableName);
        return (T) this;
    }

    default T outMapping(String parameterName, String variableName) {
        getMappableNode().addOutMapping(parameterName, variableName);
        return (T) this;
    }
}
