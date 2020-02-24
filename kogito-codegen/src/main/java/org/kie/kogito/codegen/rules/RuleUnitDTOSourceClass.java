/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.internal.ruleunit.RuleUnitVariable;
import org.kie.kogito.codegen.FileGenerator;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.DataStream;
import org.kie.kogito.rules.SingletonStore;

public class RuleUnitDTOSourceClass implements FileGenerator {

    private final RuleUnitDescription ruleUnit;

    private final String targetCanonicalName;
    private final String generatedFilePath;
    private final String packageName;

    public RuleUnitDTOSourceClass(RuleUnitDescription ruleUnit) {
        this.ruleUnit = ruleUnit;

        this.targetCanonicalName = ruleUnit.getSimpleName() + "DTO";
        this.packageName = ruleUnit.getPackageName();
        this.generatedFilePath = (packageName + "." + targetCanonicalName).replace('.', '/') + ".java";
    }

    @Override
    public String generatedFilePath() {
        return generatedFilePath;
    }

    @Override
    public String generate() {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration(packageName);

        ClassOrInterfaceDeclaration dtoClass = cu.addClass(targetCanonicalName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
        dtoClass.addImplementedType("java.util.function.Supplier<" + ruleUnit.getSimpleName() + ">");

        MethodDeclaration supplier = dtoClass.addMethod("get", com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
        supplier.addAnnotation(Override.class);
        supplier.setType(ruleUnit.getSimpleName());
        BlockStmt supplierBlock = supplier.createBody();
        supplierBlock.addStatement(ruleUnit.getSimpleName() + " unit = new " + ruleUnit.getSimpleName() + "();");

        ruleUnit.getUnitVarDeclarations().forEach(v -> processField(dtoClass, supplierBlock, v));

        supplierBlock.addStatement("return unit;");

        return cu.toString();
    }

    private void processField(ClassOrInterfaceDeclaration dtoClass, BlockStmt supplierBlock, RuleUnitVariable ruleUnitVariable) {
        boolean isDataSource = ruleUnitVariable.isDataSource();
        boolean isSingletonStore = SingletonStore.class.isAssignableFrom(ruleUnitVariable.getType());
        String typeName = ruleUnitVariable.getType().getCanonicalName();
        String genericType = Optional.ofNullable(ruleUnitVariable.getDataSourceParameterType()).map(Class::getCanonicalName).orElse("Object");
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, typeName);

        if (isSingletonStore) {
            type = new ClassOrInterfaceType(null, genericType);
        } else if (isDataSource) {
            typeName = "java.util.List";
            genericType = ruleUnitVariable.getDataSourceParameterType().getCanonicalName();
            type = new ClassOrInterfaceType(null, typeName).setTypeArguments(new ClassOrInterfaceType(null, genericType));
        }

        FieldDeclaration fieldDeclaration = dtoClass.addField(
                type, ruleUnitVariable.getName(), com.github.javaparser.ast.Modifier.Keyword.PRIVATE);
        MethodDeclaration getter = fieldDeclaration.createGetter();
        MethodDeclaration setter = fieldDeclaration.createSetter();

        if (isDataSource) {
            if (!isSingletonStore) {
                fieldDeclaration.getVariables() // it's a foreach, but it contains only this variable
                        .forEach(v -> v.setInitializer("java.util.Collections.emptyList()"));
            }
            String addMethod;
            if (DataStream.class.isAssignableFrom(ruleUnitVariable.getType())) {
                addMethod = "append";
                supplierBlock.addStatement("this." + ruleUnitVariable.getName() + ".forEach( unit." + getter.getNameAsString() + "()::" + addMethod + " );");
            } else if (DataStore.class.isAssignableFrom(ruleUnitVariable.getType())) {
                addMethod = "add";
                supplierBlock.addStatement("this." + ruleUnitVariable.getName() + ".forEach( unit." + getter.getNameAsString() + "()::" + addMethod + " );");
            } else if (SingletonStore.class.isAssignableFrom(ruleUnitVariable.getType())) {
                addMethod = "set";
                supplierBlock.addStatement("unit." + getter.getNameAsString() + "().set(" + "this." + ruleUnitVariable.getName() + " );");
            } else {
                throw new IllegalArgumentException("Unknown data source type " + ruleUnitVariable.getType());
            }

        }

    }

}
