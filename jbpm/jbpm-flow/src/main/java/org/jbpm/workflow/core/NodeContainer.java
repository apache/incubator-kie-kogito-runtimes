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
package org.jbpm.workflow.core;

import java.io.Serializable;

import org.jbpm.process.core.Context;
import org.kie.api.definition.process.Node;

public interface NodeContainer extends org.kie.api.definition.process.NodeContainer, Serializable {

    /**
     * Method for adding a node to this node container. 
     * Note that the node will get an id unique for this node container.
     * 
     * @param node  the node to be added
     * @throws IllegalArgumentException if <code>node</code> is null 
     */
    void addNode(Node node);

    /**
     * Method for removing a node from this node container
     * 
     * @param node  the node to be removed
     * @throws IllegalArgumentException if <code>node</code> is null or unknown
     */
    void removeNode(Node node);
    
    Context resolveContext(String contextId, Object param);
    
    Node internalGetNode(long id);
    
}
