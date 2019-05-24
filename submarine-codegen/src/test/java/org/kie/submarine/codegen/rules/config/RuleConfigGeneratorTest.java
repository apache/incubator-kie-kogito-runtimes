/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.submarine.codegen.rules.config;

import com.github.javaparser.ast.expr.ObjectCreationExpr;
import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.drools.core.config.StaticRuleConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RuleConfigGeneratorTest {

    @Test
    public void ruleEventListenersConfig() {
        final RuleConfigGenerator configGenerator = new RuleConfigGenerator();
        assertThat(configGenerator.ruleEventListenersConfig("test")).isSameAs(configGenerator);
    }

    @Test
    public void ruleEventListenersConfigWithNull() {
        final RuleConfigGenerator configGenerator = new RuleConfigGenerator();
        assertThatThrownBy(() -> configGenerator.ruleEventListenersConfig(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void newInstance() {
        final RuleConfigGenerator configGenerator = new RuleConfigGenerator();
        final ObjectCreationExpr ruleConfigExpression = configGenerator.newInstance();

        assertObjectCreationExpr(ruleConfigExpression, StaticRuleConfig.class);
        assertThat(ruleConfigExpression.getArguments()).hasSize(1);
        assertThat(ruleConfigExpression.getArguments().get(0)).isInstanceOf(ObjectCreationExpr.class);

        final ObjectCreationExpr argument = (ObjectCreationExpr) ruleConfigExpression.getArguments().get(0);
        assertObjectCreationExpr(argument, DefaultRuleEventListenerConfig.class);
    }

    private void assertObjectCreationExpr(final ObjectCreationExpr expression, final Class expectedClass) {
        assertThat(expression).isNotNull();
        assertThat(expression.getType().getScope()).isPresent();
        assertThat(expression.getType().getScope().get().toString()).isEqualTo(expectedClass.getPackage().getName());
        assertThat(expression.getType().getName().asString()).isEqualTo(expectedClass.getSimpleName());
    }
}