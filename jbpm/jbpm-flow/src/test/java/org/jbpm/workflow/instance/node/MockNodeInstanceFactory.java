/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceFactory;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;

public class MockNodeInstanceFactory implements NodeInstanceFactory {
    
    private MockNodeInstance instance;
    
    public MockNodeInstanceFactory(MockNodeInstance instance) {
        this.instance = instance;
    }
    
    public MockNodeInstance getMockNodeInstance() {
        return this.instance;
    }

    public NodeInstance getNodeInstance(Node node, WorkflowProcessInstance processInstance, NodeInstanceContainer nodeInstanceContainer) {
        instance.setProcessInstance(processInstance);
        instance.setNodeInstanceContainer(nodeInstanceContainer);
        return instance;
    }
      
}
