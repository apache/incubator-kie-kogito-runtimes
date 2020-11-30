package org.kie.kogito.internal.runtime;

import java.util.Collection;
import java.util.Map;

import org.kie.kogito.internal.KieBase;
import org.kie.kogito.internal.command.Command;
import org.kie.kogito.internal.event.process.ProcessEventListener;
import org.kie.kogito.internal.event.rule.AgendaEventListener;
import org.kie.kogito.internal.event.rule.RuleRuntimeEventListener;
import org.kie.kogito.internal.logger.KieRuntimeLogger;
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
        throw new UnsupportedOperationException();
        //return impl.fireAllRules(agendaFilter);
    }

    @Override
    public int fireAllRules(AgendaFilter agendaFilter, int max) {
        throw new UnsupportedOperationException();
        //return impl.fireAllRules(agendaFilter, max);
    }

    @Override
    public void fireUntilHalt() {
        impl.fireUntilHalt();

    }

    @Override
    public void fireUntilHalt(AgendaFilter agendaFilter) {
        throw new UnsupportedOperationException();
      //  impl.fireUntilHalt(agendaFilter);

    }

    @Override
    public <T> T execute(Command<T> command) {
        throw new UnsupportedOperationException();
       // return impl.execute(command);
    }

    @Override
    public <T extends SessionClock> T getSessionClock() {
        throw new UnsupportedOperationException();
        //return impl.getSessionClock();
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
        impl.unregisterChannel(name);

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
       return impl.getEntryPointId();
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
    public void update(FactHandle handle, Object object) {
        throw new UnsupportedOperationException();
       // impl.update(handle, object);

    }

    @Override
    public void update(FactHandle handle, Object object, String... modifiedProperties) {
        throw new UnsupportedOperationException();
        //impl.update(handle, object, modifiedProperties);
    }

    @Override
    public FactHandle getFactHandle(Object object) {
        throw new UnsupportedOperationException();
        //return impl.getFactHandle(object);
    }

    @Override
    public Object getObject(FactHandle factHandle) {
        throw new UnsupportedOperationException();
        //return impl.getObject(factHandle);
    }

    @Override
    public Collection<? extends Object> getObjects() {
        throw new UnsupportedOperationException();
        //return impl.getObjects();
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
        throw new UnsupportedOperationException();
        //return impl.getLogger();
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
        throw new UnsupportedOperationException();
        //return impl.getAgendaEventListeners();
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

    @Override
    public void delete(FactHandle handle, State fhState) {
        throw new UnsupportedOperationException();   
    }

    @Override
    public KieBase getKieBase() {
        throw new UnsupportedOperationException();
    }

}
