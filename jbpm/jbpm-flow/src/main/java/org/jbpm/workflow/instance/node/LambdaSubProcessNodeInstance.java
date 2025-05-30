/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jbpm.workflow.instance.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.drools.util.StringUtils;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.ContextContainer;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.KogitoProcessContextImpl;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.process.instance.impl.ContextInstanceFactory;
import org.jbpm.process.instance.impl.ContextInstanceFactoryRegistry;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.util.ContextFactory;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.node.SubProcessFactory;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.event.KogitoEventListener;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.impl.AbstractProcessInstance;

/**
 * Runtime counterpart of a SubFlow node.
 * 
 */
public class LambdaSubProcessNodeInstance extends StateBasedNodeInstance implements KogitoEventListener, ContextInstanceContainer {

    private static final long serialVersionUID = 510l;

    private Map<String, List<ContextInstance>> subContextInstances = new HashMap<>();

    private String processInstanceId;
    private boolean asyncWaitingNodeInstance;

    protected SubProcessNode getSubProcessNode() {
        return (SubProcessNode) getNode();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void internalTrigger(KogitoNodeInstance from, String type) {
        super.internalTrigger(from, type);
        // if node instance was cancelled, abort
        if (getNodeInstanceContainer().getNodeInstance(getStringId()) == null) {
            return;
        }
        if (!Node.CONNECTION_DEFAULT_TYPE.equals(type)) {
            throw new IllegalArgumentException(
                    "A SubProcess node only accepts default incoming connections!");
        }

        KogitoProcessContextImpl context = ContextFactory.fromNode(this);
        SubProcessFactory subProcessFactory = getSubProcessNode().getSubProcessFactory();
        Object o = subProcessFactory.bind(context);
        org.kie.kogito.process.ProcessInstance<?> processInstance = subProcessFactory.createInstance(o);

        org.kie.api.runtime.process.ProcessInstance pi = ((AbstractProcessInstance<?>) processInstance).internalGetProcessInstance();
        ((ProcessInstanceImpl) pi).setMetaData("ParentProcessInstanceId", getProcessInstance().getStringId());
        ((ProcessInstanceImpl) pi).setMetaData("ParentNodeInstanceId", getUniqueId());
        ((ProcessInstanceImpl) pi).setMetaData("ParentNodeId", getSubProcessNode().getUniqueId());
        ((ProcessInstanceImpl) pi).setParentProcessInstanceId(getProcessInstance().getStringId());
        ((ProcessInstanceImpl) pi)
                .setRootProcessInstanceId(StringUtils.isEmpty(getProcessInstance().getRootProcessInstanceId()) ? getProcessInstance().getStringId() : getProcessInstance().getRootProcessInstanceId());
        ((ProcessInstanceImpl) pi).setRootProcessId(StringUtils.isEmpty(getProcessInstance().getRootProcessId()) ? getProcessInstance().getProcessId() : getProcessInstance().getRootProcessId());
        ((ProcessInstanceImpl) pi).setSignalCompletion(getSubProcessNode().isWaitForCompletion());

        processInstance.start();
        this.processInstanceId = processInstance.id();
        this.asyncWaitingNodeInstance = hasAsyncNodeInstance(pi);

        subProcessFactory.unbind(context, processInstance.variables());

        if (!getSubProcessNode().isWaitForCompletion()) {
            triggerCompleted();
        } else if (processInstance.status() == KogitoProcessInstance.STATE_COMPLETED || processInstance.status() == KogitoProcessInstance.STATE_ABORTED) {
            processInstanceCompleted((ProcessInstance) pi);
        } else {
            addProcessListener();
        }
    }

    private boolean hasAsyncNodeInstance(org.kie.api.runtime.process.ProcessInstance pi) {
        return Optional.ofNullable(pi)
                .map(ProcessInstanceImpl.class::cast)
                .map(instance -> instance.getMetaData().get(Metadata.ASYNC_WAITING))
                .map(Boolean.class::cast)
                .orElse(false);
    }

    @Override
    public void cancel(CancelType cancelType) {
        super.cancel(cancelType);
        if (getSubProcessNode() != null && getSubProcessNode().isIndependent()) {
            return;
        }
        // not subprocess attached (before completion)
        if (processInstanceId == null) {
            return;
        }

        KogitoProcessRuntime kruntime = (KogitoProcessRuntime) ((ProcessInstance) getProcessInstance()).getKnowledgeRuntime();
        Optional<AbstractProcessInstance> pi =
                ((MutableProcessInstances) kruntime.getApplication().get(Processes.class).processById(this.getSubProcessNode().getProcessId()).instances()).findById(processInstanceId);
        pi.ifPresent(e -> e.abort());
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public boolean isAsyncWaitingNodeInstance() {
        return asyncWaitingNodeInstance;
    }

    public void internalSetProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public void addEventListeners() {
        super.addEventListeners();
        addProcessListener();
    }

    private void addProcessListener() {
        getProcessInstance().addEventListener("processInstanceCompleted:" + processInstanceId, this, true);
    }

    @Override
    public void removeEventListeners() {
        super.removeEventListeners();
        getProcessInstance().removeEventListener("processInstanceCompleted:" + processInstanceId, this, true);
    }

    @Override
    public void signalEvent(String type, Object event) {
        if (("processInstanceCompleted:" + processInstanceId).equals(type)) {
            this.processInstanceCompleted((ProcessInstance) event);
        } else {
            super.signalEvent(type, event);
        }
    }

    @Override
    public String[] getEventTypes() {
        return new String[] { "processInstanceCompleted:" + processInstanceId };
    }

    public void processInstanceCompleted(ProcessInstance processInstance) {
        removeEventListeners();
        handleOutMappings(processInstance);
        processInstanceId = null;
        if (processInstance.getState() == KogitoProcessInstance.STATE_ABORTED) {
            String faultName = processInstance.getOutcome() == null ? "" : processInstance.getOutcome();
            // handle exception as sub process failed with error code
            ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance) resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, faultName);
            if (exceptionScopeInstance != null) {
                KogitoProcessContextImpl context = new KogitoProcessContextImpl(this.getProcessInstance().getKnowledgeRuntime());
                context.setProcessInstance(this.getProcessInstance());
                context.setNodeInstance(this);
                context.getContextData().put("Exception", processInstance.getFaultData());
                exceptionScopeInstance.handleException(faultName, context);
                if (getSubProcessNode() != null && !getSubProcessNode().isIndependent() && getSubProcessNode().isAbortParent()) {
                    cancel();
                }
                return;
            } else if (getSubProcessNode() != null && !getSubProcessNode().isIndependent() && getSubProcessNode().isAbortParent()) {
                getProcessInstance().setState(KogitoProcessInstance.STATE_ABORTED, faultName);
                return;
            }

        }
        // handle dynamic subprocess
        if (getNode() == null) {
            setMetaData("NodeType", "SubProcessNode");
        }
        // if there were no exception proceed normally
        triggerCompleted();

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void handleOutMappings(ProcessInstance processInstance) {

        SubProcessFactory subProcessFactory = getSubProcessNode().getSubProcessFactory();
        org.kie.kogito.process.ProcessInstance<?> pi = ((org.kie.kogito.process.ProcessInstance<?>) processInstance.unwrap());
        if (pi != null) {
            Model model = (Model) pi.process().createModel();
            model.fromMap(processInstance.getVariables());
            subProcessFactory.unbind(ContextFactory.fromNode(this), model);
        }
    }

    @Override
    public String getNodeName() {
        org.kie.api.definition.process.Node node = getNode();
        if (node == null) {
            return "[Dynamic] Sub Process";
        }
        return super.getNodeName();
    }

    @Override
    public List<ContextInstance> getContextInstances(String contextId) {
        return this.subContextInstances.get(contextId);
    }

    @Override
    public void addContextInstance(String contextId, ContextInstance contextInstance) {
        List<ContextInstance> list = this.subContextInstances.get(contextId);
        if (list == null) {
            list = new ArrayList<>();
            this.subContextInstances.put(contextId, list);
        }
        list.add(contextInstance);
    }

    @Override
    public void removeContextInstance(String contextId, ContextInstance contextInstance) {
        List<ContextInstance> list = this.subContextInstances.get(contextId);
        if (list != null) {
            list.remove(contextInstance);
        }
    }

    @Override
    public ContextInstance getContextInstance(String contextId, long id) {
        List<ContextInstance> contextInstances = subContextInstances.get(contextId);
        if (contextInstances != null) {
            for (ContextInstance contextInstance : contextInstances) {
                if (contextInstance.getContextId() == id) {
                    return contextInstance;
                }
            }
        }
        return null;
    }

    @Override
    public ContextInstance getContextInstance(Context context) {
        ContextInstanceFactory conf = ContextInstanceFactoryRegistry.INSTANCE.getContextInstanceFactory(context);
        if (conf == null) {
            throw new IllegalArgumentException("Illegal context type (registry not found): " + context.getClass());
        }
        ContextInstance contextInstance = conf.getContextInstance(context, this, getProcessInstance());
        if (contextInstance == null) {
            throw new IllegalArgumentException("Illegal context type (instance not found): " + context.getClass());
        }
        return contextInstance;
    }

    @Override
    public ContextContainer getContextContainer() {
        return getSubProcessNode();
    }

}
