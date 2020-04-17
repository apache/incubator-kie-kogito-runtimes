/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;

import java.util.ArrayList;
import java.util.List;

public abstract class ExtendedNodeFactory extends NodeFactory {

    protected static final String METADATA_ACTION = "Action";

    protected ExtendedNodeFactory(RuleFlowNodeContainerFactory nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, id);
    }

    protected ExtendedNodeImpl getExtendedNode() {
        return (ExtendedNodeImpl) getNode();
    }

    public ExtendedNodeFactory onEntryAction(String dialect, String action) {
        if (getExtendedNode().getActions(dialect) != null) {
            getExtendedNode().getActions(dialect).add(new DroolsConsequenceAction(dialect, action));
        } else {
            List<DroolsAction> actions = new ArrayList<>();
            actions.add(new DroolsConsequenceAction(dialect, action));
            getExtendedNode().setActions(ExtendedNodeImpl.EVENT_NODE_ENTER, actions);
        }
        return this;
    }

    public ExtendedNodeFactory onExitAction(String dialect, String action) {
        if (getExtendedNode().getActions(dialect) != null) {
            getExtendedNode().getActions(dialect).add(new DroolsConsequenceAction(dialect, action));
        } else {
            List<DroolsAction> actions = new ArrayList<>();
            actions.add(new DroolsConsequenceAction(dialect, action));
            getExtendedNode().setActions(ExtendedNodeImpl.EVENT_NODE_EXIT, actions);
        }
        return this;
    }
}
