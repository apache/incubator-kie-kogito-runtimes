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

import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.node.FaultNode;

public class FaultNodeFactory extends ExtendedNodeFactory {

    public static final String METHOD_FAULT_NAME = "faultName";
    public static final String METHOD_FAULT_VARIABLE = "faultVariable";

    public FaultNodeFactory(RuleFlowNodeContainerFactory nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, id);
    }

    protected Node createNode() {
        return new FaultNode();
    }

    protected FaultNode getFaultNode() {
        return (FaultNode) getNode();
    }

    @Override
    public FaultNodeFactory name(String name) {
        super.name(name);
        return this;
    }

    public FaultNodeFactory faultVariable(String faultVariable) {
        getFaultNode().setFaultVariable(faultVariable);
        return this;
    }

    public FaultNodeFactory faultName(String faultName) {
        getFaultNode().setFaultName(faultName);
        return this;
    }
}
