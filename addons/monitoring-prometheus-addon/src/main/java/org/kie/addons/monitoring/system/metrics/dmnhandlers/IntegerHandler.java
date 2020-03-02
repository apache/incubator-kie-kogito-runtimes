package org.kie.addons.monitoring.system.metrics.dmnhandlers;

import io.prometheus.client.Summary;
import org.kie.addons.monitoring.system.metrics.MetricsConstants;

public class IntegerHandler implements TypeHandler<Integer> {

    private final Summary summary;

    public IntegerHandler(Class c){
        this.summary = initializeCounter(getClassName(c));
    }

    private Summary initializeCounter(String className){
        Summary summary = Summary.build().name(MetricsConstants.DECISIONS_NAME + className)
                .help(MetricsConstants.DECISIONS_HELP)
                .labelNames(MetricsConstants.HANDLER_LABEL).register();
        return summary;
    }

    @Override
    public void record(String handler, Integer sample) {
        summary.labels(handler).observe(sample);
    }
}
