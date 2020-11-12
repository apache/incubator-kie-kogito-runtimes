package org.kie.kogito.monitoring.core.quarkus;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.kie.kogito.monitoring.core.api.process.ProcessEventListener;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;

@ApplicationScoped
public class QuarkusDefaultProcessEventListenerConfig extends DefaultProcessEventListenerConfig {

    public QuarkusDefaultProcessEventListenerConfig() {
        super(new ProcessEventListener("deafault-process-monitoring-listener"));
    }
}
