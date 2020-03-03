package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import io.prometheus.client.Counter;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;

public class StringHandler implements TypeHandler<String>{

    private final Counter counter;

    public StringHandler(String prefix){
        this.counter = initializeCounter(prefix);
    }

    private Counter initializeCounter(String prefix){
        Counter counter = Counter.build().name(prefix + MetricsConstants.DECISIONS_NAME_SUFFIX)
                .help(MetricsConstants.DECISIONS_HELP)
                .labelNames(MetricsConstants.HANDLER_IDENTIFIER_LABELS).register();
        return counter;
    }

    @Override
    public void record(String handler, String sample) {
        counter.labels(handler, sample).inc();
    }
}
