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

public class RootCauseExceptionPolicy extends AbstractHierarchyExceptionPolicy {
    @Override
    protected int count(String errorCode, Throwable exception, int step) {
        Class<?> exceptionClass = exception.getClass();
        int count = isException(errorCode, exceptionClass);
        if (count == 0) {
            exceptionClass = exceptionClass.getSuperclass();
            for (int divisor = 2; count == 0 && !exceptionClass.equals(Object.class); divisor++, exceptionClass = exceptionClass.getSuperclass()) {
                count = isException(errorCode, exceptionClass) / divisor;
            }
        }
        return count / step;
    }

    private int isException(String errorCode, Class<?> exceptionClass) {
        return errorCode.equals(exceptionClass.getName()) ? FULL_VALUE : 0;
    }
}
