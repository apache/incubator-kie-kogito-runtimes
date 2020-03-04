package org.kie.kogito.codegen.grafana.types;

import java.util.HashMap;

import com.redhat.developer.model.functions.GrafanaFunction;
import com.redhat.developer.model.functions.IncreaseFunction;

public class StringType extends AbstractDmnType {
    public StringType() {
        super(String.class, "string");
        HashMap<Integer, GrafanaFunction> grafanaFunctionsToApply = new HashMap<>();
        grafanaFunctionsToApply.put(1, new IncreaseFunction("10m"));
        addFunctions(grafanaFunctionsToApply);
    }
}
