package org.kie.kogito.process.event;

import org.kie.api.runtime.process.NodeInstance;
import org.kie.kogito.process.ProcessInstance;

public class KogitoMessageEvent extends KogitoSignalEvent{
    public KogitoMessageEvent(ProcessInstance<?> processInstance, NodeInstance nodeInstance, String messageName, Object messageObject) {
        super(processInstance, nodeInstance, messageName, messageObject);
    }
    
}
