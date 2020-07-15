import java.util.List;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.signal.SignalManagerHub;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.kie.services.signal.DefaultSignalManagerHub;

@org.springframework.stereotype.Component
public class ProcessConfig extends org.kie.kogito.process.impl.AbstractProcessConfig {

    @org.springframework.beans.factory.annotation.Autowired
    public ProcessConfig(
            List<org.kie.kogito.process.WorkItemHandlerConfig> workItemHandlerConfig,
            List<org.kie.kogito.uow.UnitOfWorkManager> unitOfWorkManager,
            List<org.kie.kogito.jobs.JobsService> jobsService,
            List<org.kie.kogito.process.ProcessEventListenerConfig> processEventListenerConfigs,
            List<org.kie.api.event.process.ProcessEventListener> processEventListeners,
            List<org.kie.kogito.event.EventPublisher> eventPublishers) {

        super(workItemHandlerConfig,
              processEventListenerConfigs,
              processEventListeners,
              unitOfWorkManager,
              jobsService,
              eventPublishers);
    }
}
