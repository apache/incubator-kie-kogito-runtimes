package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.SimpleCollector;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;

public class BooleanHandler implements TypeHandler<Boolean>{

    private final Counter counter;

    private String dmnType;

    public BooleanHandler(String dmnType, CollectorRegistry registry){
        this.dmnType = dmnType;
        this.counter = initializeCounter(dmnType, registry);
    }

    public BooleanHandler(String dmnType) {
        this(dmnType, null);
    }

    @Override
    public void record(String handler, Boolean sample) {
        counter.labels(handler, sample.toString()).inc();
    }

    @Override
    public String getDmnType() {
        return dmnType;
    }

    private Counter initializeCounter(String dmnType, CollectorRegistry registry){
        Counter.Builder builder = Counter.build().name(dmnType + MetricsConstants.DECISIONS_NAME_SUFFIX)
                                                .help(MetricsConstants.DECISIONS_HELP)
                                                .labelNames(MetricsConstants.HANDLER_IDENTIFIER_LABELS);

        Counter counter = registry == null ? builder.register(CollectorRegistry.defaultRegistry) : builder.register(registry);

        return counter;
    }
}
