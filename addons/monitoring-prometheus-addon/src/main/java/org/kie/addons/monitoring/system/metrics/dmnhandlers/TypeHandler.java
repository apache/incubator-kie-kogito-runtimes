package org.kie.addons.monitoring.system.metrics.dmnhandlers;

public interface TypeHandler<T> {
    void record(String handler, T sample);
}
