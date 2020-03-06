package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import java.math.BigDecimal;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;
import org.kie.dmn.model.v1_1.TDMNElement;

public class BigDecimalHandler implements TypeHandler<BigDecimal> {

    private final Summary summary;

    private String dmnType;

    public BigDecimalHandler(String dmnType, CollectorRegistry registry) {
        this.dmnType = dmnType;
        this.summary = initializeCounter(dmnType, registry);
    }

    public BigDecimalHandler(String dmnType) {
        this(dmnType, null);
    }

    @Override
    public void record(String handler, BigDecimal sample) {
        summary.labels(handler).observe(sample.doubleValue());
    }

    @Override
    public String getDmnType() {
        return dmnType;
    }

    private Summary initializeCounter(String dmnType, CollectorRegistry registry) {
        Summary.Builder builder = Summary.build() // Calculate quantiles over a sliding window of time - default = 10 minutes
                .quantile(0.1, 0.01)   // Add 10th percentile with 1% tolerated error
                .quantile(0.25, 0.05)
                .quantile(0.50, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
                .quantile(0.75, 0.05)
                .quantile(0.9, 0.05)
                .quantile(0.99, 0.01)
                .name(dmnType + MetricsConstants.DECISIONS_NAME_SUFFIX)
                .help(MetricsConstants.DECISIONS_HELP)
                .labelNames(MetricsConstants.HANDLER_LABEL);

        Summary summary = registry == null ? builder.register(CollectorRegistry.defaultRegistry) : builder.register(registry);

        return summary;
    }
}
