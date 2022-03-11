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
package org.kie.kogito.codegen.process.openapi;

import java.util.List;
import java.util.Objects;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

/**
 * Adds {@code org.eclipse.microprofile.openapi.annotations.tags.Tag} annotations to the {@link #compilationUnit}
 */
public final class OpenApiTagGenerator {

    private final CompilationUnit compilationUnit;

    private OpenApiTagGenerator(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    /**
     * Adds {@code org.eclipse.microprofile.openapi.annotations.tags.Tag} annotations to the {@link #compilationUnit}
     * 
     * @param tags the tags to add. Each tag corresponds to a {@code org.eclipse.microprofile.openapi.annotations.tags.Tag} annotation
     * @return the {@link CompilationUnit} with the {@code org.eclipse.microprofile.openapi.annotations.tags.Tag} annotations added
     * @throws NullPointerException if {@code tags} is {@code null}
     */
    public CompilationUnit addTags(List<String> tags) {
        Objects.requireNonNull(tags, "tags cannot be null");
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> addTags(tags, cls));
        return compilationUnit;
    }

    private void addTags(List<String> tags, ClassOrInterfaceDeclaration cls) {
        tags.forEach(tag -> addTag(cls, tag));
    }

    private void addTag(ClassOrInterfaceDeclaration cls, String tag) {
        NodeList<MemberValuePair> attributes = new NodeList<>(new MemberValuePair("name", new StringLiteralExpr(tag)));
        AnnotationExpr annotationExpr = new NormalAnnotationExpr(new Name("Tag"), attributes);
        cls.addAnnotation(annotationExpr);
    }

    /**
     * Creates a new {@link OpenApiTagGenerator}
     * 
     * @param compilationUnit the compilation unit to add the {@code org.eclipse.microprofile.openapi.annotations.tags.Tag} annotations to
     * @return a new {@link OpenApiTagGenerator}
     * @throws NullPointerException if {@code compilationUnit} is {@code null}
     */
    public static OpenApiTagGenerator of(CompilationUnit compilationUnit) {
        return new OpenApiTagGenerator(Objects.requireNonNull(compilationUnit));
    }
}
