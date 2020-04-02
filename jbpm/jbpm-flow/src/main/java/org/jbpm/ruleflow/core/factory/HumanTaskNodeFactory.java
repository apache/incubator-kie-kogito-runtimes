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

import org.jbpm.process.core.Work;
import org.jbpm.process.core.impl.WorkImpl;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowNodeContainerFactory;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.HumanTaskNode;

/**
 *
 */
public class HumanTaskNodeFactory extends WorkItemNodeFactory {

    public HumanTaskNodeFactory(RuleFlowNodeContainerFactory nodeContainerFactory, NodeContainer nodeContainer, long id) {
        super(nodeContainerFactory, nodeContainer, id);
    }

    protected Node createNode() {
        return new HumanTaskNode();
    }

    protected HumanTaskNode getHumanTaskNode() {
        return (HumanTaskNode) getNode();
    }

    public HumanTaskNodeFactory name(String name) {
        super.name(name);
        return this;
    }

    public HumanTaskNodeFactory inMapping(String parameterName, String variableName) {
        super.inMapping(parameterName, variableName);
        return this;
    }

    public HumanTaskNodeFactory outMapping(String parameterName, String variableName) {
        super.outMapping(parameterName, variableName);
        return this;
    }

    public HumanTaskNodeFactory waitForCompletion(boolean waitForCompletion) {
        super.waitForCompletion(waitForCompletion);
        return this;
    }
    public HumanTaskNodeFactory timer(String delay, String period, String dialect, String action) {
        super.timer(delay, period, dialect, action);
        return this;
    }

    public HumanTaskNodeFactory taskName(String taskName) {
        Work work = getHumanTaskNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            getHumanTaskNode().setWork(work);
        }
        work.setParameter("TaskName", taskName);
        return this;
    }

    public HumanTaskNodeFactory actorId(String actorId) {
        Work work = getHumanTaskNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            getHumanTaskNode().setWork(work);
        }
        work.setParameter("ActorId", actorId);
        return this;
    }

    public HumanTaskNodeFactory groupId(String groupId) {
        Work work = getHumanTaskNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            getHumanTaskNode().setWork(work);
        }
        work.setParameter("GroupId", groupId);
        return this;
    }

    public HumanTaskNodeFactory priority(String priority) {
        Work work = getHumanTaskNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            getHumanTaskNode().setWork(work);
        }
        work.setParameter("Priority", priority);
        return this;
    }

    public HumanTaskNodeFactory comment(String comment) {
        Work work = getHumanTaskNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            getHumanTaskNode().setWork(work);
        }
        work.setParameter("Comment", comment);
        return this;
    }

    public HumanTaskNodeFactory skippable(boolean skippable) {
        Work work = getHumanTaskNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            getHumanTaskNode().setWork(work);
        }
        work.setParameter("Skippable", Boolean.toString(skippable));
        return this;
    }

    public HumanTaskNodeFactory content(String content) {
        Work work = getHumanTaskNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            getHumanTaskNode().setWork(work);
        }
        work.setParameter("Content", content);
        return this;
    }

    public HumanTaskNodeFactory swimlane(String swimlane) {
        getHumanTaskNode().setSwimlane(swimlane);
        return this;
    }

    public HumanTaskNodeFactory workParameter(String name, Object value) {
        Work work = getHumanTaskNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            getHumanTaskNode().setWork(work);
        }
        work.setParameter(name, value);
        return this;
    }
}

