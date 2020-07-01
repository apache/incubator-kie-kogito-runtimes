/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jbpm.compiler.canonical;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnknownType;
import org.jbpm.process.core.ParameterDefinition;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.javaparser.StaticJavaParser.parseStatement;
import static java.util.stream.Collectors.joining;

public class ServiceTaskDescriptor {

    public static final Logger logger = LoggerFactory.getLogger(ProcessToExecModelGenerator.class);

    private final String processId;
    private final ClassLoader contextClassLoader;
    private final String interfaceName;
    private final String operationName;
    private final Map<String, String> parameters;
    private final WorkItemNode workItemNode;
    private final String mangledName;
    Class<?> cls;
    private Method method;

    ServiceTaskDescriptor(WorkItemNode workItemNode, String processId, ClassLoader contextClassLoader) {
        this.workItemNode = workItemNode;
        interfaceName = (String) workItemNode.getWork().getParameter("Interface");
        operationName = (String) workItemNode.getWork().getParameter("Operation");
        this.processId = processId;
        this.contextClassLoader = contextClassLoader;

        NodeValidator.of("workItemNode", workItemNode.getName())
                .notEmpty("interfaceName", interfaceName)
                .notEmpty("operationName", operationName)
                .validate();

        parameters = serviceTaskParameters();

        mangledName = mangledHandlerName(interfaceName, operationName, String.valueOf(workItemNode.getId()));

        initializeReflectiveFields();
    }

    private void initializeReflectiveFields() {
        try {
            loadClass();
            findMethod();
        } catch (IllegalArgumentException e) {
            logger.warn("Could not initialize reflective fields. Will try to infer service from parameters. " +
                                "Work Item: {}, method: {}.{}",
                        workItemNode.getName(), interfaceName, operationName);
        }
    }

    public String mangledName() {
        return mangledName;
    }

    private Map<String, String> serviceTaskParameters() {
        String type = (String) workItemNode.getWork().getParameter("ParameterType");
        Map<String, String> parameters = null;
        if (type != null) {
            if (isDefaultParameterType(type)) {
                type = inferParameterType();
            }

            parameters = Collections.singletonMap("Parameter", type);
        } else {
            parameters = new LinkedHashMap<>();

            for (ParameterDefinition def : workItemNode.getWork().getParameterDefinitions()) {
                parameters.put(def.getName(), def.getType().getStringType());
            }
        }
        return parameters;
    }

    // assume 1 single arg as above
    private String inferParameterType() {
        loadClass();
        for (Method m : cls.getMethods()) {
            if (m.getName().equals(operationName) && m.getParameterCount() == 1) {
                return m.getParameterTypes()[0].getCanonicalName();
            }
        }
        throw new IllegalArgumentException(
                MessageFormat.format(
                        "Invalid work item \"{0}\": could not find a method called \"{1}\" in class \"{2}\"",
                        workItemNode.getName(), operationName, interfaceName));
    }

    private void loadClass() {
        if (cls != null) {
            return;
        }
        try {
            cls = contextClassLoader.loadClass(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    MessageFormat.format(
                            "Invalid work item \"{0}\": class not found for interfaceName \"{1}\"",
                            workItemNode.getName(), interfaceName));
        }
    }

    private boolean isDefaultParameterType(String type) {
        return type.equals("java.lang.Object") || type.equals("Object");
    }

    private String mangledHandlerName(String interfaceName, String operationName, String nodeName) {
        return String.format("%s_%s_%s_Handler", interfaceName, operationName, nodeName);
    }

    public CompilationUnit generateHandlerClassForService() {
        CompilationUnit compilationUnit = new CompilationUnit("org.kie.kogito.handlers");

        compilationUnit.getTypes().add(classDeclaration());

        return compilationUnit;
    }

    public ClassOrInterfaceDeclaration classDeclaration() {
        String unqualifiedName = StaticJavaParser.parseName(mangledName).removeQualifier().asString();
        ClassOrInterfaceDeclaration cls = new ClassOrInterfaceDeclaration()
                .setName(unqualifiedName)
                .setModifiers(Modifier.Keyword.PUBLIC)
                .addImplementedType(WorkItemHandler.class.getCanonicalName());
        ClassOrInterfaceType serviceType = new ClassOrInterfaceType(null, interfaceName);

        FieldDeclaration serviceField = new FieldDeclaration()
                .addVariable(new VariableDeclarator(serviceType, "service"));
        cls.addMember(serviceField);

        ClassOrInterfaceType processType =
                new ClassOrInterfaceType(null, org.kie.kogito.Application.class.getCanonicalName());
        FieldDeclaration processField = new FieldDeclaration()
                .addVariable(new VariableDeclarator(processType, "app"));
        cls.addMember(processField);

        // executeWorkItem method
        BlockStmt executeWorkItemBody = new BlockStmt();
        MethodDeclaration executeWorkItem = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType(void.class)
                .setName("executeWorkItem")
                .setBody(executeWorkItemBody)
                .addParameter(WorkItem.class.getCanonicalName(), "workItem")
                .addParameter(WorkItemManager.class.getCanonicalName(), "workItemManager");

        BlockStmt completeWorkItem = completeWorkItem();
        executeWorkItemBody.addStatement(completeWorkItem);

        // abortWorkItem method
        BlockStmt abortWorkItemBody = new BlockStmt();
        MethodDeclaration abortWorkItem = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType(void.class)
                .setName("abortWorkItem")
                .setBody(abortWorkItemBody)
                .addParameter(WorkItem.class.getCanonicalName(), "workItem")
                .addParameter(WorkItemManager.class.getCanonicalName(), "workItemManager");

        // getName method
        MethodDeclaration getName = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType(String.class)
                .setName("getName")
                .setBody(new BlockStmt().addStatement(new ReturnStmt(new StringLiteralExpr(mangledName))));
        cls.addMember(executeWorkItem)
                .addMember(abortWorkItem)
                .addMember(getName);

        return cls;
    }

    private MethodCallExpr callService() {
        MethodCallExpr callService = new MethodCallExpr(new NameExpr("service"), operationName);

        for (Map.Entry<String, String> paramEntry : parameters.entrySet()) {
            MethodCallExpr getParamMethod = new MethodCallExpr(new NameExpr("workItem"), "getParameter").addArgument(new StringLiteralExpr(paramEntry.getKey()));
            callService.addArgument(new CastExpr(new ClassOrInterfaceType(null, paramEntry.getValue()), getParamMethod));
        }
        return callService;
    }

    private BlockStmt completeWorkItem() {

        BlockStmt resultBody = new BlockStmt();
        BlockStmt completeWorkItemBody = new BlockStmt();
        MethodCallExpr callService = callService();

        // workItemManager.completeWorkItem(workItem.getId(), result)
//        MethodCallExpr completeWorkItem = new MethodCallExpr(new NameExpr("workItemManager"), "completeWorkItem")
//                .addArgument(new MethodCallExpr(new NameExpr("workItem"), "getId"))
//                .addArgument(new NameExpr("result"));

        VariableDeclarationExpr pid = new VariableDeclarationExpr(
                new VariableDeclarator()
                        .setType(String.class)
                        .setName("pid")
                        .setInitializer(new StringLiteralExpr(processId)));

        VariableDeclarationExpr piid = new VariableDeclarationExpr(
                new VariableDeclarator()
                        .setType(String.class)
                        .setName("piid")
                        .setInitializer(new MethodCallExpr(new NameExpr("workItem"), "getProcessInstanceId")));

        Statement completeWorkItemInvoke =
                parseStatement("app.processes().processById(pid).instances().findById(piid)" +
                                       ".orElseThrow(() -> new IllegalArgumentException(piid))" +
                                       ".completeWorkItem(piid, result);");

        BlockStmt completeWorkItem = new BlockStmt()
                .addStatement(pid)
                .addStatement(piid)
                .addStatement(completeWorkItemInvoke);

        List<DataAssociation> outAssociations = workItemNode.getOutAssociations();

        VariableDeclarator resultVarDecl = new VariableDeclarator(
                new ClassOrInterfaceType(null, Map.class.getCanonicalName()),
                "result");
        VariableDeclarationExpr resultVar = new VariableDeclarationExpr().addVariable(resultVarDecl);

        if (outAssociations.isEmpty()) {
            resultVarDecl.setInitializer(new NullLiteralExpr());
            completeWorkItemBody.addStatement(resultVar);
            completeWorkItemBody.addStatement(completeWorkItem);
        } else {
            resultVarDecl.setInitializer(new MethodCallExpr(new NameExpr("java.util.Collections"), "singletonMap")
                                                 .addArgument(new StringLiteralExpr(outAssociations.get(0).getSources().get(0)))
                                                 .addArgument(new NameExpr("value")));
            completeWorkItemBody.addStatement(resultVar);
            completeWorkItemBody.addStatement(completeWorkItem);
        }

        if (isCompletionStage()) {
            // complete async
            MethodCallExpr whenCompleteAsync = new MethodCallExpr(callService, "whenCompleteAsync")
                    .addArgument(new LambdaExpr()
                                         .setEnclosingParameters(true)
                                         .addParameter(new UnknownType(), "value")
                                         .addParameter(new UnknownType(), "exception")
                                         .setBody(completeWorkItemBody));
            resultBody.addStatement(whenCompleteAsync);
        } else {
            if (isVoidType()) {
                resultBody.addStatement(callService);
            } else {
                VariableDeclarator varDecl = new VariableDeclarator(
                        new ClassOrInterfaceType(null, Object.class.getCanonicalName()),
                        "value").setInitializer(callService);
                VariableDeclarationExpr valueVar = new VariableDeclarationExpr().addVariable(varDecl);
                resultBody.addStatement(valueVar);
            }

            resultBody.addStatement(completeWorkItemBody);
        }

        return resultBody;
    }

    private boolean isVoidType() {
        return method == null ?
                workItemNode.getOutMappings().isEmpty()
                : void.class.isAssignableFrom(method.getReturnType());
    }

    private boolean isCompletionStage() {
        return method != null && CompletionStage.class.isAssignableFrom(method.getReturnType());
    }

    private void findMethod() {
        int nParams = parameters.size();
        List<Method> candidates = Arrays.stream(cls.getMethods())
                .filter(m -> m.getName().equals(operationName) && m.getParameterCount() == nParams)
                .collect(Collectors.toList());
        switch (candidates.size()) {
            case 0:
                throw new IllegalArgumentException("Could not find any candidate for signature: %s" + signature());
            case 1:
                this.method = candidates.get(0);
                return;
            default:
                String candidateList = candidates.stream().map(Method::toString).collect(joining("\n"));
                throw new UnsupportedOperationException(
                        String.format("Found more than one candidate for signature: %s: \n%s", signature(), candidateList));
        }
    }

    private String signature() {
        String parameterList = parameters.entrySet().stream().map(e -> e.getValue() + " " + e.getKey()).collect(joining(", "));
        return String.format("%s#%s(%s)",
                             interfaceName,
                             operationName,
                             parameterList);
    }
}
