package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import io.prometheus.client.Counter;
import io.prometheus.client.SimpleCollector;

public interface TypeHandler<T> {
    void record(String handler, T sample);

    String getDmnType();
}
