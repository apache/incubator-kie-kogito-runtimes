package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import io.prometheus.client.Counter;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;

public class BooleanHandler implements TypeHandler<Boolean>{

    private final Counter counter;

    public BooleanHandler(Class c){
        this.counter = initializeCounter(getClassName(c));
    }

    private Counter initializeCounter(String className){
        Counter counter = Counter.build().name(MetricsConstants.DECISIONS_NAME + className)
                                                .help(MetricsConstants.DECISIONS_HELP)
                                                .labelNames(MetricsConstants.HANDLER_IDENTIFIER_LABELS).register();
        return counter;
    }

    @Override
    public void record(String handler, Boolean sample) {
        counter.labels(handler, sample.toString()).inc();
    }
}
