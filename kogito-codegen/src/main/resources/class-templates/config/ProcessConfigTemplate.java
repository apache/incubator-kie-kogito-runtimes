import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import org.kie.api.event.process.ProcessEventListener;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.signal.SignalManagerHub;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.kie.services.signal.DefaultSignalManagerHub;

public class ProcessConfig implements org.kie.kogito.process.ProcessConfig {

    private final WorkItemHandlerConfig workItemHandlerConfig;
    private final SignalManagerHub signalManagerHub = new DefaultSignalManagerHub();
    private final ProcessEventListenerConfig processEventListenerConfig;
    private final UnitOfWorkManager unitOfWorkManager;
    private final JobsService jobsService;

    @javax.inject.Inject
    public ProcessConfig(
            Instance<org.kie.kogito.process.WorkItemHandlerConfig> workItemHandlerConfig,
            Instance<org.kie.kogito.uow.UnitOfWorkManager> unitOfWorkManager,
            Instance<org.kie.kogito.jobs.JobsService> jobsService,
            Instance<org.kie.kogito.process.ProcessEventListenerConfig> processEventListenerConfigs,
            Instance<org.kie.api.event.process.ProcessEventListener> processEventListeners,
            Instance<org.kie.kogito.event.EventPublisher> eventPublishers) {

        this.workItemHandlerConfig = orDefault(workItemHandlerConfig, org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig::new);
        this.processEventListenerConfig = extract_processEventListenerConfig(processEventListenerConfigs, processEventListeners);
        this.unitOfWorkManager = orDefault(unitOfWorkManager, () -> new org.kie.kogito.services.uow.DefaultUnitOfWorkManager(new org.kie.kogito.services.uow.CollectingUnitOfWorkFactory()));
        this.jobsService = orDefault(jobsService, () -> null);

        eventPublishers.forEach(publisher -> unitOfWorkManager().eventManager().addPublisher(publisher));
        // unitOfWorkManager().eventManager().setService(cfg.serviceUrl().orElse(""));
    }

    public ProcessConfig(
            org.kie.kogito.process.WorkItemHandlerConfig workItemHandlerConfig,
            org.kie.kogito.uow.UnitOfWorkManager unitOfWorkManager,
            org.kie.kogito.jobs.JobsService jobsService,
            java.util.Collection<org.kie.kogito.process.ProcessEventListenerConfig> processEventListenerConfigs,
            java.util.Collection<org.kie.api.event.process.ProcessEventListener> processEventListeners,
            java.util.Collection<org.kie.kogito.event.EventPublisher> eventPublishers) {

        this.workItemHandlerConfig = orDefaultV(workItemHandlerConfig, org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig::new);
        this.processEventListenerConfig = extract_processEventListenerConfig(processEventListenerConfigs, processEventListeners);
        this.unitOfWorkManager = orDefaultV(unitOfWorkManager, () -> new org.kie.kogito.services.uow.DefaultUnitOfWorkManager(new org.kie.kogito.services.uow.CollectingUnitOfWorkFactory()));
        this.jobsService = orDefaultV(jobsService, () -> null);

        eventPublishers.forEach(publisher -> unitOfWorkManager().eventManager().addPublisher(publisher));
        // unitOfWorkManager().eventManager().setService(cfg.serviceUrl().orElse(""));
    }

    public ProcessConfig() {
        this(null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }


    @Override
    public WorkItemHandlerConfig workItemHandlers() {
        return workItemHandlerConfig;
    }

    @Override
    public ProcessEventListenerConfig processEventListeners() {
        return processEventListenerConfig;
    }

    @Override
    public SignalManagerHub signalManagerHub() {
        return signalManagerHub;
    }

    @Override
    public UnitOfWorkManager unitOfWorkManager() {
        return unitOfWorkManager;
    }

    @Override
    public JobsService jobsService() {
        return jobsService;
    }

    public org.kie.kogito.Addons addons() {
        return new org.kie.kogito.Addons(java.util.Arrays.asList());
    }

    private static <T> T orDefault(Instance<T> instance, Supplier<T> supplier) {
        if (instance.isUnsatisfied()) {
            return supplier.get();
        } else {
            return instance.get();
        }
    }

    private static <T> T orDefaultV(T instance, Supplier<T> supplier) {
        if (instance == null) {
            return supplier.get();
        } else {
            return instance;
        }
    }


    private org.kie.kogito.process.ProcessEventListenerConfig extract_processEventListenerConfig(Iterable<ProcessEventListenerConfig> processEventListenerConfigs, Iterable<ProcessEventListener> processEventListeners) {
        return this.merge_processEventListenerConfig(java.util.stream.StreamSupport.stream(processEventListenerConfigs.spliterator(), false).collect(java.util.stream.Collectors.toList()), java.util.stream.StreamSupport.stream(processEventListeners.spliterator(), false).collect(java.util.stream.Collectors.toList()));
    }

    private org.kie.kogito.process.ProcessEventListenerConfig merge_processEventListenerConfig(java.util.Collection<org.kie.kogito.process.ProcessEventListenerConfig> processEventListenerConfigs, java.util.Collection<org.kie.api.event.process.ProcessEventListener> processEventListeners) {
        return new org.kie.kogito.process.impl.CachedProcessEventListenerConfig(merge(processEventListenerConfigs, org.kie.kogito.process.ProcessEventListenerConfig::listeners, processEventListeners));
    }

    private static <C, L> List<L> merge(Collection<C> configs, Function<C, Collection<L>> configToListeners, Collection<L> listeners) {
        return Stream.concat(configs.stream().flatMap(c -> configToListeners.apply(c).stream()), listeners.stream()).collect(Collectors.toList());
    }

}
