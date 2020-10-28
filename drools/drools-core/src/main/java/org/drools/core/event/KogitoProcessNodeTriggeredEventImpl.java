/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.event;

import org.kie.kogito.internal.event.process.ProcessNodeTriggeredEvent;
import org.kie.kogito.internal.runtime.KieRuntime;
import org.kie.kogito.internal.runtime.process.NodeInstance;

public class KogitoProcessNodeTriggeredEventImpl extends org.drools.kogito.core.event.ProcessEvent implements ProcessNodeTriggeredEvent {

    private static final long serialVersionUID = 510l;

    private NodeInstance nodeInstance;

    public KogitoProcessNodeTriggeredEventImpl( final NodeInstance nodeInstance, KieRuntime kruntime ) {
        super( nodeInstance.getProcessInstance(), kruntime );
        this.nodeInstance = nodeInstance;
    }
    
    @Override
    public NodeInstance getNodeInstance() {
        return nodeInstance;
    }

    @Override
    public String toString() {
        return "==>[ProcessNodeTriggered(nodeId=" + nodeInstance.getNodeId() + "; id=" + nodeInstance.getId() 
            + "; nodeName=" + getNodeInstance().getNodeName() + "; processName=" + getProcessInstance().getProcessName() + "; processId=" + getProcessInstance().getProcessId() + ")]";
    }
}
