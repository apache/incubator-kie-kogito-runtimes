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
package org.kie.kogito.serverless.workflow.workitemparams;

import org.jbpm.util.ContextFactory;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.jackson.utils.JsonNodeVisitor;
import org.kie.kogito.jackson.utils.JsonObjectUtils;
import org.kie.kogito.process.expr.Expression;
import org.kie.kogito.process.expr.ExpressionHandlerFactory;
import org.kie.kogito.process.workitems.impl.WorkItemParamResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class ExpressionWorkItemResolver<T> implements WorkItemParamResolver<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionWorkItemResolver.class);

    protected final String language;
    protected final Object expression;
    private final String paramName;

    protected ExpressionWorkItemResolver(String language, Object expression, String paramName) {
        this.language = language;
        this.expression = expression;
        this.paramName = paramName;
    }

    protected final JsonNode evalExpression(KogitoWorkItem workItem) {
        return JsonNodeVisitor.transformTextNode(JsonObjectUtils.fromValue(expression), node -> transform(node, workItem.getParameter(paramName), ContextFactory.fromItem(workItem)));
    }

    private JsonNode transform(JsonNode node, Object inputModel, KogitoProcessContext context) {
        Expression expr = ExpressionHandlerFactory.get(language, node.asText());
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Expression: {}, valid: {}", expr.asString(), expr.isValid());
            }
            return expr.isValid() ? expr.eval(inputModel, JsonNode.class, context) : node;
        } catch (Exception ex) {
            logger.info("Error evaluating expression, returning original text {}", node);
            return node;
        }
    }
}
