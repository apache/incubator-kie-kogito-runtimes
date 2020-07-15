import java.util.List;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.signal.SignalManagerHub;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.kie.services.signal.DefaultSignalManagerHub;

@javax.inject.Singleton
public class ProcessConfig extends org.kie.kogito.process.impl.AbstractProcessConfig {

    @javax.inject.Inject
    public ProcessConfig(
            javax.enterprise.inject.Instance<org.kie.kogito.process.WorkItemHandlerConfig> workItemHandlerConfig,
            javax.enterprise.inject.Instance<org.kie.kogito.uow.UnitOfWorkManager> unitOfWorkManager,
            javax.enterprise.inject.Instance<org.kie.kogito.jobs.JobsService> jobsService,
            javax.enterprise.inject.Instance<org.kie.kogito.process.ProcessEventListenerConfig> processEventListenerConfigs,
            javax.enterprise.inject.Instance<org.kie.api.event.process.ProcessEventListener> processEventListeners,
            javax.enterprise.inject.Instance<org.kie.kogito.event.EventPublisher> eventPublishers) {

        super(workItemHandlerConfig,
              processEventListenerConfigs,
              processEventListeners,
              unitOfWorkManager,
              jobsService,
              eventPublishers);
    }

}
