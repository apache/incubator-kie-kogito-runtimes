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

package org.kie.kogito.codegen.grafana;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.kie.kogito.codegen.grafana.model.functions.GrafanaFunction;
import org.kie.kogito.codegen.grafana.types.AbstractDmnType;
import org.kie.kogito.codegen.grafana.types.BooleanType;
import org.kie.kogito.codegen.grafana.types.NumberType;
import org.kie.kogito.codegen.grafana.types.StringType;

public class SupportedDecisionTypes {

    private static final Set<AbstractDmnType> supportedDmnTypes = new HashSet<>();

    private static final HashMap<Class, String> dmnInternalClassToDmnStandardMap;

    static {
        supportedDmnTypes.add(new BooleanType());
        supportedDmnTypes.add(new NumberType());
        supportedDmnTypes.add(new StringType());

        dmnInternalClassToDmnStandardMap = new HashMap<>();
        supportedDmnTypes.stream().forEach(x -> dmnInternalClassToDmnStandardMap.put(x.getInternalClass(), x.getDmnType()));
    }

    private SupportedDecisionTypes() {
    }

    public static boolean isSupported(String type) {
        return dmnInternalClassToDmnStandardMap.containsValue(type);
    }

    public static boolean isSupported(Class c) {
        return dmnInternalClassToDmnStandardMap.containsKey(c);
    }

    public static String fromInternalToStandard(Class c) {
        return dmnInternalClassToDmnStandardMap.get(c);
    }

    public static Map<Integer, GrafanaFunction> getGrafanaFunction(String dmnType) {
        if (isSupported(dmnType)) {
            Optional<AbstractDmnType> type = supportedDmnTypes.stream().filter(x -> x.getDmnType().equalsIgnoreCase(dmnType)).findFirst();
            if (type.isPresent()) {
                return type.get().getGrafanaFunctions();
            }
        }
        return new HashMap<>();
    }

    public static Collection<String> getSupportedDMNTypes() {
        return dmnInternalClassToDmnStandardMap.values();
    }
}
