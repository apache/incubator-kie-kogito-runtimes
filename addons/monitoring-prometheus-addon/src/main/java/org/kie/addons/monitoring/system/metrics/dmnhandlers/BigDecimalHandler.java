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
        Summary summary = Summary.build().name(prefix + MetricsConstants.DECISIONS_NAME_SUFFIX)
                .help(MetricsConstants.DECISIONS_HELP)
                .labelNames(MetricsConstants.HANDLER_LABEL).register();
        return summary;
    }

    @Override
    public void record(String handler, BigDecimal sample) {
        summary.labels(handler).observe(sample.doubleValue());
    }
}
