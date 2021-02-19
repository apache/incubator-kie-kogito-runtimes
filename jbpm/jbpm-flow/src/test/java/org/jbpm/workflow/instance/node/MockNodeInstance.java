/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
/**
 * 
 */
package org.jbpm.workflow.instance.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;

public class MockNodeInstance extends NodeInstanceImpl {
    
    private static final long serialVersionUID = 510l;

    private Map<String, List<NodeInstance>> triggers = new HashMap<String, List<NodeInstance>>();
    private MockNode mockNode;
    
    public MockNodeInstance(MockNode mockNode) {
        this.mockNode = mockNode;
    }
    
    public org.kie.api.definition.process.Node getNode() {
        return mockNode;
    }
    
    public MockNode getMockNode() {
        return mockNode;
    }
    
    public void internalTrigger( KogitoNodeInstance from, String type) {
        if (type == null) {
            throw new IllegalArgumentException(
                "Trigger type is null!");
        }
        triggerTime = new Date();
        List<NodeInstance> list = triggers.get(type);
        if (list == null) {
            list = new ArrayList<NodeInstance>();
            triggers.put(type, list);
        }
        list.add(from);
    }
    
    public Map<String, List<NodeInstance>> getTriggers() {
        return triggers;
    }      
    
    public int hashCode() {
        return (int) getNodeId();
    }
    
    public boolean equals(Object object) {
        if ( object == null || (!( object instanceof MockNodeInstance ) )) {
            return false;
        }
        MockNodeInstance other = ( MockNodeInstance ) object;
        return getNodeId() == other.getNodeId();
    }        
    
    public void triggerCompleted() {
        triggerCompleted( Node.CONNECTION_DEFAULT_TYPE, true);
    }
}
