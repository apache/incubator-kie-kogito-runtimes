package org.kie.kogito.monitoring.core.common.process;

import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;

public class MonitoringProcessEventListenerConfig extends DefaultProcessEventListenerConfig {

    public MonitoringProcessEventListenerConfig() {
        super(new MetricsProcessEventListener("deafault-process-monitoring-listener"));
    }
}

