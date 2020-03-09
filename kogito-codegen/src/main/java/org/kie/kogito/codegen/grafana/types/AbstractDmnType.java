package org.kie.kogito.codegen.grafana.types;

import java.util.Map;

import org.kie.kogito.codegen.grafana.model.functions.GrafanaFunction;

public class AbstractDmnType {

    private final Class internalRepresentationClass;

    private String dmnType;

    private Map<Integer, GrafanaFunction> grafanaFunctionsToApply;

    public AbstractDmnType(Class internalRepresentationClass, String dmnType) {
        this.internalRepresentationClass = internalRepresentationClass;
        this.dmnType = dmnType;
    }

    protected void addFunctions(Map<Integer, GrafanaFunction> grafanaFunctionsToApply) {
        this.grafanaFunctionsToApply = grafanaFunctionsToApply;
    }

    public Class getInternalClass() {
        return internalRepresentationClass;
    }

    public Map<Integer, GrafanaFunction> getGrafanaFunctions() {
        return grafanaFunctionsToApply;
    }

    public String getDmnType() {
        return dmnType;
    }
}
