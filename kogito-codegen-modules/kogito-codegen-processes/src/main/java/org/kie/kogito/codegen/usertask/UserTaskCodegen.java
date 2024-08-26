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
package org.kie.kogito.codegen.usertask;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.drools.codegen.common.GeneratedFile;
import org.drools.codegen.common.GeneratedFileType;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.compiler.xml.core.SemanticModules;
import org.jbpm.process.core.Work;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.kie.api.definition.process.Process;
import org.kie.api.io.Resource;
import org.kie.kogito.codegen.api.ApplicationSection;
import org.kie.kogito.codegen.api.ConfigGenerator;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.io.CollectedResource;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.core.AbstractGenerator;
import org.kie.kogito.codegen.process.ProcessParsingException;
import org.kie.kogito.internal.SupportedExtensions;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;
import org.xml.sax.SAXException;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;

import static java.util.stream.Collectors.toList;

public class UserTaskCodegen extends AbstractGenerator {
    private static final String NODE_NAME = "NodeName";
    private static final String DESCRIPTION = "Description";
    private static final String PRIORITY = "Priority";
    private static final String TASK_NAME = "TaskName";

    private static final String ACTOR_ID = "ActorId";
    private static final String GROUP_ID = "GroupId";
    private static final String BUSINESSADMINISTRATOR_ID = "BusinessAdministratorId";
    private static final String BUSINESSADMINISTRATOR_GROUP_ID = "BusinessAdministratorGroupId";
    private static final String EXCLUDED_OWNER_ID = "ExcludedOwnerId";

    private static final SemanticModules BPMN_SEMANTIC_MODULES;

    static {
        BPMN_SEMANTIC_MODULES = new SemanticModules();
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNExtensionsSemanticModule());
        BPMN_SEMANTIC_MODULES.addSemanticModule(new BPMNDISemanticModule());
    }

    public static final String SECTION_CLASS_NAME = "usertask";

    TemplatedGenerator templateGenerator;
    private List<Work> descriptors;

    public UserTaskCodegen(KogitoBuildContext context, List<Work> collectedResources) {
        super(context, "User Task");//, );
        this.descriptors = collectedResources;

        templateGenerator = TemplatedGenerator.builder()
                .withTemplateBasePath("/class-templates/usertask")
                .withTargetTypeName(SECTION_CLASS_NAME)
                .build(context, "UserTask");
    }

    @Override
    public Optional<ApplicationSection> section() {
        return Optional.of(new UserTaskContainerGenerator(this.context(), descriptors));
    }

    @Override
    public Optional<ConfigGenerator> configGenerator() {
        return Optional.of(new UserTaskConfigGenerator(context(), descriptors));
    }

    @Override
    public boolean isEmpty() {
        return descriptors.isEmpty();
    }

    @Override
    protected Collection<GeneratedFile> internalGenerate() {
        List<GeneratedFile> generatedFiles = new ArrayList<>();
        for (Work info : descriptors) {
            CompilationUnit unit = templateGenerator.compilationUnit().get();

            String className = UserTaskCodegenHelper.className(info);
            String packageName = UserTaskCodegenHelper.packageName(info);
            unit.getPackageDeclaration().get().setName(packageName);

            ClassOrInterfaceDeclaration clazzDeclaration = unit.findFirst(ClassOrInterfaceDeclaration.class).get();
            clazzDeclaration.setName(className);

            ConstructorDeclaration declaration = clazzDeclaration.findFirst(ConstructorDeclaration.class).get();
            declaration.setName(className);

            BlockStmt block = declaration.getBody();
            NodeList<Expression> arguments = new NodeList<>();
            arguments.add(new StringLiteralExpr((String) info.getParameter("id")));
            arguments.add(new StringLiteralExpr((String) info.getParameter(NODE_NAME)));
            arguments.add(new NullLiteralExpr());
            block.addStatement(new ExplicitConstructorInvocationStmt().setThis(false).setArguments(arguments));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setPotentialUsers", NodeList.nodeList(toStringExpression(info.getParameter(ACTOR_ID)))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setPotentialGroups", NodeList.nodeList(toStringExpression(info.getParameter(GROUP_ID)))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setAdminUsers", NodeList.nodeList(toStringExpression(info.getParameter(BUSINESSADMINISTRATOR_ID)))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setAdminGroups", NodeList.nodeList(toStringExpression(info.getParameter(BUSINESSADMINISTRATOR_GROUP_ID)))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setExcludedUsers", NodeList.nodeList(toStringExpression(info.getParameter(EXCLUDED_OWNER_ID)))));

            block.addStatement(new MethodCallExpr(new ThisExpr(), "setTaskDescription", NodeList.nodeList(toStringExpression(info.getParameter(DESCRIPTION)))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setTaskPriority", NodeList.nodeList(toStringExpression(info.getParameter(PRIORITY)))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setReferenceName", NodeList.nodeList(toStringExpression(info.getParameter(NODE_NAME)))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setSkippable", NodeList.nodeList(toStringExpression(info.getParameter("Skippable")))));

            block.addStatement(new MethodCallExpr(new ThisExpr(), "setNotStartedDeadLines", NodeList.nodeList(toStringExpression(info.getParameter("NotStartedNotify")))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setNotCompletedDeadlines", NodeList.nodeList(toStringExpression(info.getParameter("NotCompletedNotify")))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setNotStartedReassignments", NodeList.nodeList(toStringExpression(info.getParameter("NotCompletedReassign")))));
            block.addStatement(new MethodCallExpr(new ThisExpr(), "setNotCompletedReassigments", NodeList.nodeList(toStringExpression(info.getParameter("NotStartedReassign")))));

            generatedFiles.add(new GeneratedFile(GeneratedFileType.SOURCE, UserTaskCodegenHelper.path(info).resolve(className + ".java"), unit.toString()));
        }

        return generatedFiles;
    }

    private Expression toStringExpression(Object value) {
        if (value == null) {
            return new CastExpr(StaticJavaParser.parseType(String.class.getName()), new NullLiteralExpr());
        }

        return new StringLiteralExpr(value.toString());
    }

    public static UserTaskCodegen ofCollectedResources(KogitoBuildContext context, Collection<CollectedResource> resources) {

        Set<String> extensions = SupportedExtensions.getBPMNExtensions();
        Predicate<Resource> supportExtensions = resource -> extensions.stream().anyMatch(resource.getSourcePath()::endsWith);

        List<Work> userTasks = resources.stream()
                .map(CollectedResource::resource)
                .filter(supportExtensions)
                .flatMap(resource -> {
                    List<Work> data = new ArrayList<>();
                    for (KogitoWorkflowProcess process : parseProcessFile(resource).stream().map(KogitoWorkflowProcess.class::cast).toList()) {
                        List<Work> descriptors = process.getNodesRecursively()
                                .stream()
                                .filter(HumanTaskNode.class::isInstance)
                                .map(HumanTaskNode.class::cast)
                                .map(HumanTaskNode::getWork)
                                .toList();

                        data.addAll(descriptors);
                        data.forEach(e -> e.setParameter("PackageName", process.getPackageName()));
                    }

                    return data.stream();

                })
                .collect(toList());

        return ofUserTasks(context, userTasks);
    }

    private static UserTaskCodegen ofUserTasks(KogitoBuildContext context, List<Work> userTasks) {
        return new UserTaskCodegen(context, userTasks);
    }

    protected static Collection<Process> parseProcessFile(Resource r) {
        try (Reader reader = r.getReader()) {
            XmlProcessReader xmlReader = new XmlProcessReader(
                    BPMN_SEMANTIC_MODULES,
                    Thread.currentThread().getContextClassLoader());
            return xmlReader.read(reader);
        } catch (SAXException | IOException e) {
            throw new ProcessParsingException(e);
        }
    }
}