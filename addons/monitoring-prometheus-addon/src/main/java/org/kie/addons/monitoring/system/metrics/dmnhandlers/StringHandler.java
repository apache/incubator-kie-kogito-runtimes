package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;

public class StringHandler implements TypeHandler<String>{

    private final Counter counter;

    private String dmnType;

    public StringHandler(String dmnType, CollectorRegistry registry){
        this.dmnType = dmnType;
        this.counter = initializeCounter(dmnType, registry);
    }

    public StringHandler(String dmnType){
        this(dmnType, null);
    }


    @Override
    public void record(String handler, String sample) {
        counter.labels(handler, sample).inc();
    }

    @Override
    public String getDmnType() {
        return dmnType;
    }

    private Counter initializeCounter(String dmnType, CollectorRegistry registry){
        Counter.Builder builder = Counter.build().name(dmnType + MetricsConstants.DECISIONS_NAME_SUFFIX)
                .help(MetricsConstants.DECISIONS_HELP)
                .labelNames(MetricsConstants.HANDLER_IDENTIFIER_LABELS);

        return registry == null ? builder.register(CollectorRegistry.defaultRegistry) : builder.register(registry);
    }
}
