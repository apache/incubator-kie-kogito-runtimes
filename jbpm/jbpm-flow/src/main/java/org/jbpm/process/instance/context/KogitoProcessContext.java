package org.jbpm.process.instance.context;



import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.runtime.process.CaseAssignment;
import org.kie.api.runtime.process.CaseData;
import org.kie.kogito.internal.runtime.KieRuntime;
import org.kie.kogito.internal.runtime.process.NodeInstance;
import org.kie.kogito.internal.runtime.process.ProcessContext;
import org.kie.kogito.internal.runtime.process.ProcessInstance;
import org.kie.kogito.internal.runtime.process.WorkflowProcessInstance;


public class KogitoProcessContext implements ProcessContext {

    private KieRuntime runtime;
    private ProcessInstance processInstance;
    private NodeInstance nodeInstance;
    private Map<String,Object> vars = new ConcurrentHashMap<>();
    private CaseData caseData;
    private CaseAssignment caseAssignment;

    public KogitoProcessContext(KieRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public KieRuntime getKieRuntime() {
        return runtime;
    }

    @Override
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    @Override
    public NodeInstance getNodeInstance() {
       return nodeInstance;
    }

    @Override
    public Object getVariable(String variableName) {
       if (nodeInstance != null )
           return nodeInstance.getVariable(variableName);
       else if (processInstance instanceof WorkflowProcessInstance) {
           return ((WorkflowProcessInstance)processInstance).getVariable(variableName);
       }
       throw new IllegalStateException ("Either nodeInstance or processInstance needs to be set");
    }

    @Override
    public void setVariable(String variableName, Object value) {
        if (nodeInstance != null)
            nodeInstance.setVariable(variableName, value);
        else if (processInstance instanceof WorkflowProcessInstance) {
            ((WorkflowProcessInstance) processInstance).setVariable(variableName, value);
        } else {
            throw new IllegalStateException("Either nodeInstance or processInstance needs to be set");
        }
    }

    @Override
    public CaseAssignment getCaseAssignment() {
        return caseAssignment;
    }

    @Override
    public CaseData getCaseData() {
        return caseData;
    }

    public void setNodeInstance(NodeInstance nodeInstance) {
       this.nodeInstance = nodeInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;   
    }
}
