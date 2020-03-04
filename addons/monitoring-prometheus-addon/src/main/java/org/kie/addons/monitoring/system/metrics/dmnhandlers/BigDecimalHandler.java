package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import java.math.BigDecimal;

import io.prometheus.client.Summary;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;

public class BigDecimalHandler implements TypeHandler<BigDecimal> {

    private final Summary summary;

    public BigDecimalHandler(String prefix){
        this.summary = initializeCounter(prefix);
    }

    private Summary initializeCounter(String prefix){
        Summary summary = Summary.build() // Calculate quantiles over a sliding window of time - default = 10 minutes
                .quantile(0.1, 0.01)   // Add 10th percentile with 1% tolerated error
                .quantile(0.25, 0.05)
                .quantile(0.50, 0.05)   // Add 50th percentile (= median) with 5% tolerated error
                .quantile(0.75, 0.05)
                .quantile(0.9, 0.01)
                .quantile(0.99, 0.01)
                .name(prefix + MetricsConstants.DECISIONS_NAME_SUFFIX)
                .help(MetricsConstants.DECISIONS_HELP)
                .labelNames(MetricsConstants.HANDLER_LABEL).register();
        return summary;
    }

    @Override
    public void record(String handler, BigDecimal sample) {
        summary.labels(handler).observe(sample.doubleValue());
    }
}
