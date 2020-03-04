package org.kie.kogito.codegen.grafana.types;

import java.util.HashMap;

import com.redhat.developer.model.functions.GrafanaFunction;
import com.redhat.developer.model.functions.IncreaseFunction;

public class BooleanType extends AbstractDmnType {
    public BooleanType() {
        super(Boolean.class, "boolean");
        HashMap<Integer, GrafanaFunction> grafanaFunctionsToApply = new HashMap<>();
        grafanaFunctionsToApply.put(1, new IncreaseFunction("10m"));
        addFunctions(grafanaFunctionsToApply);
    }
}
