/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

public abstract class AbstractHierarchyExceptionPolicy implements ExceptionHandlerPolicy {

    protected static final int FULL_VALUE = 200;

    @Override
    public int test(String errorCode, Throwable exception) {
        int count = count(errorCode, exception, 1);
        if (count == 0) {
            Throwable rootCause = exception.getCause();
            for (int step = 2; count == 0 && rootCause != null; step++) {
                count = count(errorCode, exception, step);
                rootCause = rootCause.getCause();
            }
        }
        return count;
    }

    protected int count(String errorCode, Throwable exception, int step) {
        return verify(errorCode, exception) ? FULL_VALUE - step : 0;
    }

    protected boolean verify(String errorCode, Throwable exception) {
        return false;
    }

}
