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

package org.jbpm.workflow.instance.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.BoundaryEventNode;
import org.jbpm.workflow.core.node.CatchLinkNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.DynamicNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.EventSubProcessNode;
import org.jbpm.workflow.core.node.FaultNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.MilestoneNode;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.StateNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.ThrowLinkNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.ActionNodeInstance;
import org.jbpm.workflow.instance.node.BoundaryEventNodeInstance;
import org.jbpm.workflow.instance.node.CatchLinkNodeInstance;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.CompositeNodeInstance;
import org.jbpm.workflow.instance.node.DynamicNodeInstance;
import org.jbpm.workflow.instance.node.EndNodeInstance;
import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.jbpm.workflow.instance.node.EventSubProcessNodeInstance;
import org.jbpm.workflow.instance.node.FaultNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.jbpm.workflow.instance.node.JoinInstance;
import org.jbpm.workflow.instance.node.MilestoneNodeInstance;
import org.jbpm.workflow.instance.node.RuleSetNodeInstance;
import org.jbpm.workflow.instance.node.SplitInstance;
import org.jbpm.workflow.instance.node.StartNodeInstance;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.jbpm.workflow.instance.node.ThrowLinkNodeInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;

public class NodeInstanceFactoryRegistry {

    private static final NodeInstanceFactoryRegistry INSTANCE = new NodeInstanceFactoryRegistry();

    private Map<Class<? extends Node>, NodeInstanceFactory> registry;

    public static NodeInstanceFactoryRegistry getInstance(Environment environment) {
        // allow custom NodeInstanceFactoryRegistry to be given as part of the environment - e.g simulation
        if (environment != null && environment.get("NodeInstanceFactoryRegistry") != null) {
            return (NodeInstanceFactoryRegistry) environment.get("NodeInstanceFactoryRegistry");
        }

        return INSTANCE;
    }

    protected NodeInstanceFactoryRegistry() {
        this.registry = new HashMap<>();
    }

    public void register(Class<? extends Node> cls,
                         NodeInstanceFactory factory) {
        this.registry.put(cls, factory);
    }

    public NodeInstanceFactory getProcessNodeInstanceFactory(Node node) {
        Class<?> clazz = node.getClass();
        while (clazz != null) {
            NodeInstanceFactory result = this.get(clazz);
            if (result != null) {
                return result;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    protected NodeInstanceFactory get(Class<?> clazz) {
        // hard wired nodes:
        switch (clazz.getSimpleName()) {
            case "RuleSetNode":
                return factory(RuleSetNodeInstance::new);
            case "Split":
                return factory(SplitInstance::new);
            case "Join":
                return factoryOnce(JoinInstance::new);
            case "StartNode":
                return factory(StartNodeInstance::new);
            case "EndNode":
                return factory(EndNodeInstance::new);
            case "MilestoneNode":
                return factory(MilestoneNodeInstance::new);
            case "SubProcessNode":
                return factory(SubProcessNodeInstance::new);
            case "ActionNode":
                return factory(ActionNodeInstance::new);
            case "WorkItemNode":
                return factory(WorkItemNodeInstance::new);
            case "TimerNode":
                return factory(TimerNodeInstance::new);
            case "FaultNode":
                return factory(FaultNodeInstance::new);
            case "EventSubProcessNode":
                return factory(EventSubProcessNodeInstance::new);
            case "CompositeNode":
                return factory(CompositeNodeInstance::new);
            case "CompositeContextNode":
                return factory(CompositeContextNodeInstance::new);
            case "HumanTaskNode":
                return factory(HumanTaskNodeInstance::new);
            case "ForEachNode":
                return factory(ForEachNodeInstance::new);
            case "EventNode":
                return factory(EventNodeInstance::new);
            case "StateNode":
                return factory(StateNodeInstance::new);
            case "DynamicNode":
                return factory(DynamicNodeInstance::new);
            case "BoundaryEventNode":
                return factory(BoundaryEventNodeInstance::new);
            case "CatchLinkNode":
                return factory(
                        CatchLinkNodeInstance::new);
            case "ThrowLinkNode":
                return factory(
                        ThrowLinkNodeInstance::new);
            default:
                return this.registry.get(clazz);
        }
    }

    protected NodeInstanceFactory factoryOnce(Supplier<NodeInstanceImpl> supplier) {
        return (node, processInstance, nodeInstanceContainer) -> {
            NodeInstance result = ((org.jbpm.workflow.instance.NodeInstanceContainer)
                    nodeInstanceContainer).getFirstNodeInstance(node.getId());
            if (result != null) {
                return result;
            } else {
                return createInstance(supplier.get(), node, processInstance, nodeInstanceContainer);
            }
        };
    }

    protected NodeInstanceFactory factory(Supplier<NodeInstanceImpl> supplier) {
        return (node, processInstance, nodeInstanceContainer) ->
                createInstance(supplier.get(), node, processInstance, nodeInstanceContainer);
    }

    private static NodeInstance createInstance(NodeInstanceImpl nodeInstance, Node node, WorkflowProcessInstance processInstance, NodeInstanceContainer nodeInstanceContainer) {
        nodeInstance.setNodeId(node.getId());
        nodeInstance.setNodeInstanceContainer(nodeInstanceContainer);
        nodeInstance.setProcessInstance(processInstance);
        String uniqueId = (String) node.getMetaData().get("UniqueId");
        if (uniqueId == null) {
            uniqueId = node.getId() + "";
        }
        nodeInstance.setMetaData("UniqueId", uniqueId);
        int level = ((org.jbpm.workflow.instance.NodeInstanceContainer) nodeInstanceContainer).getLevelForNode(uniqueId);
        nodeInstance.setLevel(level);
        return nodeInstance;
    }
}
