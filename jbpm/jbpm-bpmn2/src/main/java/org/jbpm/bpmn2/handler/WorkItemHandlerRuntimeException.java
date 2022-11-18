/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.bpmn2.handler;

import java.util.*;

import org.kie.api.runtime.process.WorkItemHandler;

/**
 * This exception provides extra information about the WorkItemHandler operation called to catchers of this exception.
 * It is only meant to be thrown from a {@link WorkItemHandler} instance method.
 */
public class WorkItemHandlerRuntimeException extends RuntimeException {

    /** Generated serial version uid */
    private static final long serialVersionUID = 1217036861831832336L;

    public static final String WORKITEMHANDLERTYPE = "workItemHandlerType";

    private final Map<String, Object> info;

    public WorkItemHandlerRuntimeException(Throwable cause, String message) {
        this(cause, message, Collections.emptyMap());
    }

    public WorkItemHandlerRuntimeException(Throwable cause) {
        this(cause, Collections.emptyMap());
    }

    public WorkItemHandlerRuntimeException(Throwable cause, Map<String, Object> info) {
        super(cause);
        this.info = info;
    }

    public WorkItemHandlerRuntimeException(Throwable cause, String message, Map<String, Object> info) {
        super(message, cause);
        this.info = info;
    }

    public Map<String, Object> getInformationMap() {
        return Collections.unmodifiableMap(this.info);
    }
}
