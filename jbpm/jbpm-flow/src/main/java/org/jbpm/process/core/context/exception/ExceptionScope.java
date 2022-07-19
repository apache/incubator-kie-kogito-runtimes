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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.AbstractContext;
import org.jbpm.ruleflow.core.Metadata;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.process.workitem.WorkItemExecutionException;

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

    private Throwable getException(KogitoProcessContext context) {
        return (Throwable) context.getContextData().get("Exception");
    }

    private boolean isHandler(ExceptionHandler handler, Throwable exception) {
        return handler.getExceptionCode().map(code -> test(code, exception)).orElse(false);
    }

    public ExceptionHandler getExceptionHandler(String key, Throwable exception, KogitoProcessContext context) {
        Optional<ExceptionHandler> result = Optional.empty();
        if (key != null) {
            result = Optional.ofNullable(exceptionHandlers.get(key));
        } else if (exception != null) {
            Collection<String> errorNames = getErrorNames(context);
            if (!errorNames.isEmpty()) {
                return errorNames.stream().map(k -> exceptionHandlers.get(k)).filter(h -> isHandler(h, exception)).findFirst().orElse(null);
            } else if (exception instanceof WorkItemExecutionException) {
                result = Optional.ofNullable(exceptionHandlers.get(((WorkItemExecutionException) exception).getErrorCode()));
            }
            if (result.isEmpty()) {
                result = exceptionHandlers.entrySet().stream().filter(e -> e.getKey() != null && test(e.getKey(), exception)).findAny().map(Entry::getValue);
            }
        }
        return result.orElse(exceptionHandlers.get(null));
    }

    private Collection<String> getErrorNames(KogitoProcessContext context) {
        if (context == null) {
            return Collections.emptyList();
        }
        Node node = context.getNodeInstance().getNode();
        Collection<String> errorNames = null;
        do {
            errorNames = (Collection<String>) node.getMetaData().get(Metadata.ERROR_NAME);
            NodeContainer container = node.getNodeContainer();
            node = container instanceof Node ? (Node) container : null;
        } while (errorNames == null && node != null);
        return errorNames == null ? Collections.emptyList() : errorNames;
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
