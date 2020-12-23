package org.kie.kogito.process.event;

import org.kie.api.runtime.process.NodeInstance;
import org.kie.kogito.process.ProcessInstance;

public class KogitoSignalEvent extends KogitoNodeEvent{

    private String name;
    private Object object;
    
    public KogitoSignalEvent(ProcessInstance<?> processInstance, NodeInstance nodeInstance, String name, Object object) {
        super(processInstance, nodeInstance);
        this.name = name;
        this.object = object;
     
    }
    
    public String getName () {
        return name;
    }
    
    public Object getObject() {
        return object;
    }
}
