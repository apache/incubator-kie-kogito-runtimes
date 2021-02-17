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

import org.jbpm.process.core.Contextable;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.NodeContainer;
import org.kie.kogito.internal.process.runtime.KogitoNode;

/**
 * Represents a node in a RuleFlow. 
 * 
 */
public interface Node extends KogitoNode, Contextable, Serializable {

    String CONNECTION_DEFAULT_TYPE = "DROOLS_DEFAULT";
    
    /**
     * Method for setting the id of the node
     * 
     * @param id	the id of the node
     */
    void setId(long id);

    /**
     * Method for setting the name of the node
     * 
     * @param name 	the name of the node
     */
    void setName(String name);
    
    String getUniqueId();

    void addIncomingConnection(String type, Connection connection);
    
    void addOutgoingConnection(String type, Connection connection);
    
    void removeIncomingConnection(String type, Connection connection);
    
    void removeOutgoingConnection(String type, Connection connection);

    void setParentContainer(NodeContainer nodeContainer);
    
    void setMetaData(String name, Object value);
    
}
