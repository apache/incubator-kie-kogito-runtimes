/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.codegen.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.WildcardType;
import org.kie.kogito.Model;
import org.kie.kogito.codegen.AbstractApplicationSection;
import org.kie.kogito.codegen.BodyDeclarationComparator;
import org.kie.kogito.codegen.TemplatedGenerator;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.process.Processes;

import static com.github.javaparser.StaticJavaParser.parse;

public class ProcessesContainerGenerator extends AbstractApplicationSection {

    private static final String RESOURCE = "/class-templates/ProcessesTemplate.java";
    private static final String RESOURCE_CDI = "/class-templates/CdiProcessesTemplate.java";
    private static final String RESOURCE_SPRING = "/class-templates/SpringProcessesTemplate.java";

    private final String packageName;
    private final List<ProcessGenerator> processes;
    private final List<BodyDeclaration<?>> factoryMethods;

    private DependencyInjectionAnnotator annotator;

    private NodeList<BodyDeclaration<?>> applicationDeclarations;
    private BlockStmt byProcessIdBody = new BlockStmt();
    private BlockStmt processesBody = new BlockStmt();
    private final TemplatedGenerator templatedGenerator;

    public ProcessesContainerGenerator(String packageName) {
        super("Processes", "processes", Processes.class);
        this.packageName = packageName;
        this.processes = new ArrayList<>();
        this.factoryMethods = new ArrayList<>();
        this.applicationDeclarations = new NodeList<>();

        this.templatedGenerator = new TemplatedGenerator(
                packageName,
                "Processes",
                RESOURCE_CDI,
                RESOURCE_SPRING,
                RESOURCE);
    }

    public List<BodyDeclaration<?>> factoryMethods() {
        return factoryMethods;
    }

    public void addProcess(ProcessGenerator p) {
        processes.add(p);
        addProcessToApplication(p);
    }

    public void addProcessToApplication(ProcessGenerator r) {
        ObjectCreationExpr newProcess = new ObjectCreationExpr()
                .setType(r.targetCanonicalName())
                .addArgument("application");
        IfStmt byProcessId = new IfStmt(new MethodCallExpr(new StringLiteralExpr(r.processId()), "equals", NodeList.nodeList(new NameExpr("processId"))),
                                        new ReturnStmt(new MethodCallExpr(
                                                newProcess,
                                                "configure")),
                                        null);

        byProcessIdBody
                .addStatement(byProcessId);
    }

    public ProcessesContainerGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        this.templatedGenerator.withDependencyInjection(annotator);
        return this;
    }

    @Override
    public ClassOrInterfaceDeclaration classDeclaration() {
        CompilationUnit compilationUnit = templatedGenerator.compilationUnit()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Template: No CompilationUnit"));

        if (annotator == null) {
            byProcessIdBody
                    .addStatement(new ReturnStmt(new NullLiteralExpr()));

            NodeList<Expression> processIds = NodeList.nodeList(processes.stream().map(p -> new StringLiteralExpr(p.processId())).collect(Collectors.toList()));
            processesBody
                    .addStatement(new ReturnStmt(new MethodCallExpr(new NameExpr(Arrays.class.getCanonicalName()), "asList", processIds)));

            FieldDeclaration applicationFieldDeclaration = new FieldDeclaration();
            applicationFieldDeclaration
                    .addVariable(new VariableDeclarator(new ClassOrInterfaceType(null, "Application"), "application"))
                    .setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);
            applicationDeclarations.add(applicationFieldDeclaration);

            ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration("Processes")
                    .addModifier(Modifier.Keyword.PUBLIC)
                    .addParameter("Application", "application")
                    .setBody(new BlockStmt().addStatement("this.application = application;"));
            applicationDeclarations.add(constructorDeclaration);

            compilationUnit.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("processIds")).get()
                    .setBody(this.processesBody);

            compilationUnit.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("processById")).get()
                    .setBody(this.byProcessIdBody);

            ClassOrInterfaceDeclaration cls = super.classDeclaration().setMembers(applicationDeclarations);
            cls.getMembers().sort(new BodyDeclarationComparator());
        }
        return compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).get();
    }

//    @Override
//    public CompilationUnit injectableClass() {
//        CompilationUnit compilationUnit = parse(this.getClass().getResourceAsStream(RESOURCE)).setPackageDeclaration(packageName);
//        ClassOrInterfaceDeclaration cls = compilationUnit
//                .findFirst(ClassOrInterfaceDeclaration.class)
//                .orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface declaration!"));
//
//        cls.findAll(FieldDeclaration.class, fd -> fd.getVariable(0).getNameAsString().equals("processes")).forEach(fd -> {
//            annotator.withInjection(fd);
//            fd.getVariable(0).setType(new ClassOrInterfaceType(null, new SimpleName(annotator.multiInstanceInjectionType()),
//                                                               NodeList.nodeList(new ClassOrInterfaceType(null, new SimpleName(org.kie.kogito.process.Process.class.getCanonicalName()), NodeList.nodeList(new WildcardType(new ClassOrInterfaceType(null, Model.class.getCanonicalName())))))));
//        });
//
//        annotator.withApplicationComponent(cls);
//
//        return compilationUnit;
//    }
}
