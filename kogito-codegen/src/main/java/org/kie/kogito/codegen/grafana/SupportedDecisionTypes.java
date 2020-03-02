package org.kie.kogito.codegen.grafana;

import java.util.Arrays;

public class SupportedDecisionTypes {
    private static final String[] supportedTypes = new String[]{"string", "integer", "double", "boolean"};

    private static final Class[] supportedClasses = new Class[]{String.class, Integer.class, Double.class, Boolean.class};

    public static boolean isSupported(String type){
        return  Arrays.stream(supportedTypes).anyMatch(type::equals);
    }

    public static boolean isSupported(Class c){
        return  Arrays.stream(supportedClasses).anyMatch(c::equals);
    }
}
