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
package org.kie.kogito.serverless.workflow.suppliers;

import java.util.function.Supplier;

import org.jbpm.compiler.canonical.descriptors.ExpressionUtils;
import org.kogito.workitem.rest.auth.ClientOAuth2AuthDecorator;
import org.kogito.workitem.rest.auth.PasswordOAuth2AuthDecorator;

import com.github.javaparser.ast.expr.Expression;

public class PasswordOAuth2AuthDecoratorSupplier extends ClientOAuth2AuthDecorator implements Supplier<Expression> {

    private final Expression expression;

    public PasswordOAuth2AuthDecoratorSupplier(String tokenUrl, String refreshUrl) {
        super(tokenUrl, refreshUrl);
        this.expression = ExpressionUtils.getObjectCreationExpr(PasswordOAuth2AuthDecorator.class, tokenUrl, refreshUrl);
    }

    @Override
    public Expression get() {
        return expression;
    }

}
