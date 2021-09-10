/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.kogito.internal.process.event.KogitoEventListener;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime counterpart of an event node.
 *
 */
public class AsyncEventNodeInstance extends EventNodeInstance {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AsyncEventNodeInstance.class);
    private final String eventType = UUID.randomUUID().toString();

    private final KogitoEventListener listener = new AsyncExternalEventListener();

    //receive the signal when it is the node is executed
    private class AsyncExternalEventListener implements KogitoEventListener {
        @Override
        public String[] getEventTypes() {
            return new String[] { getEventType() };
        }

        @Override
        public void signalEvent(String type,
                Object event) {
            triggerCompleted();
        }
    }

    public void internalTrigger(final KogitoNodeInstance from, String type) {
        super.internalTrigger(from, type);
        Optional<CompletableFuture<Void>> futureExecution = Optional.ofNullable(getProcessInstance().getKnowledgeRuntime().getProcessRuntime())
                .filter(InternalProcessRuntime.class::isInstance)
                .map(InternalProcessRuntime.class::cast)
                .map(InternalProcessRuntime::getAsyncExecutor)
                .map(asyncExecutor -> asyncExecutor.run(() -> {
                    getProcessInstance().signalEvent(getEventType(), null);
                    return null;
                }))
                .map(result -> result.thenAccept(r -> logger.info("Async trigger executed for node {}", getNode())));

        if (!futureExecution.isPresent()) {
            logger.warn("No async executor service found continuing as sync operation for node {}", getNode());
            triggerCompleted();
        }
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public Node getNode() {
        return new AsyncEventNode(super.getNode());
    }

    @Override
    protected KogitoEventListener getEventListener() {
        return listener;
    }

    @Override
    public void triggerCompleted() {
        getProcessInstance().removeEventListener(getEventType(), getEventListener(), true);
        NodeInstanceContainer instanceContainer = (NodeInstanceContainer) getNodeInstanceContainer();
        instanceContainer.setCurrentLevel(getLevel());
        instanceContainer.removeNodeInstance(this);

        NodeInstance actualInstance = instanceContainer.getNodeInstance(getNode());
        //trigger the actual node
        triggerNodeInstance((org.jbpm.workflow.instance.NodeInstance) actualInstance, NodeImpl.CONNECTION_DEFAULT_TYPE);
    }
}
