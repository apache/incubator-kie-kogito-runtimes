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

package org.kie.kogito.codegen.decision.events;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.dmn.api.core.DMNModel;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.template.InvalidTemplateException;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.core.CodegenUtils;
import org.kie.kogito.event.EventKind;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.printer.YamlPrinter;

public class CloudEventMetaFactoryGenerator {

    public static final String RESPONSE_EVENT_TYPE = "DecisionResponse";
    public static final String RESPONSE_FULL_EVENT_TYPE = "DecisionResponseFull";
    public static final String RESPONSE_ERROR_EVENT_TYPE = "DecisionResponseError";

    public static final String TEMPLATE_EVENT_FOLDER = "/class-templates/events/";
    private static final String CLASS_NAME = "CloudEventMetaFactory";

    private final KogitoBuildContext context;
    private final TemplatedGenerator generator;
    private final List<DMNModel> models;

    public CloudEventMetaFactoryGenerator(final KogitoBuildContext context, List<DMNModel> models) {
        this.context = context;
        this.generator = buildTemplatedGenerator(context);
        this.models = models;
    }

    public final String generatedFilePath() {
        return generator.generatedFilePath();
    }

    public String generate() {
        CompilationUnit compilationUnit = generator.compilationUnitOrThrow("Cannot generate CloudEventMetaFactory");

        ClassOrInterfaceDeclaration classDefinition = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new InvalidTemplateException(generator, "Compilation unit doesn't contain a class or interface declaration!"));

        classDefinition.setName("Decision" + CLASS_NAME);

        MethodDeclaration templatedBuildMethod = classDefinition
                .findFirst(MethodDeclaration.class, x -> x.getName().toString().startsWith("buildCloudEventMeta_$methodName$"))
                .orElseThrow(() -> new InvalidTemplateException(generator, "Impossible to find expected buildCloudEventMeta_ method"));

        List<CloudEventMetaMethodData> methodDataList = models.stream()
                .flatMap(CloudEventMetaFactoryGenerator::buildMethodDataStreamFromModel)
                .distinct()
                .collect(Collectors.toList());

        methodDataList.forEach(methodData -> {
            MethodDeclaration builderMethod = templatedBuildMethod.clone();

            String methodNameValue = String.format("%s_%s", methodData.kind.name(), methodData.methodNameChunk);
            String builderMethodName = getBuilderMethodName(classDefinition, templatedBuildMethod.getNameAsString(), methodNameValue);
            builderMethod.setName(builderMethodName);

            Map<String, Expression> expressions = new HashMap<>();
            expressions.put("$type$", new StringLiteralExpr(methodData.type));
            expressions.put("$source$", new StringLiteralExpr(methodData.source));
            expressions.put("$kind$", new FieldAccessExpr(new NameExpr(new SimpleName(EventKind.class.getName())), methodData.kind.name()));

            YamlPrinter printer = new YamlPrinter(true);
            System.out.println(printer.output(builderMethod));

            builderMethod.findFirst(MethodCallExpr.class)
                    .ifPresent(callExpr -> CodegenUtils.interpolateArguments(callExpr, expressions));

            classDefinition.addMember(builderMethod);
        });

        templatedBuildMethod.remove();

        if (context.hasDI()) {
            context.getDependencyInjectionAnnotator().withFactoryClass(classDefinition);
            classDefinition.findAll(FieldDeclaration.class, CodegenUtils::isConfigBeanField)
                    .forEach(fd -> context.getDependencyInjectionAnnotator().withInjection(fd));
            classDefinition.findAll(MethodDeclaration.class, x -> x.getName().toString().startsWith("buildCloudEventMeta_"))
                    .forEach(md -> context.getDependencyInjectionAnnotator().withFactoryMethod(md));
        }

        return compilationUnit.toString();
    }

    static Stream<CloudEventMetaMethodData> buildMethodDataStreamFromModel(DMNModel model) {
        String source = Optional.of(model.getName())
                .filter(s -> !s.isEmpty())
                .map(CloudEventMetaFactoryGenerator::urlEncodedStringFrom)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse("");

        Stream<CloudEventMetaMethodData> modelStream = Stream.of(
                buildMethodDataFromModel(RESPONSE_EVENT_TYPE, source, model.getName()),
                buildMethodDataFromModel(RESPONSE_FULL_EVENT_TYPE, source, model.getName()),
                buildMethodDataFromModel(RESPONSE_ERROR_EVENT_TYPE, source, model.getName()));

        Stream<CloudEventMetaMethodData> decisionServiceStream = model.getDecisionServices().stream()
                .flatMap(ds -> buildMethodDataStreamFromDecisionService(model, ds.getName()));

        return Stream.concat(modelStream, decisionServiceStream);
    }

    static CloudEventMetaMethodData buildMethodDataFromModel(String type, String source, String modelName) {
        return new CloudEventMetaMethodData(type, source, EventKind.PRODUCED, buildMethodNameChunk(type, modelName, null));
    }

    static Stream<CloudEventMetaMethodData> buildMethodDataStreamFromDecisionService(DMNModel model, String decisionServiceName) {
        String source = Stream.of(model.getName(), decisionServiceName)
                .filter(s -> s != null && !s.isEmpty())
                .map(CloudEventMetaFactoryGenerator::urlEncodedStringFrom)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.joining("/"));

        return Stream.of(
                buildMethodDataFromDecisionService(RESPONSE_EVENT_TYPE, source, model.getName(), decisionServiceName),
                buildMethodDataFromDecisionService(RESPONSE_FULL_EVENT_TYPE, source, model.getName(), decisionServiceName),
                buildMethodDataFromDecisionService(RESPONSE_ERROR_EVENT_TYPE, source, model.getName(), decisionServiceName));
    }

    static CloudEventMetaMethodData buildMethodDataFromDecisionService(String type, String source, String modelName, String decisionServiceName) {
        return new CloudEventMetaMethodData(type, source, EventKind.PRODUCED, buildMethodNameChunk(type, modelName, decisionServiceName));
    }

    static String buildMethodNameChunk(String type, String modelName, String decisionServiceName) {
        return Stream.of(EventKind.PRODUCED.name(), type, modelName, decisionServiceName)
                .filter(s -> s != null && !s.isEmpty())
                .map(CloudEventMetaFactoryGenerator::toValidJavaIdentifier)
                .collect(Collectors.joining("_"));
    }

    static TemplatedGenerator buildTemplatedGenerator(KogitoBuildContext context) {
        return TemplatedGenerator.builder()
                .withTemplateBasePath(TEMPLATE_EVENT_FOLDER)
                .withTargetTypeName("Decision" + CLASS_NAME)
                .withFallbackContext(JavaKogitoBuildContext.CONTEXT_NAME)
                .build(context, CLASS_NAME);
    }

    static String getBuilderMethodName(ClassOrInterfaceDeclaration classDefinition, String templatedBuildMethodName, String methodNameValue) {
        String baseMethodName = templatedBuildMethodName.replace("$methodName$", methodNameValue);
        List<MethodDeclaration> methods = classDefinition.findAll(MethodDeclaration.class);
        int counter = 0;
        while (true) {
            String expectedMethodName = counter == 0
                    ? baseMethodName
                    : String.format("%s_%d", baseMethodName, counter);
            if (methods.stream().anyMatch(m -> m.getNameAsString().equals(expectedMethodName))) {
                counter++;
            } else {
                return expectedMethodName;
            }
        }
    }

    static String toValidJavaIdentifier(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            if (c == '_') {
                sb.append("__");
            } else if (!Character.isJavaIdentifierPart(c)) {
                sb.append("_").append(Integer.valueOf(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static Optional<String> urlEncodedStringFrom(String input) {
        return Optional.ofNullable(input)
                .map(i -> {
                    try {
                        return URLEncoder.encode(i, StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static class CloudEventMetaMethodData {

        final String type;
        final String source;
        final EventKind kind;
        final String methodNameChunk;

        public CloudEventMetaMethodData(String type, String source, EventKind kind, String methodNameChunk) {
            this.type = type;
            this.source = source;
            this.kind = kind;
            this.methodNameChunk = methodNameChunk;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CloudEventMetaMethodData that = (CloudEventMetaMethodData) o;
            return type.equals(that.type) && source.equals(that.source) && kind == that.kind;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, source, kind);
        }
    }
}
