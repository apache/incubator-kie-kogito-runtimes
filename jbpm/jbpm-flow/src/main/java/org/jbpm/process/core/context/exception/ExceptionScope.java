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
package org.jbpm.process.core.context.exception;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.AbstractContext;
import org.jbpm.ruleflow.core.Metadata;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;

public class ExceptionScope extends AbstractContext {

    private static final long serialVersionUID = 510l;

    public static final String EXCEPTION_SCOPE = "ExceptionScope";

    protected Map<String, ExceptionHandler> exceptionHandlers = new HashMap<String, ExceptionHandler>();

    private transient Collection<ExceptionHandlerPolicy> policies = ExceptionHandlerPolicyFactory.getHandlerPolicies();

    @Override
    public String getType() {
        return EXCEPTION_SCOPE;
    }

    public void setExceptionHandler(String exception, ExceptionHandler exceptionHandler) {
        this.exceptionHandlers.put(exception, exceptionHandler);
    }

    private Optional<String> getErrorName(KogitoProcessContext context) {
        if (context == null) {
            return Optional.empty();
        }
        Node node = context.getNodeInstance().getNode();
        String errorName = null;
        do {
            errorName = (String) node.getMetaData().get(Metadata.ERROR_NAME);
            NodeContainer container = node.getNodeContainer();
            node = container instanceof Node ? (Node) container : null;
        } while (errorName == null && node != null);
        return Optional.ofNullable(errorName);
    }

    private Throwable getException(KogitoProcessContext context) {
        return (Throwable) context.getContextData().get("Exception");
    }

    public ExceptionHandler getExceptionHandler(String key, Throwable exception, KogitoProcessContext context) {
        if (key == null) {
            key = getErrorName(context).orElse(exception != null ? exception.getClass().getCanonicalName() : null);
        }
        ExceptionHandler handler = exceptionHandlers.get(key);
        if (handler == null || exception != null && handler.getExceptionCode().map(errorCode -> !test(errorCode, exception)).orElse(true)) {
            handler = exceptionHandlers.get(null);
        }
        return handler;
    }

    private boolean test(String errorCode, Throwable exception) {
        boolean found = false;
        Iterator<ExceptionHandlerPolicy> iter = policies.iterator();
        while (!found && iter.hasNext()) {
            found = iter.next().test(errorCode, exception);
        }
        return found;
    }

    public void removeExceptionHandler(String exception) {
        this.exceptionHandlers.remove(exception);
    }

    public Map<String, ExceptionHandler> getExceptionHandlers() {
        return exceptionHandlers;
    }

    public void setExceptionHandlers(Map<String, ExceptionHandler> exceptionHandlers) {
        if (exceptionHandlers == null) {
            throw new IllegalArgumentException("Exception handlers are null");
        }
        this.exceptionHandlers = exceptionHandlers;
    }

    @Override
    public Context resolveContext(Object param) {
        if (param instanceof String) {
            return getExceptionHandler((String) param, null, null) == null ? null : this;
        } else if (param instanceof Throwable) {
            return getExceptionHandler(null, (Throwable) param, null) == null ? null : this;
        } else if (param instanceof KogitoProcessContext) {
            return getExceptionHandler(null, getException((KogitoProcessContext) param), (KogitoProcessContext) param) == null ? null : this;
        }
        throw new IllegalArgumentException(
                "ExceptionScopes can only resolve exception names: " + param);
    }
}
