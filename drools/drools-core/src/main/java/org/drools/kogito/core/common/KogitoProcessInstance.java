package org.drools.kogito.core.common;

import java.util.Collections;
import java.util.Map;

import org.kie.api.definition.process.Process;
import org.kie.kogito.internal.runtime.process.ProcessInstance;

public class KogitoProcessInstance implements ProcessInstance {
    
    
    private org.kie.api.runtime.process.ProcessInstance impl;

    public KogitoProcessInstance (org.kie.api.runtime.process.ProcessInstance impl) {
        this.impl = impl;
    }

    @Override
    public void signalEvent(String type, Object event) {
       impl.signalEvent(type, event);
        
    }

    @Override
    public String[] getEventTypes() {
        return impl.getEventTypes();
    }

    @Override
    public String getProcessId() {
        return impl.getProcessId();
    }

    @Override
    public Process getProcess() {
        return impl.getProcess();
    }

    @Override
    public String getId() {
        return Long.toString(impl.getId());
    }

    @Override
    public String getProcessName() {
        return impl.getProcessName();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public int getState() {
        return impl.getState();
    }

    @Override
    public String getParentProcessInstanceId() {
       return Long.toString(impl.getParentProcessInstanceId());
    }

    @Override
    public String getRootProcessInstanceId() {
        return null;
    }

    @Override
    public String getRootProcessId() {
        return null;
    }

    @Override
    public Map<String, Object> getVariables() {
        return Collections.emptyMap();
    }

    @Override
    public String getReferenceId() {
        return null;
    }

}
