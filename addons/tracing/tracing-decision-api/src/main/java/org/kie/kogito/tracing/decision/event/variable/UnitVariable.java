/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.tracing.decision.event.variable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class UnitVariable extends TypedVariable {

    @JsonInclude(NON_NULL)
    private String baseType;

    private JsonNode value;

    private UnitVariable() {
    }

    public UnitVariable(String type) {
        this(type, null, null);
    }

    public UnitVariable(String type, JsonNode value) {
        this(type, null, value);
    }

    public UnitVariable(String type, String baseType, JsonNode value) {
        super(Kind.UNIT, type);
        this.baseType = baseType;
        this.value = value;
    }

    public String getBaseType() {
        return baseType;
    }

    public JsonNode getValue() {
        return value;
    }
}
