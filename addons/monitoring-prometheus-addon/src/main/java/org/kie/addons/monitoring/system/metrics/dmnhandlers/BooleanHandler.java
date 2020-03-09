package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class BooleanHandler implements TypeHandler<Boolean> {

    private final Counter counter;

    private String dmnType;

    public BooleanHandler(String dmnType, CollectorRegistry registry) {
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

    private Counter initializeCounter(String dmnType, CollectorRegistry registry) {
        Counter.Builder builder = Counter.build().name(dmnType + DecisionConstants.DECISIONS_NAME_SUFFIX)
                .help(DecisionConstants.DECISIONS_HELP)
                .labelNames(DecisionConstants.HANDLER_IDENTIFIER_LABELS);

        return registry == null ? builder.register(CollectorRegistry.defaultRegistry) : builder.register(registry);
    }
}
