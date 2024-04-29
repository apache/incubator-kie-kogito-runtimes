package org.kie.sonataflow.monitoring;

import org.kie.kogito.KogitoGAV;
import org.kie.kogito.config.ConfigBean;
import org.kie.kogito.internal.process.event.KogitoProcessEventListener;
import org.kie.kogito.serverless.workflow.monitoring.SonataFlowMetricProcessEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Metrics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class SonataFlowMetricEventListenerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SonataFlowMetricEventListenerFactory.class);

    @Inject
    ConfigBean configBean;

    @Produces
    public KogitoProcessEventListener produceProcessListener() {
        LOGGER.info("Producing sonataflow listener for process monitoring.");
        return new SonataFlowMetricProcessEventListener(
                configBean.getGav().orElse(KogitoGAV.EMPTY_GAV), Metrics.globalRegistry);
    }
}
