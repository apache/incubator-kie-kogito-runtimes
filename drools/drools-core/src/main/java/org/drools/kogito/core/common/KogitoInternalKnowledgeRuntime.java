package org.drools.kogito.core.common;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.drools.core.common.EndOperationListener;
import org.drools.core.common.WorkingMemoryAction;
import org.drools.core.time.TimerService;
import org.drools.kogito.core.runtime.process.InternalProcessRuntime;
import org.kie.kogito.internal.KieBase;
import org.kie.kogito.internal.event.process.ProcessEventListener;
import org.kie.kogito.internal.event.rule.AgendaEventListener;
import org.kie.kogito.internal.event.rule.RuleRuntimeEventListener;
import org.kie.kogito.internal.logger.KieRuntimeLogger;
import org.kie.kogito.internal.runtime.Calendars;
import org.kie.kogito.internal.runtime.Channel;
import org.kie.kogito.internal.runtime.Environment;
import org.kie.kogito.internal.runtime.Globals;
import org.kie.kogito.internal.runtime.KieSessionConfiguration;
import org.kie.kogito.internal.runtime.ObjectFilter;
import org.kie.kogito.internal.runtime.process.ProcessInstance;
import org.kie.kogito.internal.runtime.process.WorkItemManager;
import org.kie.kogito.internal.runtime.rule.Agenda;
import org.kie.kogito.internal.runtime.rule.AgendaFilter;
import org.kie.kogito.internal.runtime.rule.EntryPoint;
import org.kie.kogito.internal.runtime.rule.FactHandle;
import org.kie.kogito.internal.runtime.rule.FactHandle.State;
import org.kie.kogito.internal.runtime.rule.LiveQuery;
import org.kie.kogito.internal.runtime.rule.QueryResults;
import org.kie.kogito.internal.runtime.rule.ViewChangedEventListener;
import org.kie.kogito.internal.time.SessionClock;
import org.kie.kogito.jobs.JobsService;

public class KogitoInternalKnowledgeRuntime implements InternalKnowledgeRuntime {
    
    
    private org.drools.core.common.InternalKnowledgeRuntime impl;

    public KogitoInternalKnowledgeRuntime (org.drools.core.common.InternalKnowledgeRuntime impl) {
        this.impl = impl;
    }

    @Override
    public <T extends SessionClock> T getSessionClock() {
        return impl.getSessionClock();
    }

    @Override
    public void setGlobal(String identifier, Object value) {
        impl.setGlobal(identifier, value);

    }

    @Override
    public Object getGlobal(String identifier) {
        return impl.getGlobal(identifier);
    }

    @Override
    public Globals getGlobals() {
        throw new UnsupportedOperationException();
        //return impl.getGlobals();
    }

    @Override
    public Calendars getCalendars() {
        throw new UnsupportedOperationException();
        //return impl.getCalendars();
    }

    @Override
    public Environment getEnvironment() {
        throw new UnsupportedOperationException();
        //return impl.getEnvironment();
    }

 
    @Override
    public void registerChannel(String name, Channel channel) {
        throw new UnsupportedOperationException();
        //impl.registerChannel(name, channel);

    }

    @Override
    public void unregisterChannel(String name) {
        throw new UnsupportedOperationException();
        //impl.unregisterChannel(name);

    }

    @Override
    public Map<String, Channel> getChannels() {
        throw new UnsupportedOperationException();
        //return impl.getChannels();
    }

    @Override
    public KieSessionConfiguration getSessionConfiguration() {
        throw new UnsupportedOperationException();
       //return impl.getSessionConfiguration();
    }

    @Override
    public void halt() {
         impl.halt();

    }

    @Override
    public Agenda getAgenda() {
        throw new UnsupportedOperationException();
        //return impl.getAgenda();
    }

    @Override
    public EntryPoint getEntryPoint(String name) {
        throw new UnsupportedOperationException();
        //return impl.getEntryPoint(name);
    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        throw new UnsupportedOperationException();
       //return impl.getEntryPoints();
    }

    @Override
    public QueryResults getQueryResults(String query, Object... arguments) {
        throw new UnsupportedOperationException();
        //return impl.getQueryResults(query, arguments);
    }

    @Override
    public LiveQuery openLiveQuery(String query, Object[] arguments, ViewChangedEventListener listener) {
        throw new UnsupportedOperationException();
        //return impl.openLiveQuery(query, arguments, listener);
    }

    @Override
    public String getEntryPointId() {
        throw new UnsupportedOperationException();
        //return impl.getEntryPointId();
    }

    @Override
    public FactHandle insert(Object object) {
        throw new UnsupportedOperationException();
        //return impl.insert(object);
    }

    @Override
    public void retract(FactHandle handle) {
        throw new UnsupportedOperationException();
         //impl.retract(handle);

    }

    @Override
    public void delete(FactHandle handle) {
        throw new UnsupportedOperationException();
        //impl.delete(handle);

    }

    @Override
    public void delete(FactHandle handle, State fhState) {
        throw new UnsupportedOperationException();
       //impl.delete(handle);
    }

    @Override
    public void update(FactHandle handle, Object object) {
        throw new UnsupportedOperationException();
        //impl.update(handle, object);

    }

    @Override
    public void update(FactHandle handle, Object object, String... modifiedProperties) {
        throw new UnsupportedOperationException();
        //impl.update(handle, object, modifiedProperties);

    }

    @Override
    public FactHandle getFactHandle(Object object) {
        throw new UnsupportedOperationException();
       // return impl.getFactHandle(object);
    }

    @Override
    public Object getObject(FactHandle factHandle) {
        throw new UnsupportedOperationException();
        //return impl.getObject(factHandle);
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return impl.getObjects();
    }

    @Override
    public Collection<? extends Object> getObjects(ObjectFilter filter) {
        throw new UnsupportedOperationException();
        //return impl.getObjects(filter);
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        throw new UnsupportedOperationException();
        //return impl.getFactHandles();
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        throw new UnsupportedOperationException();
        //return impl.getFactHandles(filter);
    }

    @Override
    public long getFactCount() {
        return impl.getFactCount();
    }

    @Override
    public ProcessInstance startProcess(String processId) {
        return new KogitoProcessInstance(impl.startProcess(processId));
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters) {
        return new KogitoProcessInstance(impl.startProcess(processId,parameters));
    }

    @Override
    public ProcessInstance createProcessInstance(String processId, Map<String, Object> parameters) {
        return new KogitoProcessInstance(impl.startProcess(processId,parameters));
    }

    @Override
    public ProcessInstance startProcess(String processId, AgendaFilter agendaFilter) {
        throw new UnsupportedOperationException();
      //  return new KogitoProcessInstance(impl.startProcess(processId,agendaFilter));
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters, AgendaFilter agendaFilter) {
        throw new UnsupportedOperationException();
        //return new KogitoProcessInstance(impl.startProcess(processId,parameters, agendaFilter));
    }

    @Override
    public ProcessInstance startProcessInstance(String processInstanceId) {
        return new KogitoProcessInstance(impl.startProcessInstance(Long.parseLong(processInstanceId)));
    }

    @Override
    public ProcessInstance startProcessInstance(String processInstanceId, String trigger) {
        return new KogitoProcessInstance(impl.startProcessInstance(Long.parseLong(processInstanceId)));
    }

    @Override
    public void signalEvent(String type, Object event) {
        impl.signalEvent(type, event);

    }

    @Override
    public void signalEvent(String type, Object event, String processInstanceId) {
        impl.signalEvent(type, event, Long.parseLong(processInstanceId));
    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return impl.getProcessInstances().stream().map(KogitoProcessInstance::new).collect(Collectors.toList());
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
       return new KogitoProcessInstance(impl.getProcessInstance(Long.parseLong(processInstanceId)));
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId, boolean readonly) {
        return new KogitoProcessInstance(impl.getProcessInstance(Long.parseLong(processInstanceId)));
    }

    @Override
    public void abortProcessInstance(String processInstanceId) {
        impl.abortProcessInstance(Long.parseLong(processInstanceId));
    }

    @Override
    public WorkItemManager getWorkItemManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JobsService getJobsService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public KieRuntimeLogger getLogger() {
        throw new UnsupportedOperationException();
        //return impl.getLogger();
    }

    @Override
    public void addEventListener(RuleRuntimeEventListener listener) {
        throw new UnsupportedOperationException();
        //impl.addEventListener(listener);

    }

    @Override
    public void removeEventListener(RuleRuntimeEventListener listener) {
        throw new UnsupportedOperationException();
        //impl.removeEventListener(listener);

    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        throw new UnsupportedOperationException();
        //return impl.getRuleRuntimeEventListeners();
    }

    @Override
    public void addEventListener(AgendaEventListener listener) {
        throw new UnsupportedOperationException();
        //impl.addEventListener(listener);

    }

    @Override
    public void removeEventListener(AgendaEventListener listener) {
        throw new UnsupportedOperationException();
       //impl.removeEventListener(listener);

    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        throw new UnsupportedOperationException();
        //return impl.getAgendaEventListeners();
    }

    @Override
    public void addEventListener(ProcessEventListener listener) {
       throw new UnsupportedOperationException();

    }

    @Override
    public void removeEventListener(ProcessEventListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ProcessEventListener> getProcessEventListeners() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimerService getTimerService() {
        return impl.getTimerService();
    }

    @Override
    public void startOperation() {
        impl.startOperation();

    }

    @Override
    public void endOperation() {
       impl.endOperation();

    }

    @Override
    public void executeQueuedActions() {
       impl.executeQueuedActions();

    }

    @Override
    public void queueWorkingMemoryAction(WorkingMemoryAction action) {
        impl.queueWorkingMemoryAction(action);

    }

    @Override
    public InternalProcessRuntime getProcessRuntime() {
       throw new UnsupportedOperationException();
    }

    @Override
    public void setIdentifier(long id) {
        impl.setIdentifier(id);

    }

    @Override
    public void setEndOperationListener(EndOperationListener listener) {
        impl.setEndOperationListener(listener);
    }

    @Override
    public long getLastIdleTimestamp() {
        return impl.getLastIdleTimestamp();
    }

    @Override
    public KieBase getKieBase() {
        throw new UnsupportedOperationException();
    }
}
