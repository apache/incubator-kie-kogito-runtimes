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
package org.kogito.workitem.openapi.suppliers;

import java.util.function.Supplier;

import org.kogito.workitem.openapi.JsonNodeParameterResolver;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class JsonNodeParameterExprSupplier implements Supplier<Expression> {

    private final String parameterDefinition;

    public JsonNodeParameterExprSupplier(final String parameterDefinition) {
        this.parameterDefinition = parameterDefinition;
    }

    @Override
    public Expression get() {
        return new ObjectCreationExpr()
                .setType(JsonNodeParameterResolver.class.getCanonicalName())
                .addArgument(new StringLiteralExpr(this.parameterDefinition));
    }
}
