/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.codegen.decision;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.junit.jupiter.api.Test;
import org.kie.api.io.ResourceType;
import org.kie.kogito.codegen.AbstractGenerator;
import org.kie.kogito.codegen.AddonsConfig;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.GeneratedFileType;
import org.kie.kogito.codegen.context.KogitoBuildContext;
import org.kie.kogito.codegen.context.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.io.CollectedResource;

import static com.github.javaparser.StaticJavaParser.parse;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecisionModelResourcesProviderGeneratorTest {

    @Test
    public void generateDecisionModelResourcesProvider() throws Exception {

        final KogitoBuildContext context = QuarkusKogitoBuildContext.builder()
                .withAddonsConfig(AddonsConfig.builder().withTracing(true).build())
                .build();

        final Collection<CollectedResource> collectedResources = CollectedResource.fromPaths(
                Paths.get("src/test/resources/decision/models/vacationDays").toAbsolutePath(),
                Paths.get("src/test/resources/decision/models/vacationDaysAlt").toAbsolutePath()
        );

        final long numberOfModels = collectedResources.stream()
                .filter(r -> r.resource().getResourceType() == ResourceType.DMN)
                .count();

        final DecisionCodegen codeGenerator = DecisionCodegen
                .ofCollectedResources(context, collectedResources);

        final List<GeneratedFile> generatedFiles = codeGenerator.generate();
        assertThat(generatedFiles.size()).isGreaterThanOrEqualTo(2); // the two resources below, see https://github.com/kiegroup/kogito-runtimes/commit/18ec525f530b1ff1bddcf18c0083f14f86aff171#diff-edd3a09d62dc627ee10fe37925944217R53

        // Align this FAI-215 test (#621) with unknown order of generated files (ie.: additional generated files might be present)
        //A Rest endpoint is always generated per model.
        List<GeneratedFile> generatedRESTFiles = generatedFiles.stream()
                .filter(gf -> gf.type().equals(AbstractGenerator.REST_TYPE))
                .collect(toList());
        assertFalse(generatedRESTFiles.isEmpty());
        assertEquals(numberOfModels, generatedRESTFiles.size());

        List<GeneratedFile> generatedCLASSFile = generatedFiles.stream()
                .filter(gf -> gf.type().equals(GeneratedFileType.SOURCE))
                .collect(toList());
        assertEquals(1, generatedCLASSFile.size());
        GeneratedFile classFile = generatedCLASSFile.get(0);
        assertEquals("org/kie/kogito/app/DecisionModelResourcesProvider.java", classFile.relativePath());

        final CompilationUnit compilationUnit = parse(new ByteArrayInputStream(classFile.contents()));

        final ClassOrInterfaceDeclaration classDeclaration = compilationUnit
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface declaration!"));

        assertNotNull(classDeclaration);

        final MethodDeclaration methodDeclaration = classDeclaration
                .findFirst(MethodDeclaration.class,
                           d -> d.getName().getIdentifier().equals("getResources"))
                .orElseThrow(() -> new NoSuchElementException("Class declaration doesn't contain a method named \"getResources\"!"));
        assertNotNull(methodDeclaration);
        assertTrue(methodDeclaration.getBody().isPresent());

        final BlockStmt body = methodDeclaration.getBody().get();
        assertTrue(body.getStatements().size() > 2);
        assertTrue(body.getStatements().get(1).isExpressionStmt());

        final ExpressionStmt expression = (ExpressionStmt) body.getStatements().get(1);
        assertTrue(expression.getExpression() instanceof MethodCallExpr);

        final MethodCallExpr call = (MethodCallExpr) expression.getExpression();
        assertEquals("add", call.getName().getIdentifier());
        assertTrue(call.getScope().isPresent());
        assertTrue(call.getScope().get().isNameExpr());

        final NameExpr nameExpr = call.getScope().get().asNameExpr();
        assertEquals("resourcePaths", nameExpr.getName().getIdentifier());

        long numberOfAddStms = body.getStatements().stream()
                .filter(this::isAddStatement)
                .count();
        assertEquals(numberOfModels, numberOfAddStms);

        List<NodeList<Expression>> defaultDecisionModelResources = body.getStatements().stream()
                .filter(this::isAddStatement)
                .map(stm -> stm.asExpressionStmt().getExpression().asMethodCallExpr().getArguments())
                .collect(toList());

        // verify .add(..) number of parameters
        defaultDecisionModelResources.forEach(nodeList -> assertEquals(1, nodeList.size()));

        Set<String> distinctDefaultDecisionModelResources = defaultDecisionModelResources.stream()
                .map(nodeList -> nodeList.get(0).toString())
                .collect(toSet());

        assertEquals(defaultDecisionModelResources.size(), distinctDefaultDecisionModelResources.size());
    }

    private boolean isAddStatement(Statement stm) {
        return stm.isExpressionStmt() &&
                stm.asExpressionStmt().getExpression().isMethodCallExpr() &&
                "add".equals(stm.asExpressionStmt().getExpression().asMethodCallExpr().getName().getIdentifier());
    }
}
