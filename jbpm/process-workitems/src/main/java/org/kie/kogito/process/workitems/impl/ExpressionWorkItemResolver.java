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
package org.kie.kogito.process.workitems.impl;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;

public abstract class ExpressionWorkItemResolver implements WorkItemParamResolver {

    protected final String expression;
    private final String paramName;

    protected ExpressionWorkItemResolver(String expression, String paramName) {
        this.expression = expression;
        this.paramName = paramName;
    }

    @Override
    public Object apply(KogitoWorkItem workItem) {
        return evalExpression(workItem.getParameter(paramName));

    }

    protected abstract Object evalExpression(Object inputModel);
}
