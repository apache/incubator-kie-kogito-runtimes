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

package org.kie.kogito.codegen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;

public class CodegenUtils {

    private static final Modifier.Keyword[] NONE = new Modifier.Keyword[]{};
    private static final Modifier.Keyword[] PUBLIC = new Modifier.Keyword[]{Modifier.Keyword.PUBLIC};
    private static final Modifier.Keyword[] PROTECTED = new Modifier.Keyword[]{Modifier.Keyword.PROTECTED};
    private static final Modifier.Keyword[] PRIVATE = new Modifier.Keyword[]{Modifier.Keyword.PRIVATE};

    public static ClassOrInterfaceType genericType(Class<?> outer, Class<?> inner) {
        return genericType(outer.getCanonicalName(), inner.getCanonicalName());
    }

    public static ClassOrInterfaceType genericType(String outer, Class<?> inner) {
        return genericType(outer, inner.getCanonicalName());
    }

    public static ClassOrInterfaceType genericType(Class<?> outer, String inner) {
        return genericType(outer.getCanonicalName(), inner);
    }

    public static ClassOrInterfaceType genericType(String outer, String inner) {
        return new ClassOrInterfaceType(null, outer).setTypeArguments(new ClassOrInterfaceType(null, inner));
    }

    public static FieldDeclaration field(Modifier.Keyword[] modifiers, Type type, String name) {
        return new FieldDeclaration()
                .setModifiers(modifiers)
                .addVariable(variableDeclarator(type, name));
    }

    public static FieldDeclaration field(Modifier.Keyword[] modifiers, Type type, String name, Expression expr) {
        return new FieldDeclaration()
                .setModifiers(modifiers)
                .addVariable(variableDeclarator(type, name, expr));
    }

    public static FieldDeclaration field(Modifier.Keyword[] modifiers, Class<?> type, String name) {
        return new FieldDeclaration()
                .setModifiers(modifiers)
                .addVariable(variableDeclarator(type, name));
    }

    public static FieldDeclaration field(Modifier.Keyword[] modifiers, Class<?> type, String name, Expression expr) {
        return new FieldDeclaration()
                .setModifiers(modifiers)
                .addVariable(variableDeclarator(type, name, expr));
    }

    public static FieldDeclaration field(Type type, String name) {
        return field(NONE, type, name);
    }

    public static FieldDeclaration field(Type type, String name, Expression expr) {
        return field(NONE, type, name, expr);
    }

    public static FieldDeclaration field(Class<?> type, String name) {
        return field(NONE, type, name);
    }

    public static FieldDeclaration field(Class<?> type, String name, Expression expr) {
        return field(NONE, type, name, expr);
    }

    public static FieldDeclaration publicField(Type type, String name) {
        return field(PUBLIC, type, name);
    }

    public static FieldDeclaration publicField(Type type, String name, Expression expr) {
        return field(PUBLIC, type, name, expr);
    }

    public static FieldDeclaration publicField(Class<?> type, String name) {
        return field(PUBLIC, type, name);
    }

    public static FieldDeclaration publicField(Class<?> type, String name, Expression expr) {
        return field(PUBLIC, type, name, expr);
    }

    public static FieldDeclaration protectedField(Type type, String name) {
        return field(PROTECTED, type, name);
    }

    public static FieldDeclaration protectedField(Type type, String name, Expression expr) {
        return field(PROTECTED, type, name, expr);
    }

    public static FieldDeclaration protectedField(Class<?> type, String name) {
        return field(PROTECTED, type, name);
    }

    public static FieldDeclaration protectedField(Class<?> type, String name, Expression expr) {
        return field(PROTECTED, type, name, expr);
    }

    public static FieldDeclaration privateField(Type type, String name) {
        return field(PRIVATE, type, name);
    }

    public static FieldDeclaration privateField(Type type, String name, Expression expr) {
        return field(PRIVATE, type, name, expr);
    }

    public static FieldDeclaration privateField(Class<?> type, String name) {
        return field(PRIVATE, type, name);
    }

    public static FieldDeclaration privateField(Class<?> type, String name, Expression expr) {
        return field(PRIVATE, type, name, expr);
    }

    public static MethodDeclaration method(Modifier.Keyword[] modifiers, Class<?> type, String name, NodeList<Parameter> parameters, BlockStmt body) {
        return new MethodDeclaration()
                .setModifiers(modifiers)
                .setType(type == null ? "void" : type.getCanonicalName())
                .setName(name)
                .setParameters(parameters)
                .setBody(body);
    }

    public static MethodDeclaration publicMethod(Class<?> type, String name, BlockStmt body) {
        return method(PUBLIC, type, name, new NodeList<>(), body);
    }

    public static MethodDeclaration publicMethod(Class<?> type, String name, NodeList<Parameter> parameters, BlockStmt body) {
        return method(PUBLIC, type, name, parameters, body);
    }

    public static MethodDeclaration privateMethod(Class<?> type, String name, BlockStmt body) {
        return method(PRIVATE, type, name, new NodeList<>(), body);
    }

    public static MethodDeclaration privateMethod(Class<?> type, String name, NodeList<Parameter> parameters, BlockStmt body) {
        return method(PRIVATE, type, name, parameters, body);
    }

    public static ObjectCreationExpr newObject(Class<?> type) {
        return newObject(type.getCanonicalName());
    }

    public static ObjectCreationExpr newObject(Class<?> type, Expression... arguments) {
        return newObject(type.getCanonicalName(), arguments);
    }

    public static ObjectCreationExpr newObject(Class<?> type, String... arguments) {
        return newObject(type.getCanonicalName(), arguments);
    }

    public static ObjectCreationExpr newObject(String type) {
        return new ObjectCreationExpr(null, new ClassOrInterfaceType(null, type), new NodeList<>());
    }

    public static ObjectCreationExpr newObject(String type, Expression... arguments) {
        return new ObjectCreationExpr(null, new ClassOrInterfaceType(null, type), NodeList.nodeList(arguments));
    }

    public static ObjectCreationExpr newObject(String type, String... arguments) {
        return new ObjectCreationExpr(null, new ClassOrInterfaceType(null, type),
                NodeList.nodeList(Arrays.stream(arguments).map(NameExpr::new).collect(Collectors.toList()))
        );
    }

    public static Parameter parameter(Type type, String name) {
        return new Parameter().setType(type).setName(name);
    }

    public static Parameter parameter(Class<?> type, String name) {
        return parameter(type.getCanonicalName(), name);
    }

    public static Parameter parameter(String type, String name) {
        return new Parameter().setType(type).setName(name);
    }

    public static Expression variable(Type type, String name) {
        return new VariableDeclarationExpr(variableDeclarator(type, name));
    }

    public static Expression variable(Type type, String name, Expression expr) {
        return new VariableDeclarationExpr(variableDeclarator(type, name, expr));
    }

    public static Expression variable(Class<?> type, String name) {
        return variable(type.getCanonicalName(), name);
    }

    public static Expression variable(Class<?> type, String name, Expression expr) {
        return variable(type.getCanonicalName(), name, expr);
    }

    public static Expression variable(String type, String name) {
        return new VariableDeclarationExpr(variableDeclarator(type, name));
    }

    public static Expression variable(String type, String name, Expression expr) {
        return new VariableDeclarationExpr(variableDeclarator(type, name, expr));
    }

    public static VariableDeclarator variableDeclarator(Type type, String name) {
        return new VariableDeclarator(type, name);
    }

    public static VariableDeclarator variableDeclarator(Type type, String name, Expression expr) {
        return new VariableDeclarator(type, name, expr);
    }

    public static VariableDeclarator variableDeclarator(Class<?> type, String name) {
        return variableDeclarator(type.getCanonicalName(), name);
    }

    public static VariableDeclarator variableDeclarator(Class<?> type, String name, Expression expr) {
        return variableDeclarator(type.getCanonicalName(), name, expr);
    }

    public static VariableDeclarator variableDeclarator(String type, String name) {
        return new VariableDeclarator(new ClassOrInterfaceType(null, type), name);
    }

    public static VariableDeclarator variableDeclarator(String type, String name, Expression expr) {
        return new VariableDeclarator(new ClassOrInterfaceType(null, type), name, expr);
    }

    public static void interpolateArguments(MethodDeclaration md, String dataType) {
        md.getParameters().forEach(p -> p.setType(dataType));
    }

    //Defaults the "to be interpolated type" to $Type$.
    public static void interpolateTypes(ClassOrInterfaceType t, String dataClazzName) {
        SimpleName returnType = t.getName();
        Map<String, String> interpolatedTypes = new HashMap<>();
        interpolatedTypes.put("$Type$", dataClazzName);
        interpolateTypes(returnType, interpolatedTypes);
        t.getTypeArguments().ifPresent(ta -> interpolateTypeArguments(ta, interpolatedTypes));
    }

    public static void interpolateTypes(ClassOrInterfaceType t, Map<String, String> typeInterpolations) {
        SimpleName returnType = t.getName();
        interpolateTypes(returnType, typeInterpolations);
        t.getTypeArguments().ifPresent(ta -> interpolateTypeArguments(ta, typeInterpolations));
    }

    public static void interpolateTypes(SimpleName returnType, Map<String, String> typeInterpolations) {
        typeInterpolations.entrySet().stream().forEach(entry -> {
            String identifier = returnType.getIdentifier();
            String newIdentifier = identifier.replace(entry.getKey(), entry.getValue());
            returnType.setIdentifier(newIdentifier);
        });
    }

    public static void interpolateTypeArguments(NodeList<Type> ta, Map<String, String> typeInterpolations) {
        ta.stream().map(Type::asClassOrInterfaceType)
                .forEach(t -> interpolateTypes(t, typeInterpolations));
    }

    public static boolean isProcessField(FieldDeclaration fd) {
        return fd.getElementType().asClassOrInterfaceType().getNameAsString().equals("Process");
    }

    public static boolean isApplicationField(FieldDeclaration fd) {
        return fd.getElementType().asClassOrInterfaceType().getNameAsString().equals("Application");
    }

    public static MethodDeclaration extractOptionalInjection(String type, String fieldName, String defaultMethod, DependencyInjectionAnnotator annotator) {
        BlockStmt body = new BlockStmt();
        MethodDeclaration extractMethod = new MethodDeclaration()
                .addModifier(Modifier.Keyword.PROTECTED)
                .setName("extract_" + fieldName)
                .setType(type)
                .setBody(body);
        Expression condition = annotator.optionalInstanceExists(fieldName);
        IfStmt valueExists = new IfStmt(condition, new ReturnStmt(new MethodCallExpr(new NameExpr(fieldName), "get")), new ReturnStmt(new NameExpr(defaultMethod)));
        body.addStatement(valueExists);
        return extractMethod;
    }

}
