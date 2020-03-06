package org.kie.kogito.codegen.grafana.model.functions;

import java.util.Map;
import java.util.stream.Collectors;

public class ExprBuilder {
    private ExprBuilder(){}

    public static String apply(String expr, Map<Integer, GrafanaFunction> functions){
        for(Integer key : functions.keySet().stream().sorted().collect(Collectors.toList())){
            GrafanaFunction function = functions.get(key);
            if (function.hasTimeParameter()){
                expr = String.format("%s[%s]", expr, function.getTimeParameter());
            }
            expr = String.format("%s(%s)", function.getFunction(), expr);
        }

        return expr;
    }
}
