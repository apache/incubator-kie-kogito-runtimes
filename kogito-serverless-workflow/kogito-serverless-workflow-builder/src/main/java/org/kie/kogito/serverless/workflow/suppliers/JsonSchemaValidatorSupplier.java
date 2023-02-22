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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jbpm.compiler.canonical.descriptors.ExpressionUtils;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;
import org.kie.kogito.serverless.workflow.actions.JsonSchemaValidator;
import org.kie.kogito.serverless.workflow.parser.SwaggerSchemaGenerator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.media.Discriminator;

public class JsonSchemaValidatorSupplier extends JsonSchemaValidator implements Supplier<Expression>, SwaggerSchemaGenerator {

    private static final long serialVersionUID = 1L;

    private Expression content;

    public JsonSchemaValidatorSupplier(String schema, boolean failOnValidationErrors) {
        super(schema, failOnValidationErrors);
    }

    @Override
    public Expression get() {
        return ExpressionUtils.getObjectCreationExpr(JsonSchemaValidator.class, schemaRef, failOnValidationErrors);
    }

    @Override
    public Expression getContent() throws IOException {

        if (content == null) {
            Expression schemaAnnotation = getSchema(ObjectMapperFactory.get().convertValue(load().toString(), new TypeReference<io.swagger.v3.oas.models.media.Schema<String>>() {
            }));

        }
        return content;

    }

    public Expression getSchema(io.swagger.v3.oas.models.media.Schema<String> schemaObject) {
        final NormalAnnotationExpr schemaAnnotation = new NormalAnnotationExpr();
        schemaAnnotation.setName(new Name(Schema.class.getCanonicalName()));
        addPair(schemaAnnotation, "name", schemaObject.getName(), StringLiteralExpr::new);
        addPair(schemaAnnotation, "title", schemaObject.getTitle(), StringLiteralExpr::new);
        addPair(schemaAnnotation, "multipleOf", schemaObject.getMultipleOf(), v -> new DoubleLiteralExpr(v.doubleValue()));
        addPair(schemaAnnotation, "maximum", schemaObject.getMaximum(), this::from);
        addPair(schemaAnnotation, "exclusiveMaximum", schemaObject.getExclusiveMaximum(), BooleanLiteralExpr::new);
        addPair(schemaAnnotation, "multipleOf", schemaObject.getMultipleOf(), this::from);
        addPair(schemaAnnotation, "minimun", schemaObject.getMinimum(), this::from);
        addPair(schemaAnnotation, "exclusiveMinimum", schemaObject.getExclusiveMinimum(), BooleanLiteralExpr::new);
        addPair(schemaAnnotation, "maxLength", schemaObject.getMaxLength(), IntegerLiteralExpr::new);
        addPair(schemaAnnotation, "minLength", schemaObject.getMinLength(), IntegerLiteralExpr::new);
        addPair(schemaAnnotation, "pattern", schemaObject.getPattern(), StringLiteralExpr::new);
        addPair(schemaAnnotation, "maxProperties", schemaObject.getMaxProperties(), IntegerLiteralExpr::new);
        addPair(schemaAnnotation, "minProperties", schemaObject.getMinProperties(), IntegerLiteralExpr::new);
        addPair(schemaAnnotation, "requiredProperties", schemaObject.getRequired(), this::from);
        addPair(schemaAnnotation, "description", schemaObject.getDescription(), StringLiteralExpr::new);
        addPair(schemaAnnotation, "format", schemaObject.getFormat(), StringLiteralExpr::new);
        addPair(schemaAnnotation, "ref", schemaObject.get$ref(), StringLiteralExpr::new);
        addPair(schemaAnnotation, "nullable", schemaObject.getNullable(), BooleanLiteralExpr::new);
        addPair(schemaAnnotation, "readOnly", schemaObject.getReadOnly(), BooleanLiteralExpr::new);
        addPair(schemaAnnotation, "writeOnly", schemaObject.getWriteOnly(), BooleanLiteralExpr::new);
        addPair(schemaAnnotation, "example", schemaObject.getExample(), this::from);
        addPair(schemaAnnotation, "externalDocs", schemaObject.getExternalDocs(), this::from);
        addPair(schemaAnnotation, "deprecated", schemaObject.getDeprecated(), BooleanLiteralExpr::new);
        addPair(schemaAnnotation, "type", schemaObject.getType(), this::from);
        addPair(schemaAnnotation, "defaultValue", schemaObject.getDefault(), this::from);
        Discriminator discriminator = schemaObject.getDiscriminator();
        if (discriminator != null) {
            addPair(schemaAnnotation, "discriminatorProperty", discriminator.getPropertyName(), StringLiteralExpr::new);
            addPair(schemaAnnotation, "discriminatorMapping", discriminator.getMapping(), this::getMapping);
        }
        addPair(schemaAnnotation, "allowableValues", schemaObject.getEnum(), this::from);
        addPair(schemaAnnotation, "extensions", schemaObject.getExtensions(), this::getExtensions);
        return schemaAnnotation;
    }

    private StringLiteralExpr from(Object b) {
        return new StringLiteralExpr(b.toString());
    }

    private Expression from(Collection<String> collection) {
        //TODO return string[]
        return null;
    }

    private Expression getMapping(Map<String, String> mapping) {
        //TODO return DiscriminatorMapping array
        return null;
    }

    private Expression getExtensions(Map<String, Object> extensions) {
        //TODO return Extension  array
        return null;
    }

    private Expression from(io.swagger.v3.oas.models.ExternalDocumentation documentation) {
        final NormalAnnotationExpr schemaAnnotation = new NormalAnnotationExpr();
        schemaAnnotation.setName(new Name(ExternalDocumentation.class.getCanonicalName()));
        //TODO fill sshema for ExternalDocumentation
        return schemaAnnotation;
    }

    private static <T> void addPair(NormalAnnotationExpr annot, String name, T value, Function<T, Expression> convert) {
        if (value != null) {
            annot.addPair(name, convert.apply(value));
        }
    }

}
