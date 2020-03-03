package org.kie.kogito.codegen.grafana;

import java.math.BigDecimal;
import java.util.HashMap;
public class SupportedDecisionTypes {
    private static final HashMap<Class, String> dmnInternalClassToDmnStandardMap;

    static {
        dmnInternalClassToDmnStandardMap = new HashMap<>();
        dmnInternalClassToDmnStandardMap.put(String.class, "string");
        dmnInternalClassToDmnStandardMap.put(BigDecimal.class, "number");
        dmnInternalClassToDmnStandardMap.put(Boolean.class, "boolean");
    }

    public static boolean isSupported(String type){
        return  dmnInternalClassToDmnStandardMap.values().stream().anyMatch(type::equals);
    }

    public static boolean isSupported(Class c){
        return  dmnInternalClassToDmnStandardMap.keySet().stream().anyMatch(c::equals);
    }

    public static String fromInternalToStandard(Class c){
        return dmnInternalClassToDmnStandardMap.get(c);
    }

}
