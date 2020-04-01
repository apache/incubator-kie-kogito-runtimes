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

package org.jbpm.ruleflow.core.factory;

import org.jbpm.process.core.context.exception.ExceptionHandler;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.event.EventTypeFilter;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.EventSubProcessNode;

public class EventSubProcessNodeFactory extends CompositeNodeFactory {

    public EventSubProcessNodeFactory(RuleFlowNodeContainerFactory nodeContainerFactory, NodeContainer nodeContainer, long id) {
    	super(nodeContainerFactory, nodeContainer, id);
    }

    protected EventSubProcessNode getEventSubProcessNode() {
        return (EventSubProcessNode) getNodeContainer();
    }

    @Override
    protected CompositeContextNode createNode(long id) {
        EventSubProcessNode node = new EventSubProcessNode();
        node.setId(id);
        return node;
    }
    @Override
    public EventSubProcessNodeFactory variable(String name, DataType type) {
        super.variable(name, type);
        return this;
    }

    @Override
    public EventSubProcessNodeFactory variable(String name, DataType type, Object value) {
        super.variable(name, type, value);
        return this;
    }

    @Override
    public EventSubProcessNodeFactory exceptionHandler(String exception, ExceptionHandler exceptionHandler) {
        super.exceptionHandler(exception, exceptionHandler);
        return this;
    }

    @Override
    public EventSubProcessNodeFactory name(String name) {
        super.name(name);
        return this;
    }

    @Override
    public EventSubProcessNodeFactory metaData(String name, Object value) {
        super.metaData(name, value);
        return this;
    }

    @Override
    public EventSubProcessNodeFactory linkIncomingConnections(long nodeId) {
        super.linkIncomingConnections(nodeId);
        return this;
    }

    @Override
    public EventSubProcessNodeFactory linkOutgoingConnections(long nodeId) {
        super.linkOutgoingConnections(nodeId);
        return this;
    }

    public EventSubProcessNodeFactory keepActive(boolean keepActive) {
        getEventSubProcessNode().setKeepActive(keepActive);
        return this;
    }

    public CompositeNodeFactory timer(String delay, String period, String dialect, String action) {
        Timer timer = new Timer();
        timer.setDelay(delay);
        timer.setPeriod(period);
        getEventSubProcessNode().addTimer(timer, new DroolsConsequenceAction(dialect, action));
        return this;
    }

    public EventSubProcessNodeFactory event(EventTypeFilter event) {
        getEventSubProcessNode().addEvent(event);
        return this;
    }
}
