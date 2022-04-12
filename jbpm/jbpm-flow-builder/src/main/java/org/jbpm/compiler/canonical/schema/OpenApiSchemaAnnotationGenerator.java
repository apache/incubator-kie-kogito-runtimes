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

package org.jbpm.compiler.canonical.schema;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.models.media.Schema;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public final class OpenApiSchemaAnnotationGenerator {

    private OpenApiSchemaAnnotationGenerator() {
    }

    public static AnnotationExpr fromSchema(final Schema schema) {
        final NormalAnnotationExpr schemaAnnotation = new NormalAnnotationExpr();
        schemaAnnotation.setName(new Name(org.eclipse.microprofile.openapi.annotations.media.Schema.class.getCanonicalName()));
        // option not to use reflection
        schemaAnnotation.addPair("description", new StringLiteralExpr(schema.getDescription()));
        schemaAnnotation.addPair("type", SchemaType.class.getCanonicalName() + "." + schema.getType().name());
        final NodeList<Expression> requiredProperties = new NodeList<>();
        for (final String required : schema.getRequired()) {
            requiredProperties.add(new StringLiteralExpr(required));
        }
        schemaAnnotation.addPair("requiredProperties", new ArrayInitializerExpr(requiredProperties));

        final NodeList<Expression> properties = new NodeList<>();
        schema.getProperties().forEach((key, value) -> {
            final NormalAnnotationExpr schemaPropertyAnnotation = new NormalAnnotationExpr();
            schemaPropertyAnnotation.setName(new Name(SchemaProperty.class.getCanonicalName()));
            schemaPropertyAnnotation.addPair("name", new StringLiteralExpr(key));
            schemaPropertyAnnotation.addPair("type", SchemaType.class.getCanonicalName() + "." + value.getType().name());
            if (value.getRef() != null && !value.getRef().isEmpty()) {
                schemaPropertyAnnotation.addPair("ref", new StringLiteralExpr(value.getRef()));
            }
            properties.add(schemaPropertyAnnotation);
        });
        schemaAnnotation.addPair("properties", new ArrayInitializerExpr(properties));
        return schemaAnnotation;
    }

}
