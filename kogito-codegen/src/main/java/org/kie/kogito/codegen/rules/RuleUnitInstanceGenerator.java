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

package org.kie.kogito.codegen.rules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.core.util.ClassUtils;
import org.kie.api.runtime.KieSession;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.internal.ruleunit.RuleUnitVariable;
import org.kie.kogito.codegen.BodyDeclarationComparator;
import org.kie.kogito.codegen.FileGenerator;
import org.kie.kogito.conf.DefaultEntryPoint;
import org.kie.kogito.conf.EntryPoint;
import org.kie.kogito.rules.units.AbstractRuleUnitInstance;
import org.kie.kogito.rules.units.EntryPointDataProcessor;

import static org.kie.internal.ruleunit.RuleUnitUtil.isDataSource;

public class RuleUnitInstanceGenerator implements FileGenerator {

    private final RuleUnitDescription ruleUnit;
    private final String packageName;
    private final String typeName;
    /**
     * class loader is currently used to resolve type declarations
     * in the rule unit
     *
     */
    private final ClassLoader classLoader;
    private final String canonicalName;
    private final String targetTypeName;
    private final String targetCanonicalName;
    private final String generatedFilePath;

    public static String qualifiedName(String packageName, String typeName) {
        return packageName + "." + typeName + "RuleUnitInstance";
    }

    public RuleUnitInstanceGenerator( RuleUnitDescription ruleUnit, String packageName, String typeName, ClassLoader classLoader) {
        this.ruleUnit = ruleUnit;
        this.packageName = packageName;
        this.typeName = typeName;
        this.classLoader = classLoader;
        this.canonicalName = packageName + "." + typeName;
        this.targetTypeName = typeName + "RuleUnitInstance";
        this.targetCanonicalName = packageName + "." + targetTypeName;
        this.generatedFilePath = targetCanonicalName.replace('.', '/') + ".java";
    }

    @Override
    public String generatedFilePath() {
        return generatedFilePath;
    }

    @Override
    public String generate() {
        return compilationUnit().toString();
    }

    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit = new CompilationUnit(packageName);
        compilationUnit.getTypes().add(classDeclaration());
        return compilationUnit;
    }

    private MethodDeclaration bindMethod() {
        MethodDeclaration methodDeclaration = new MethodDeclaration();
        BlockStmt methodBlock = new BlockStmt();
        methodDeclaration.setName("bind")
                .addAnnotation( "Override" )
                .addModifier(Modifier.Keyword.PROTECTED)
                .addParameter(KieSession.class.getCanonicalName(), "runtime")
                .addParameter(typeName, "value")
                .setType(void.class)
                .setBody(methodBlock);

        // we are currently relying on reflection, but proper way to do this
        // would be to use JavaParser on the src class AND fallback
        // on reflection if the class is not available.
        Class<?> typeClass;
        try {
            typeClass = classLoader.loadClass(canonicalName);
        } catch (ClassNotFoundException e) {
            return createBindingsFromUnitVars( methodDeclaration, methodBlock );
        }

        try {
            for (Method m : typeClass.getMethods()) {
                String methodName = m.getName();
                String propertyName = ClassUtils.getter2property(methodName);
                if (propertyName == null || propertyName.equals( "class" )) {
                    continue;
                }
                boolean isDataSource = isDataSource( m.getReturnType() );
                String entryPointName = isDataSource ? getEntryPointName( typeClass, propertyName ) : null;

                addBinding( methodBlock, methodName, propertyName, isDataSource, entryPointName );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return methodDeclaration;
    }

    private MethodDeclaration createBindingsFromUnitVars( MethodDeclaration methodDeclaration, BlockStmt methodBlock ) {
        for (RuleUnitVariable unitVar : ruleUnit.getUnitVarDeclarations()) {
            boolean isDataSource = unitVar.isDataSource();
            String methodName = unitVar.getter();
            String propertyName = unitVar.getName();
            String entryPointName = isDataSource ? propertyName : null;

            addBinding( methodBlock, methodName, propertyName, isDataSource, entryPointName );
        }

        return methodDeclaration;
    }

    private void addBinding( BlockStmt methodBlock, String methodName, String propertyName, boolean isDataSource, String entryPointName ) {
        if ( isDataSource ) {

            //  value.$method())
            Expression fieldAccessor =
                    new MethodCallExpr(new NameExpr("value"), methodName);

            // .subscribe( new EntryPointDataProcessor(runtime.getEntryPoint()) )
            MethodCallExpr drainInto = new MethodCallExpr(fieldAccessor, "subscribe")
                    .addArgument(new ObjectCreationExpr(null, StaticJavaParser.parseClassOrInterfaceType( EntryPointDataProcessor.class.getName() ), NodeList.nodeList(
                            new MethodCallExpr(
                            new NameExpr("runtime"), "getEntryPoint",
                            NodeList.nodeList(new StringLiteralExpr( entryPointName ))))));

            methodBlock.addStatement(drainInto);
        }

        MethodCallExpr setGlobalCall = new MethodCallExpr( new NameExpr("runtime"), "setGlobal" );
        setGlobalCall.addArgument( new StringLiteralExpr( propertyName ) );
        setGlobalCall.addArgument( new MethodCallExpr(new NameExpr("value"), methodName) );
        methodBlock.addStatement(setGlobalCall);
    }

    private String getEntryPointName( Class<?> typeClass, String propertyName ) {
        try {
            Field dataSourceField = typeClass.getDeclaredField( propertyName );
            if (dataSourceField.getAnnotation( DefaultEntryPoint.class ) != null) {
                return org.kie.api.runtime.rule.EntryPoint.DEFAULT_NAME;
            }
            EntryPoint epAnn = dataSourceField.getAnnotation( EntryPoint.class );
            if (epAnn != null) {
                return epAnn.value();
            }
        } catch (NoSuchFieldException e) { }
        return propertyName;
    }

    public ClassOrInterfaceDeclaration classDeclaration() {
        ClassOrInterfaceDeclaration classDecl = new ClassOrInterfaceDeclaration()
                .setName(targetTypeName)
                .addModifier(Modifier.Keyword.PUBLIC);
        classDecl
                .addExtendedType(
                        new ClassOrInterfaceType(null, AbstractRuleUnitInstance.class.getCanonicalName())
                                .setTypeArguments(new ClassOrInterfaceType(null, canonicalName)))
                .addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter(RuleUnitGenerator.ruleUnitType(canonicalName), "unit")
                .addParameter(canonicalName, "value")
                .addParameter(KieSession.class.getCanonicalName(), "session")
                .setBody(new BlockStmt().addStatement(new MethodCallExpr(
                        "super",
                        new NameExpr("unit"),
                        new NameExpr("value"),
                        new NameExpr("session")
                )));
        classDecl.addMember(bindMethod());
        classDecl.getMembers().sort(new BodyDeclarationComparator());
        return classDecl;
    }
}
