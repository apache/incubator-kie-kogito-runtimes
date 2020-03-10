/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.codegen.grafana.types;

import java.util.HashMap;

import org.kie.kogito.codegen.grafana.model.functions.GrafanaFunction;
import org.kie.kogito.codegen.grafana.model.functions.IncreaseFunction;

public class BooleanType extends AbstractDmnType {

    public BooleanType() {
        super(Boolean.class, "boolean");
        HashMap<Integer, GrafanaFunction> grafanaFunctionsToApply = new HashMap<>();
        grafanaFunctionsToApply.put(1, new IncreaseFunction("10m"));
        addFunctions(grafanaFunctionsToApply);
    }
}
