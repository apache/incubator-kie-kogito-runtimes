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
package org.jbpm.workflow.core.node;

import org.jbpm.workflow.core.Node;
import org.kie.api.definition.process.Connection;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;

public class TimerNode extends ExtendedNodeImpl {

    private static final long serialVersionUID = 510l;
    
    private Timer timer;
    
    public void setTimer(Timer timer) {
        this.timer = timer;
    }
    
    public Timer getTimer() {
        return this.timer;
    }

    public void validateAddIncomingConnection(final String type, final Connection connection) {
        super.validateAddIncomingConnection(type, connection);
        if (!Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
        	throw new IllegalArgumentException(
                    "This type of node [" + connection.getTo().getMetaData().get("UniqueId") + ", " + connection.getTo().getName() 
                    + "] only accepts default incoming connection type!");
        }
        if (getFrom() != null && !"true".equals(System.getProperty("jbpm.enable.multi.con"))) {
        	throw new IllegalArgumentException(
                    "This type of node [" + connection.getTo().getMetaData().get("UniqueId") + ", " + connection.getTo().getName() 
                    + "] cannot have more than one incoming connection!");
        }
    }

    public void validateAddOutgoingConnection(final String type, final Connection connection) {
        super.validateAddOutgoingConnection(type, connection);
        if (!Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
        	throw new IllegalArgumentException(
                    "This type of node [" + connection.getFrom().getMetaData().get("UniqueId") + ", " + connection.getFrom().getName() 
                    + "] only accepts default outgoing connection type!");
        }
        if (getTo() != null && !"true".equals(System.getProperty("jbpm.enable.multi.con"))) {
        	throw new IllegalArgumentException(
                    "This type of node [" + connection.getFrom().getMetaData().get("UniqueId") + ", " + connection.getFrom().getName() 
                    + "] cannot have more than one outgoing connection!");
        }
    }
    
}
