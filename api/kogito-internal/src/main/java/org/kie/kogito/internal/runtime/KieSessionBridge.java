package org.kie.kogito.internal.runtime;

import java.util.Collection;
import java.util.Map;

import org.kie.api.KieBase;
import org.kie.api.command.Command;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.Calendars;
import org.kie.api.runtime.Channel;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieSession.AtomicAction;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.Agenda;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.FactHandle.State;
import org.kie.api.runtime.rule.LiveQuery;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.ViewChangedEventListener;
import org.kie.api.time.SessionClock;
import org.kie.kogito.internal.event.process.ProcessEventListener;
import org.kie.kogito.internal.runtime.process.ProcessInstance;
import org.kie.kogito.internal.runtime.process.WorkItemManager;
import org.kie.kogito.jobs.JobsService;

/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class KieSessionBridge implements KieSession {

    private org.kie.api.runtime.KieSession impl;
    
    public KieSessionBridge (org.kie.api.runtime.KieSession impl) {
        this.impl = impl;
    }
    
    @Override
    public int fireAllRules() {
        return impl.fireAllRules();
    }

    @Override
    public int fireAllRules(int max) {
        return impl.fireAllRules(max);
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter) {
        return impl.fireAllRules(agendaFilter);
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter, int max) {
        return impl.fireAllRules(agendaFilter, max);
    }

    @Override
    public void fireUntilHalt() {
        impl.fireUntilHalt();

    }

    @Override
    public void fireUntilHalt(AgendaFilter agendaFilter) {
        impl.fireUntilHalt(agendaFilter);

    }

    @Override
    public <T> T execute(Command<T> command) {
        return impl.execute(command);
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
        return impl.getGlobals();
    }

    @Override
    public Calendars getCalendars() {
        return impl.getCalendars();
    }

    @Override
    public Environment getEnvironment() {
        return impl.getEnvironment();
    }

    @Override
    public KieBase getKieBase() {
      return impl.getKieBase();
    }

    @Override
    public void registerChannel(String name, Channel channel) {
        impl.registerChannel(name, channel);

    }

    @Override
    public void unregisterChannel(String name) {
        impl.unregisterChannel(name);

    }

    @Override
    public Map<String, Channel> getChannels() {
        return impl.getChannels();
    }

    @Override
    public KieSessionConfiguration getSessionConfiguration() {
       return impl.getSessionConfiguration();
    }

    @Override
    public void halt() {
        impl.halt();

    }

    @Override
    public Agenda getAgenda() {
        return impl.getAgenda();
    }

    @Override
    public EntryPoint getEntryPoint(String name) {

        return impl.getEntryPoint(name);
    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        return impl.getEntryPoints();
    }

    @Override
    public QueryResults getQueryResults(String query, Object... arguments) {
        return impl.getQueryResults(query, arguments);
    }

    @Override
    public LiveQuery openLiveQuery(String query, Object[] arguments, ViewChangedEventListener listener) {
        return impl.openLiveQuery(query, arguments, listener);
    }

    @Override
    public String getEntryPointId() {
       return impl.getEntryPointId();
    }

    @Override
    public FactHandle insert(Object object) {
        return impl.insert(object);
    }

    @Override
    public void retract(FactHandle handle) {
        impl.retract(handle);

    }

    @Override
    public void delete(FactHandle handle) {
        impl.delete(handle);

    }

    @Override
    public void delete(FactHandle handle, State fhState) {
        impl.delete(handle, fhState);

    }

    @Override
    public void update(FactHandle handle, Object object) {
        impl.update(handle, object);

    }

    @Override
    public void update(FactHandle handle, Object object, String... modifiedProperties) {
        impl.update(handle, object, modifiedProperties);
    }

    @Override
    public FactHandle getFactHandle(Object object) {
        return impl.getFactHandle(object);
    }

    @Override
    public Object getObject(FactHandle factHandle) {
        return impl.getObject(factHandle);
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return impl.getObjects();
    }

    @Override
    public Collection<? extends Object> getObjects(ObjectFilter filter) {
        return impl.getObjects(filter);
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        return impl.getFactHandles();
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        return impl.getFactHandles(filter);
    }

    @Override
    public long getFactCount() {
        return impl.getFactCount();
    }

    @Override
    public ProcessInstance startProcess(String processId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance createProcessInstance(String processId, Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance startProcess(String processId, AgendaFilter agendaFilter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters, AgendaFilter agendaFilter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance startProcessInstance(String processInstanceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance startProcessInstance(String processInstanceId, String trigger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void signalEvent(String type, Object event) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void signalEvent(String type, Object event, String processInstanceId) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId, boolean readonly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abortProcessInstance(String processInstanceId) {
        throw new UnsupportedOperationException();

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
        return impl.getLogger();
    }

    @Override
    public void addEventListener(RuleRuntimeEventListener listener) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeEventListener(RuleRuntimeEventListener listener) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEventListener(AgendaEventListener listener) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeEventListener(AgendaEventListener listener) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return impl.getAgendaEventListeners();
    }


    @Override
    public int getId() {
        return impl.getId();
    }

    @Override
    public long getIdentifier() {
        return impl.getIdentifier();
    }

    @Override
    public void dispose() {
        impl.dispose();

    }

    @Override
    public void destroy() {
        impl.destroy();

    }

    @Override
    public void submit(AtomicAction action) {
        impl.submit(action);

    }

    @Override
    public <T> T getKieRuntime(Class<T> cls) {
        return impl.getKieRuntime(cls);
    }

    @Override
    public void addEventListener(org.kie.kogito.internal.event.process.ProcessEventListener listener) { 
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeEventListener(org.kie.kogito.internal.event.process.ProcessEventListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ProcessEventListener> getProcessEventListeners() {
        throw new UnsupportedOperationException();
    }

}
