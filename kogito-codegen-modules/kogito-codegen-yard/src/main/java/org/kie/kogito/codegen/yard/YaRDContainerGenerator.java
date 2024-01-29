/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.codegen.yard;

import java.util.Collection;
import java.util.stream.Collectors;

import org.kie.dmn.feel.codegen.feel11.CodegenStringUtil;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.template.InvalidTemplateException;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

public class YaRDContainerGenerator implements ApplicationSection {

    public static final String SECTION_CLASS_NAME = "YaRDRuntime";
    private static final String ADD_CONTENT_STATEMENT = "add( readResource( \"$content$\"  ) );";
    private final Collection<YaRDResource> resources;
    private final TemplatedGenerator templatedGenerator;

    public YaRDContainerGenerator(
            final KogitoBuildContext context,
            final Collection<YaRDResource> resources) {
        this.templatedGenerator = TemplatedGenerator.builder()
                .withTargetTypeName(SECTION_CLASS_NAME)
                .build(context, "YaRDContainer");
        this.resources = resources;
    }

    private static Statement toLoadStatement(final YaRDResource resource) {
        final String path = CodegenStringUtil.escapeIdentifier(resource.getModel().getName()) + ".yard.yml";
        final String rawStatement = ADD_CONTENT_STATEMENT
                .replace("$content$", path);
        return StaticJavaParser.parseStatement(rawStatement);
    }

    @Override
    public String sectionClassName() {
        return SECTION_CLASS_NAME;
    }

    @Override
    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit = templatedGenerator.compilationUnitOrThrow();
        MethodDeclaration loadContent = compilationUnit.findFirst(MethodDeclaration.class, md -> "loadContent".equals(md.getName().asString()))
                .orElseThrow(() -> new InvalidTemplateException(templatedGenerator, "Impossible to find method loadContent"));

        BlockStmt loadContentBlock = loadContent.getBody()
                .orElseThrow(() -> new InvalidTemplateException(templatedGenerator, "loadContent method must have a body"));

        Collection<Statement> loadStatements = resources.stream().map(YaRDContainerGenerator::toLoadStatement).collect(Collectors.toList());

        loadContentBlock.getStatements().addAll(loadStatements);

        return compilationUnit;
    }
}
