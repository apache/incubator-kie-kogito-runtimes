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

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.AbstractContext;
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

    public ExceptionHandler getExceptionHandler(String key, Throwable exception, KogitoProcessContext context) {
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
        return this;
    }
}
