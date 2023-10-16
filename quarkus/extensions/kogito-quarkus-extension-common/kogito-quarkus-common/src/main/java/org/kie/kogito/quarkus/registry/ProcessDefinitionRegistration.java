package org.kie.kogito.quarkus.registry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.quarkus.runtime.StartupEvent;
import org.kie.kogito.Application;
import org.kie.kogito.process.Processes;
import org.kie.kogito.quarkus.config.KogitoRuntimeConfig;
import org.kie.kogito.services.registry.ProcessDefinitionEventRegistry;

@ApplicationScoped
public class ProcessDefinitionRegistration {

    Instance<Processes> processes;
    ProcessDefinitionEventRegistry processDefinitionRegistry;

    @Inject
    public ProcessDefinitionRegistration(Application application, KogitoRuntimeConfig runtimeConfig, Instance<Processes> processes) {
        this.processes = processes;
        this.processDefinitionRegistry = new ProcessDefinitionEventRegistry(application, runtimeConfig.serviceUrl.orElse(null));
    }

    void onStartUp(@Observes StartupEvent startupEvent) {
        if (processes.isResolvable()) {
            processDefinitionRegistry.register(processes.get());
        }
    }
}
