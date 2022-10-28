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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.AbstractContext;
import org.kie.kogito.process.workitem.WorkItemExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionScope extends AbstractContext {

    private static final long serialVersionUID = 510l;

    private static final Logger logger = LoggerFactory.getLogger(ExceptionScope.class);

    public static final String EXCEPTION_SCOPE = "ExceptionScope";

    protected Map<String, ExceptionHandler> exceptionHandlers = new HashMap<>();
    private transient Collection<ExceptionHandlerPolicy> policies = ExceptionHandlerPolicyFactory.getHandlerPolicies();

    @Override
    public String getType() {
        return EXCEPTION_SCOPE;
    }

    public void setExceptionHandler(String exception, ExceptionHandler exceptionHandler) {
        this.exceptionHandlers.put(exception, exceptionHandler);
    }

    public ExceptionHandler getExceptionHandler(String exception) {
        ExceptionHandler result = exceptionHandlers.get(exception);
        if (result == null) {
            result = exceptionHandlers.get(null);
        }
        return result;
    }

    private static class MatchPolicy implements Comparable<MatchPolicy> {
        private final Entry<String, ExceptionHandler> exceptionHandler;
        private final int handlerWeight;

        public MatchPolicy(Entry<String, ExceptionHandler> exceptionHandler, int handlerWeight) {
            this.exceptionHandler = exceptionHandler;
            this.handlerWeight = handlerWeight;
        }

        @Override
        public int compareTo(MatchPolicy o) {
            return o.handlerWeight - this.handlerWeight;
        }

        public Entry<String, ExceptionHandler> getExceptionHandler() {
            return exceptionHandler;
        }

        @Override
        public int hashCode() {
            return exceptionHandler.getKey().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MatchPolicy other = (MatchPolicy) obj;
            return Objects.equals(exceptionHandler.getKey(), other.exceptionHandler.getKey());
        }

        @Override
        public String toString() {
            return "MatchPolicy [exceptionHandler=" + exceptionHandler + ", handlerWeight=" + handlerWeight + "]";
        }
    }

    protected ExceptionHandler getHandlerFromPolicies(Throwable exception) {
        SortedSet<MatchPolicy> collector = new TreeSet<>();
        for (Entry<String, ExceptionHandler> handler : exceptionHandlers.entrySet()) {
            int handlerWeight = test(handler.getKey(), exception);
            if (handlerWeight > 0) {
                MatchPolicy match = new MatchPolicy(handler, handlerWeight);
                if (!collector.add(match)) {
                    logger.warn("There are two exception handlers with the same priority for exception {}. Rejected hit is {}. Weights are {}", exception, match, collector);
                }
            }
        }
        return collector.isEmpty() ? null : collector.first().getExceptionHandler().getValue();
    }

    public ExceptionHandler getExceptionHandler(Throwable exception) {
        ExceptionHandler handler = getHandlerFromPolicies(exception);
        if (handler == null && exception instanceof WorkItemExecutionException) {
            handler = exceptionHandlers.get(((WorkItemExecutionException) exception).getErrorCode());
        }
        if (handler == null) {
            handler = exceptionHandlers.get(null);
        }
        return handler;
    }

    private int test(String className, Throwable exception) {
        return className != null ? policies.stream().collect(Collectors.summingInt(p -> p.test(className, exception))) : 0;
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
            return getExceptionHandler((String) param) == null ? null : this;
        } else if (param instanceof Throwable) {
            return getExceptionHandler((Throwable) param) == null ? null : this;
        }
        throw new IllegalArgumentException(
                "ExceptionScopes can only resolve exception names: " + param);
    }

}
