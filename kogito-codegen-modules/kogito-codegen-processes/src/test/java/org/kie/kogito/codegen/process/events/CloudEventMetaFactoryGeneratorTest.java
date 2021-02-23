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
package org.kie.kogito.codegen.process.events;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.jbpm.compiler.canonical.TriggerMetaData;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.api.AddonsConfig;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.process.ProcessGenerationUtils;
import org.kie.kogito.event.EventKind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CloudEventMetaFactoryGeneratorTest {

    @Test
    void verifyProcessWithMessageEvent() {
        final ClassOrInterfaceDeclaration clazz = generateAndParseClass("/messageevent/IntermediateCatchEventMessage.bpmn2", 1, true);

        assertThat(clazz).isNotNull();
        assertEquals(1, clazz.getMethods().size());
        assertReturnExpressionContains(clazz.getMethods().get(0), "customers", EventKind.CONSUMED);
    }

    @Test
    void verifyProcessWithStartAndEndMessageEvent() {
        final ClassOrInterfaceDeclaration clazz = generateAndParseClass("/messagestartevent/MessageStartAndEndEvent.bpmn2", 2, true);

        assertThat(clazz).isNotNull();
        assertEquals(2, clazz.getMethods().size());
        assertReturnExpressionContains(clazz.getMethods().get(0), "customers", EventKind.CONSUMED);
        assertReturnExpressionContains(clazz.getMethods().get(1), "process.messagestartevent.processedcustomers", EventKind.PRODUCED);
    }

    @Test
    void verifyProcessWithIntermediateThrowEventMessageEvent() {
        final ClassOrInterfaceDeclaration clazz = generateAndParseClass("/messageevent/IntermediateThrowEventMessage.bpmn2", 1, true);

        assertThat(clazz).isNotNull();
        assertEquals(1, clazz.getMethods().size());
        assertReturnExpressionContains(clazz.getMethods().get(0), "process.messageintermediateevent.customers", EventKind.PRODUCED);
    }

    @Test
    void verifyProcessWithBoundaryEventMessageEvent() {
        final ClassOrInterfaceDeclaration clazz = generateAndParseClass("/messageevent/BoundaryMessageEventOnTask.bpmn2", 1, true);

        assertThat(clazz).isNotNull();
        assertEquals(1, clazz.getMethods().size());
        assertReturnExpressionContains(clazz.getMethods().get(0), "customers", EventKind.CONSUMED);
    }

    @Test
    void verifyProcessWithoutMessageEvent() {
        final ClassOrInterfaceDeclaration clazz = generateAndParseClass("/usertask/approval.bpmn2", 0, true);

        assertThat(clazz).isNotNull();
        assertTrue(clazz.getMethods().isEmpty());
    }

    private void assertReturnExpressionContains(MethodDeclaration method, String expectedType, EventKind expectedKind) {
        Optional<String> optExpr = method.getBody()
                .map(BlockStmt::getStatements)
                .filter(stmtList -> stmtList.size() == 1)
                .map(stmtList -> stmtList.get(0))
                .filter(Statement::isReturnStmt)
                .map(Statement::asReturnStmt)
                .flatMap(ReturnStmt::getExpression)
                .map(Expression::toString);

        System.err.println(optExpr);

        assertTrue(
                optExpr.filter(str -> str.contains(String.format("\"%s\"", expectedType))).isPresent(),
                () -> String.format("Method %s doesn't contain \"%s\" as event type", method.getName(), expectedType)
        );
        assertTrue(
                optExpr.filter(str -> str.contains(String.format("%s.%s", EventKind.class.getName(), expectedKind.name()))).isPresent(),
                () -> String.format("Method %s doesn't contain %s as event kind", method.getName(), expectedKind.name())
        );
    }

    private ClassOrInterfaceDeclaration generateAndParseClass(String bpmnFile, int expectedTriggers, boolean withInjection) {
        KogitoBuildContext context = (withInjection ?
                QuarkusKogitoBuildContext.builder() :
                JavaKogitoBuildContext.builder())
                .withAddonsConfig(AddonsConfig.builder().withCloudEvents(true).build())
                .build();

        final CloudEventMetaFactoryGenerator generator =
                new CloudEventMetaFactoryGenerator(
                        context,
                        ProcessGenerationUtils.execModelFromProcessFile(bpmnFile));
        if (expectedTriggers > 0) {
            assertThat(generator.getTriggers()).isNotEmpty();
            int triggersCount = 0;
            for (Map.Entry<String, List<TriggerMetaData>> entry : generator.getTriggers().entrySet()) {
                triggersCount += entry.getValue().size();
            }
            assertThat(triggersCount).isEqualTo(expectedTriggers);
        } else {
            assertThat(generator.getTriggers()).isEmpty();
        }
        final String source = generator.generate();
        assertThat(source).isNotNull();
        final ClassOrInterfaceDeclaration clazz = StaticJavaParser
                .parse(source)
                .getClassByName(generator.getClassName())
                .orElseThrow(() -> new IllegalArgumentException("Class does not exists"));
        return clazz;
    }
}