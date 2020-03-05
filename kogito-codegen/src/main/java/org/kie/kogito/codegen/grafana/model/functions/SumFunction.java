package org.kie.kogito.codegen.grafana.model.functions;

public class SumFunction implements GrafanaFunction {

    private final String function = "sum";

    public SumFunction(){}

    @Override
    public String getFunction() {
        return function;
    }

    @Override
    public boolean hasTimeParameter() {
        return false;
    }

    @Override
    public String getTimeParameter() {
        return null;
    }

}
