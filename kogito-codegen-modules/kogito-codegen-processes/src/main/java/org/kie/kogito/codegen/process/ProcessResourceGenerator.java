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
package org.kie.kogito.codegen.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.drools.core.util.StringUtils;
import org.jbpm.compiler.canonical.ProcessToExecModelGenerator;
import org.jbpm.compiler.canonical.UserTaskModelMetaData;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.QuarkusKogitoBuildContext;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.core.BodyDeclarationComparator;
import org.kie.kogito.codegen.core.CodegenUtils;
import org.kie.kogito.codegen.core.GeneratorConfig;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import static com.github.javaparser.StaticJavaParser.parse;
import static org.drools.core.util.StringUtils.ucFirst;
import static org.kie.kogito.codegen.core.CodegenUtils.interpolateTypes;

/**
 * ProcessResourceGenerator
 */
public class ProcessResourceGenerator {

    private static final String REST_TEMPLATE_NAME = "RestResource";
    private static final String REACTIVE_REST_TEMPLATE_NAME = "ReactiveRestResource";
    private static final String REST_USER_TASK_TEMPLATE_NAME = "RestResourceUserTask";
    private static final String REST_SIGNAL_TEMPLATE_NAME = "RestResourceSignal";

    private final String relativePath;

    private final KogitoBuildContext context;
    private final String resourceClazzName;
    private final String processClazzName;
    private final String processName;
    private final String appCanonicalName;
    private KogitoWorkflowProcess process;
    private String processId;
    private String dataClazzName;
    private String modelfqcn;

    private boolean startable;
    private boolean dynamic;
    private List<UserTaskModelMetaData> userTasks;
    private Map<String, String> signals;
    private CompilationUnit taskModelFactoryUnit;
    private String taskModelFactoryClassName;

    public ProcessResourceGenerator(
            KogitoBuildContext context,
            KogitoWorkflowProcess process,
            String modelfqcn,
            String processfqcn,
            String appCanonicalName) {
        this.context = context;
        this.process = process;
        this.processId = process.getId();
        this.processName = processId.substring(processId.lastIndexOf('.') + 1);
        this.appCanonicalName = appCanonicalName;
        String classPrefix = StringUtils.ucFirst(processName);
        this.resourceClazzName = classPrefix + "Resource";
        this.relativePath = process.getPackageName().replace(".", "/") + "/" + resourceClazzName + ".java";
        this.modelfqcn = modelfqcn + "Output";
        this.dataClazzName = modelfqcn.substring(modelfqcn.lastIndexOf('.') + 1);
        this.processClazzName = processfqcn;
    }

    public ProcessResourceGenerator withUserTasks(List<UserTaskModelMetaData> userTasks) {
        this.userTasks = userTasks;
        return this;
    }

    public ProcessResourceGenerator withSignals(Map<String, String> signals) {
        this.signals = signals;
        return this;
    }

    public ProcessResourceGenerator withTriggers(boolean startable, boolean dynamic) {
        this.startable = startable;
        this.dynamic = dynamic;
        return this;
    }

    public String getTaskModelFactory() {
        return taskModelFactoryUnit.toString();
    }

    public String getTaskModelFactoryClassName() {
        return taskModelFactoryClassName;
    }

    public String className() {
        return resourceClazzName;
    }

    protected String getRestTemplateName() {
        boolean isReactiveGenerator = "reactive".equals(context.getApplicationProperty(GeneratorConfig.KOGITO_REST_RESOURCE_TYPE_PROP)
                .orElse(""));
        boolean isQuarkus = context.name().equals(QuarkusKogitoBuildContext.CONTEXT_NAME);

        return isQuarkus && isReactiveGenerator ? REACTIVE_REST_TEMPLATE_NAME : REST_TEMPLATE_NAME;
    }

    public String generate() {
        TemplatedGenerator.Builder templateBuilder = TemplatedGenerator.builder()
                .withFallbackContext(QuarkusKogitoBuildContext.CONTEXT_NAME);
        CompilationUnit clazz = templateBuilder.build(context, getRestTemplateName())
                .compilationUnitOrThrow();
        clazz.setPackageDeclaration(process.getPackageName());
        clazz.addImport(modelfqcn);

        ClassOrInterfaceDeclaration template = clazz
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface declaration!"));

        template.setName(resourceClazzName);
        AtomicInteger index = new AtomicInteger(0);
        //Generate signals endpoints
        Optional.ofNullable(signals)
                .ifPresent(signalsMap -> {
                    //using template class to the endpoints generation
                    CompilationUnit signalClazz = templateBuilder.build(context, REST_SIGNAL_TEMPLATE_NAME)
                            .compilationUnitOrThrow();

                    ClassOrInterfaceDeclaration signalTemplate = signalClazz
                            .findFirst(ClassOrInterfaceDeclaration.class)
                            .orElseThrow(() -> new NoSuchElementException("SignalResourceTemplate class not found!"));

                    signalsMap.entrySet()
                            .stream()
                            .filter(e -> Objects.nonNull(e.getKey()))
                            .forEach(entry -> {
                                String methodName = "signal_" + index.getAndIncrement();
                                String outputType = modelfqcn;
                                String signalName = entry.getKey();
                                String signalType = entry.getValue();

                                signalTemplate.findAll(MethodDeclaration.class)
                                        .forEach(md -> {
                                            MethodDeclaration cloned = md.clone();
                                            BlockStmt body = cloned.getBody().get();
                                            if (signalType == null) {
                                                body.findAll(NameExpr.class, nameExpr -> "data".equals(nameExpr.getNameAsString())).forEach(name -> name.replace(new NullLiteralExpr()));
                                            }
                                            template.addMethod(methodName, Keyword.PUBLIC)
                                                    .setType(outputType)
                                                    // Remove data parameter ( payload ) if signalType is null 
                                                    .setParameters(signalType == null ? NodeList.nodeList(cloned.getParameter(0)) : cloned.getParameters())
                                                    .setBody(body)
                                                    .setAnnotations(cloned.getAnnotations());
                                        });

                                if (signalType != null) {
                                    template.findAll(ClassOrInterfaceType.class).forEach(name -> {
                                        String identifier = name.getNameAsString();
                                        name.setName(identifier.replace("$signalType$", signalType));
                                    });
                                }

                                template.findAll(StringLiteralExpr.class).forEach(vv -> {
                                    String s = vv.getValue();
                                    String interpolated = s.replace("$signalName$", signalName);
                                    interpolated = interpolated.replace("$signalPath$", sanitizeName(signalName));
                                    vv.setString(interpolated);
                                });
                            });
                });

        // security must be applied before user tasks are added to make sure that user task
        // endpoints are not security annotated as they should restrict access based on user assignments
        securityAnnotated(template);

        Map<String, String> typeInterpolations = new HashMap<>();
        taskModelFactoryUnit = parse(this.getClass().getResourceAsStream("/class-templates/TaskModelFactoryTemplate.java"));
        String taskModelFactorySimpleClassName = ucFirst(ProcessToExecModelGenerator.extractProcessId(processId) + "_" + "TaskModelFactory");
        taskModelFactoryUnit.setPackageDeclaration(process.getPackageName());
        taskModelFactoryClassName = process.getPackageName() + "." + taskModelFactorySimpleClassName;
        ClassOrInterfaceDeclaration taskModelFactoryClass = taskModelFactoryUnit.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow(IllegalStateException::new);
        taskModelFactoryClass.setName(taskModelFactorySimpleClassName);
        typeInterpolations.put("$TaskModelFactory$", taskModelFactoryClassName);

        if (userTasks != null && !userTasks.isEmpty()) {

            CompilationUnit userTaskClazz = templateBuilder.build(context, REST_USER_TASK_TEMPLATE_NAME).compilationUnitOrThrow();

            ClassOrInterfaceDeclaration userTaskTemplate = userTaskClazz
                    .findFirst(ClassOrInterfaceDeclaration.class)
                    .orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface declaration!"));

            MethodDeclaration taskModelFactoryMethod = taskModelFactoryClass
                    .findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("from"))
                    .orElseThrow(IllegalStateException::new);
            SwitchStmt switchExpr = taskModelFactoryMethod.getBody().map(b -> b.findFirst(SwitchStmt.class).orElseThrow(IllegalStateException::new)).orElseThrow(IllegalStateException::new);

            for (UserTaskModelMetaData userTask : userTasks) {
                String methodSuffix = sanitizeName(userTask.getName()) + "_" + index.getAndIncrement();
                userTaskTemplate.findAll(MethodDeclaration.class).forEach(md -> {
                    MethodDeclaration cloned = md.clone();
                    template.addMethod(cloned.getName() + "_" + methodSuffix, Keyword.PUBLIC)
                            .setType(cloned.getType())
                            .setParameters(cloned.getParameters())
                            .setBody(cloned.getBody().get())
                            .setAnnotations(cloned.getAnnotations());
                });

                template.findAll(StringLiteralExpr.class).forEach(s -> interpolateUserTaskStrings(s, userTask));
                template.findAll(ClassOrInterfaceType.class).forEach(c -> interpolateUserTaskTypes(c, userTask));
                template.findAll(NameExpr.class).forEach(c -> interpolateUserTaskNameExp(c, userTask));
                if (!userTask.isAdHoc()) {
                    template.findAll(MethodDeclaration.class)
                            .stream()
                            .filter(md -> md.getNameAsString().equals("signal_" + methodSuffix))
                            .collect(Collectors.toList()).forEach(template::remove);
                }
                switchExpr.getEntries().add(0, userTask.getModelSwitchEntry());
            }

        }

        typeInterpolations.put("$Clazz$", resourceClazzName);
        typeInterpolations.put("$Type$", dataClazzName);
        template.findAll(StringLiteralExpr.class).forEach(this::interpolateStrings);
        template.findAll(ClassOrInterfaceType.class).forEach(cls -> interpolateTypes(cls, typeInterpolations));
        template.findAll(MethodDeclaration.class).forEach(this::interpolateMethods);

        if (context.hasDI()) {
            //            template.findAll(FieldDeclaration.class,
            //                    CodegenUtils::isProcessField).forEach(fd -> context.getDependencyInjectionAnnotator().withNamedInjection(fd, processId));
        } else {
            template.findAll(FieldDeclaration.class,
                    CodegenUtils::isProcessField).forEach(this::initializeProcessField);
        }

        // if triggers are not empty remove createResource method as there is another trigger to start process instances
        if ((!startable && !dynamic) || !isPublic()) {
            Optional<MethodDeclaration> createResourceMethod = template.findFirst(MethodDeclaration.class).filter(md -> md.getNameAsString().equals("createResource_" + processName));
            createResourceMethod.ifPresent(template::remove);
        }

        if (context.hasDI()) {
            context.getDependencyInjectionAnnotator().withApplicationComponent(template);
        }

        enableValidation(template);

        template.getMembers().sort(new BodyDeclarationComparator());
        return clazz.toString();
    }

    private void securityAnnotated(ClassOrInterfaceDeclaration template) {
        if (context.hasDI() && process.getMetaData().containsKey("securityRoles")) {
            String[] roles = ((String) process.getMetaData().get("securityRoles")).split(",");
            template.findAll(MethodDeclaration.class).stream().filter(context.getRestAnnotator()::isRestAnnotated)
                    .forEach(md -> context.getDependencyInjectionAnnotator().withSecurityRoles(md, roles));
        }
    }

    private void enableValidation(ClassOrInterfaceDeclaration template) {
        Optional.ofNullable(context)
                .filter(KogitoBuildContext::isValidationSupported)
                .ifPresent(c -> template.findAll(Parameter.class)
                        .stream()
                        .filter(param -> param.getTypeAsString().equals(dataClazzName + "Input"))
                        .forEach(this::insertValidationAnnotations));
    }

    private void insertValidationAnnotations(Parameter param) {
        param.addAnnotation("javax.validation.Valid");
        param.addAnnotation("javax.validation.constraints.NotNull");
    }

    private void initializeProcessField(FieldDeclaration fd) {
        fd.getVariable(0).setInitializer(new ObjectCreationExpr().setType(processClazzName));
    }

    private void interpolateStrings(StringLiteralExpr vv) {
        String s = vv.getValue();
        String documentation =
                process.getMetaData()
                        .getOrDefault("Documentation", processName).toString();
        String interpolated =
                s.replace("$name$", processName)
                        .replace("$id$", processId)
                        .replace("$documentation$", documentation);
        vv.setString(interpolated);
    }

    private void interpolateUserTaskStrings(StringLiteralExpr vv, UserTaskModelMetaData userTask) {
        String s = vv.getValue();
        String interpolated = s.replace("$taskName$", sanitizeName(userTask.getName()));
        interpolated = interpolated.replace("$taskNodeName$", userTask.getNodeName());
        vv.setString(interpolated);
    }

    private void interpolateUserTaskNameExp(NameExpr name, UserTaskModelMetaData userTask) {
        name.setName(userTask.templateReplacement(name.getNameAsString()));
    }

    private void interpolateMethods(MethodDeclaration m) {
        SimpleName methodName = m.getName();
        String interpolated =
                methodName.asString().replace("$name$", processName);
        m.setName(interpolated);
    }

    private void interpolateUserTaskTypes(Type t, UserTaskModelMetaData userTask) {
        if (t.isArrayType()) {
            t = t.asArrayType().getElementType();
        }
        if (t.isClassOrInterfaceType()) {
            SimpleName returnType = t.asClassOrInterfaceType().getName();
            interpolateUserTaskTypes(returnType, userTask);
            t.asClassOrInterfaceType().getTypeArguments().ifPresent(o -> interpolateUserTaskTypeArguments(o, userTask));
        }
    }

    private void interpolateUserTaskTypes(SimpleName returnType, UserTaskModelMetaData userTask) {
        returnType.setIdentifier(userTask.templateReplacement(returnType.getIdentifier()));
    }

    private void interpolateUserTaskTypeArguments(NodeList<Type> ta, UserTaskModelMetaData userTask) {
        ta.stream().forEach(t -> interpolateUserTaskTypes(t, userTask));
    }

    private String sanitizeName(String name) {
        return name.replaceAll("\\s", "_");
    }

    public String generatedFilePath() {
        return relativePath;
    }

    protected boolean isPublic() {
        return KogitoWorkflowProcess.PUBLIC_VISIBILITY.equalsIgnoreCase(process.getVisibility());
    }
}
