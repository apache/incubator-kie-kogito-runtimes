package org.kie.kogito.monitoring.system.metrics.dmnhandlers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;

public class LocalDateTimeHandler implements TypeHandler<LocalDateTime> {

    private final Summary summary;

    private String dmnType;

    public LocalDateTimeHandler(String dmnType, CollectorRegistry registry) {
        this.dmnType = dmnType;
        this.summary = initializeCounter(dmnType, registry);
    }

    public LocalDateTimeHandler(String dmnType) {
        this(dmnType, null);
    }

    @Override
    public void record(String type, String endpointName, LocalDateTime sample) {
        summary.labels(type, endpointName).observe(sample.toInstant(ZoneOffset.UTC).toEpochMilli());
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
                .name(dmnType.replace(" ", "_") + DecisionConstants.DECISIONS_NAME_SUFFIX)
                .help(DecisionConstants.DECISIONS_HELP)
                .labelNames(DecisionConstants.DECISION_ENDPOINT_LABELS);
        return registry == null ? builder.register(CollectorRegistry.defaultRegistry) : builder.register(registry);
    }
}