package org.kie.addons.monitoring.system.metrics.dmnhandlers;

public interface TypeHandler<T> {
    void record(String handler, T sample);

    default String getClassName(Class c){
        return c.getCanonicalName().replace(".", "_");
    }
}
