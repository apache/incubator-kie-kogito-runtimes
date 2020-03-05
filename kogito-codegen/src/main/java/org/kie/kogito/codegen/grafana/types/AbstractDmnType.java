package org.kie.kogito.codegen.grafana.types;

import java.util.HashMap;

import org.kie.kogito.codegen.grafana.model.functions.GrafanaFunction;

public class AbstractDmnType {
    private final Class internalRepresentationClass;

    private String dmnType;

    private HashMap<Integer, GrafanaFunction> grafanaFunctionsToApply;

    public AbstractDmnType(Class internalRepresentationClass, String dmnType) {
        this.internalRepresentationClass = internalRepresentationClass;
        this.dmnType = dmnType;
    }

    protected void addFunctions( HashMap<Integer, GrafanaFunction> grafanaFunctionsToApply) {
        this.grafanaFunctionsToApply = grafanaFunctionsToApply;
    }

    public Class getInternalClass(){
        return internalRepresentationClass;
    }

    public HashMap<Integer, GrafanaFunction> getGrafanaFunctionsToApply(){
        return grafanaFunctionsToApply;
    }

    public String getDmnType(){
        return dmnType;
    }

}
