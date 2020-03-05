package org.kie.kogito.codegen.grafana;

import java.util.HashMap;
import java.util.HashSet;
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

    public static boolean isSupported(String type){
        return  dmnInternalClassToDmnStandardMap.containsValue(type);
    }

    public static boolean isSupported(Class c){
        return dmnInternalClassToDmnStandardMap.containsKey(c);
    }

    public static String fromInternalToStandard(Class c){
        return dmnInternalClassToDmnStandardMap.get(c);
    }

    public static HashMap<Integer, GrafanaFunction> getGrafanaFunction(String dmnType) {
        if (isSupported(dmnType)) {
            return supportedDmnTypes.stream().filter(x -> x.getDmnType().equalsIgnoreCase(dmnType)).findFirst().get().getGrafanaFunctionsToApply();
        }
        return new HashMap<>();
    }
}
