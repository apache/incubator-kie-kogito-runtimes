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
package org.jbpm.process.instance.context.exception;

import java.util.Optional;

import org.jbpm.process.core.context.exception.ExceptionHandler;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.instance.context.AbstractContextInstance;
import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExceptionScopeInstance extends AbstractContextInstance {

    private static final long serialVersionUID = 510l;
    private static final Logger logger = LoggerFactory.getLogger(ExceptionScopeInstance.class);

    @Override
    public String getContextType() {
        return ExceptionScope.EXCEPTION_SCOPE;
    }

    public ExceptionScope getExceptionScope() {
        return (ExceptionScope) getContext();
    }

    public void handleException(Throwable exception, KogitoProcessContext params) {
        handleException(null, exception, params);
    }

    public void handleException(String key, KogitoProcessContext params) {
        handleException(key, null, params);
    }

    private Optional<String> getErrorName(KogitoProcessContext context) {
        Node node = context.getNodeInstance().getNode();
        String errorName = null;
        do {
            errorName = (String) node.getMetaData().get(Metadata.ERROR_NAME);
            NodeContainer container = node.getNodeContainer();
            node = container instanceof Node ? (Node) container : null;
        } while (errorName == null && node != null);
        return Optional.ofNullable(errorName);
    }

    public void handleException(String key, Throwable exception, KogitoProcessContext context) {
        if (key == null) {
            key = getErrorName(context).orElse(exception.getClass().getCanonicalName());
        }
        ExceptionHandler handler = getExceptionScope().getExceptionHandler(key, exception);
        if (handler == null) {
            logger.info("Could not find ExceptionHandler for key {} and exception {}", key, exception);
            if (exception != null) {
                throw new WorkflowRuntimeException(context.getNodeInstance(), context.getProcessInstance(), null, exception);
            } else {
                throw new IllegalArgumentException("Could not find ExceptionHandler for key " + key);
            }
        }
        handleException(handler, key, context);
    }

    public abstract void handleException(ExceptionHandler handler, String exception, KogitoProcessContext params);

}
