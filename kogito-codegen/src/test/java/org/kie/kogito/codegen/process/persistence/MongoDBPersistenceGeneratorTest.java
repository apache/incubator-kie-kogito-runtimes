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

package org.kie.kogito.codegen.process.persistence;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.GeneratedFile;
import org.kie.kogito.codegen.GeneratorContext;
import org.kie.kogito.codegen.context.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.data.Person;
import org.kie.kogito.codegen.di.CDIDependencyInjectionAnnotator;

import static com.github.javaparser.StaticJavaParser.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MongoDBPersistenceGeneratorTest {

    private static final String TEST_RESOURCES = "src/test/resources";
    GeneratorContext context = GeneratorContext.ofResourcePath(new File(TEST_RESOURCES));
    final Path targetDirectory = Paths.get("target");

    @Test
    void test() {
        context.withBuildContext(new QuarkusKogitoBuildContext((className -> true)));
        PersistenceGenerator persistenceGenerator = new PersistenceGenerator(targetDirectory.toFile(), Collections.singleton(Person.class), true, null, null, Arrays.asList(
                                                                                                                                                                            "com.mongodb.client.MongoClient"));
        persistenceGenerator.setPackageName(this.getClass().getPackage().getName());
        persistenceGenerator.setDependencyInjection(null);
        persistenceGenerator.setContext(context);
        persistenceGenerator.setDependencyInjection(new CDIDependencyInjectionAnnotator());
        Collection<GeneratedFile> generatedFiles = persistenceGenerator.generate();

        Optional<GeneratedFile> generatedCLASSFile = generatedFiles.stream().filter(gf -> gf.getType() == GeneratedFile.Type.CLASS).findFirst();
        assertTrue(generatedCLASSFile.isPresent());
        GeneratedFile classFile = generatedCLASSFile.get();
        assertEquals("org/kie/kogito/persistence/KogitoProcessInstancesFactoryImpl.java", classFile.relativePath());

        final CompilationUnit compilationUnit = parse(new ByteArrayInputStream(classFile.contents()));

        final ClassOrInterfaceDeclaration classDeclaration = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface declaration!"));

        assertNotNull(classDeclaration);

        final MethodDeclaration methodDeclaration = classDeclaration.findFirst(MethodDeclaration.class, d -> d.getName().getIdentifier().equals("dbName")).orElseThrow(() -> new NoSuchElementException("Class declaration doesn't contain a method named \"dbName\"!"));
        assertNotNull(methodDeclaration);
        assertTrue(methodDeclaration.getBody().isPresent());

        final BlockStmt body = methodDeclaration.getBody().get();
        assertThat(body.getStatements().size()).isOne();
        assertTrue(body.getStatements().get(0).isReturnStmt());

        final ReturnStmt returnStmt = (ReturnStmt) body.getStatements().get(0);
        assertThat(returnStmt.toString()).contains("kogito");
    }
}
