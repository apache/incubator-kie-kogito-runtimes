/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jbpm.process.instance.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.impl.XmlProcessDumper;
import org.jbpm.process.core.impl.XmlProcessDumperFactory;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.workflow.core.WorkflowProcess;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.rule.Agenda;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;

/**
 * Default implementation of a process instance.
 */
public abstract class ProcessInstanceImpl implements ProcessInstance,
        Serializable {

    private static final long serialVersionUID = 510l;

    private String id;
    private String processId;
    private transient Process process;
    private String processXml;
    private int state = STATE_PENDING;
    private Map<String, ContextInstance> contextInstances = new HashMap<>();
    private Map<String, List<ContextInstance>> subContextInstances = new HashMap<>();
    private transient InternalKnowledgeRuntime kruntime;
    private Map<String, Object> metaData = new HashMap<>();
    private String outcome;
    private String parentProcessInstanceId;
    private String rootProcessInstanceId;
    private String description;
    private String rootProcessId;

    private Map<String, List<String>> headers;

    private String processVersion;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getStringId() {
        return this.id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

    public void updateProcess(final Process process) {
        setProcess(process);
        XmlProcessDumper dumper = XmlProcessDumperFactory.newXmlProcessDumperFactory();
        this.processXml = dumper.dumpProcess(process);
    }

    public String getProcessXml() {
        return processXml;
    }

    public void setProcessXml(String processXml) {
        if (processXml != null && processXml.trim().length() > 0) {
            this.processXml = processXml;
        }
    }

    @Override
    public Process getProcess() {
        if (this.process == null) {
            if (processXml == null) {
                if (kruntime == null) {
                    throw new IllegalStateException("Process instance " + id + "[" + processId + "] is disconnected.");
                }
                this.process = kruntime.getKieBase().getProcess(processId);
            } else {
                XmlProcessDumper dumper = XmlProcessDumperFactory.newXmlProcessDumperFactory();
                this.process = dumper.readProcess(processXml);
            }
        }
        return this.process;
    }

    @Override
    public void setProcess(final Process process) {
        this.processId = process.getId();
        this.processVersion = process.getVersion();
        this.process = process;
    }

    public void setInternalProcess(Process process) {
        this.process = process;
    }

    @Override
    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    @Override
    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Override
    public String getProcessName() {
        return getProcess().getName();
    }

    public void setInternalState(int state) {
        this.state = state;
    }

    @Override
    public void setState(final int state, String outcome) {
        this.outcome = outcome;
        internalSetState(state);
    }

    public void internalSetState(final int state) {
        this.state = state;
    }

    protected boolean automaticCompensation() {
        return process.getMetaData().containsKey(Metadata.COMPENSATE_WHEN_ABORTED);
    }

    @Override
    public int getState() {
        return this.state;
    }

    @Override
    public void setState(final int state) {
        internalSetState(state);
    }

    @Override
    public InternalKnowledgeRuntime getKnowledgeRuntime() {
        return this.kruntime;
    }

    @Override
    public void setKnowledgeRuntime(final InternalKnowledgeRuntime kruntime) {
        if (this.kruntime != null) {
            throw new IllegalArgumentException("Runtime can only be set once.");
        }
        this.kruntime = kruntime;
    }

    public Agenda getAgenda() {
        if (getKnowledgeRuntime() == null) {
            return null;
        }
        return getKnowledgeRuntime().getAgenda();
    }

    @Override
    public ContextContainer getContextContainer() {
        return (ContextContainer) getProcess();
    }

    public void setContextInstance(String contextId, ContextInstance contextInstance) {
        this.contextInstances.put(contextId, contextInstance);
    }

    @Override
    public ContextInstance getContextInstance(String contextId) {
        ContextInstance contextInstance = this.contextInstances.get(contextId);
        if (contextInstance != null) {
            return contextInstance;
        }
        Context context = ((ContextContainer) getProcess()).getDefaultContext(contextId);
        if (context != null) {
            contextInstance = getContextInstance(context);
            return contextInstance;
        }
        return null;
    }

    @Override
    public List<ContextInstance> getContextInstances(String contextId) {
        return this.subContextInstances.get(contextId);
    }

    @Override
    public void addContextInstance(String contextId, ContextInstance contextInstance) {
        List<ContextInstance> list = this.subContextInstances.get(contextId);
        if (list == null) {
            list = new ArrayList<>();
            this.subContextInstances.put(contextId, list);
        }
        list.add(contextInstance);
    }

    @Override
    public void removeContextInstance(String contextId, ContextInstance contextInstance) {
        List<ContextInstance> list = this.subContextInstances.get(contextId);
        if (list != null) {
            list.remove(contextInstance);
        }
    }

    @Override
    public ContextInstance getContextInstance(String contextId, long id) {
        List<ContextInstance> contextInstances = subContextInstances.get(contextId);
        if (contextInstances != null) {
            for (ContextInstance contextInstance : contextInstances) {
                if (contextInstance.getContextId() == id) {
                    return contextInstance;
                }
            }
        }
        return null;
    }

    @Override
    public ContextInstance getContextInstance(final Context context) {
        ContextInstanceFactory conf = ContextInstanceFactoryRegistry.INSTANCE.getContextInstanceFactory(context);
        if (conf == null) {
            throw new IllegalArgumentException("Illegal context type (registry not found): " + context.getClass());
        }
        ContextInstance contextInstance = conf.getContextInstance(context, this, this);
        if (contextInstance == null) {
            throw new IllegalArgumentException("Illegal context type (instance not found): " + context.getClass());
        }
        return contextInstance;
    }

    @Override
    public void signalEvent(String type, Object event) {
    }

    @Override
    public void start() {
        start(null);
    }

    @Override
    public void start(String trigger) {
        synchronized (this) {
            if (getState() != KogitoProcessInstance.STATE_PENDING) {
                throw new IllegalArgumentException("A process instance can only be started once");
            }
            setState(KogitoProcessInstance.STATE_ACTIVE);
            internalStart(trigger);
        }
    }

    protected abstract void internalStart(String trigger);

    @Override
    public void disconnect() {
        ((InternalProcessRuntime) kruntime.getProcessRuntime()).getProcessInstanceManager().internalRemoveProcessInstance(this);
    }

    @Override
    public void reconnect() {
        ((InternalProcessRuntime) kruntime.getProcessRuntime()).getProcessInstanceManager().internalAddProcessInstance(this);
    }

    @Override
    public String[] getEventTypes() {
        return new String[0];
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("ProcessInstance ");
        b.append(getStringId());
        b.append(" [processId=");
        b.append(this.process.getId());
        b.append(",state=");
        b.append(this.state);
        b.append("]");
        return b.toString();
    }

    @Override
    public Map<String, Object> getMetaData() {
        return this.metaData;
    }

    @Override
    public void setMetaData(String name, Object data) {
        this.metaData.put(name, data);
    }

    @Override
    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    @Override
    public String getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    @Override
    public void setParentProcessInstanceId(String parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    @Override
    public String getRootProcessInstanceId() {
        return rootProcessInstanceId;
    }

    @Override
    public void setRootProcessInstanceId(String rootProcessInstanceId) {
        this.rootProcessInstanceId = rootProcessInstanceId;
    }

    @Override
    public String getRootProcessId() {
        return rootProcessId;
    }

    @Override
    public void setRootProcessId(String rootProcessId) {
        this.rootProcessId = rootProcessId;
    }

    @Override
    public String getDescription() {
        if (description == null) {
            description = process.getName();
            if (process != null) {
                Object metaData = process.getMetaData().get("customDescription");
                if (metaData instanceof String) {
                    description = ((WorkflowProcess) process).evaluateExpression((String) metaData, this);
                }
            }
        }

        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
}
