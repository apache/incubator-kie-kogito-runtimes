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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.drools.core.util.ClassUtils;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.internal.ruleunit.RuleUnitVariable;
import org.kie.kogito.codegen.FileGenerator;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStream;
import org.kie.kogito.rules.RuleUnitData;
import org.kie.kogito.rules.units.ReflectiveRuleUnitDescription;

import static org.drools.core.util.StringUtils.ucFirst;
import static org.drools.modelcompiler.util.ClassUtil.toRawClass;

public class RuleUnitDTOSourceClass implements FileGenerator {

    private final RuleUnitDescription ruleUnit;

    private final String targetCanonicalName;
    private final String generatedFilePath;
    private final String packageName;

    public RuleUnitDTOSourceClass(RuleUnitDescription ruleUnit ) {
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
        cu.setPackageDeclaration( packageName );

        ClassOrInterfaceDeclaration dtoClass = cu.addClass( targetCanonicalName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC );
        dtoClass.addImplementedType( "java.util.function.Supplier<" + ruleUnit.getSimpleName() + ">" );

        MethodDeclaration supplier = dtoClass.addMethod( "get", com.github.javaparser.ast.Modifier.Keyword.PUBLIC );
        supplier.addAnnotation( Override.class );
        supplier.setType( ruleUnit.getSimpleName() );
        BlockStmt supplierBlock = supplier.createBody();
        supplierBlock.addStatement( ruleUnit.getSimpleName() + " unit = new " + ruleUnit.getSimpleName() + "();" );

        ruleUnit.getUnitVarDeclarations().forEach(v -> processField(dtoClass, supplierBlock, v));

        supplierBlock.addStatement( "return unit;" );

        return cu.toString();
    }


    private void processField(ClassOrInterfaceDeclaration dtoClass, BlockStmt supplierBlock, RuleUnitVariable ruleUnitVariable) {
        boolean isDataSource = ruleUnitVariable.isDataSource();
        String typeName = ruleUnitVariable.getType().getCanonicalName();
        String genericType = null;

        if ( isDataSource ) {
            int genericStart = typeName.indexOf( '<' );
            if (genericStart > 0) {
                genericType = typeName.substring( genericStart+1, typeName.length()-1 ).trim();
                typeName = "java.util.List" + typeName.substring( genericStart );
            } else {
                genericType = "Object";
                typeName = "java.util.List";
            }
        }
        FieldDeclaration fieldDeclaration = dtoClass.addField(typeName, ruleUnitVariable.getName(), com.github.javaparser.ast.Modifier.Keyword.PRIVATE);
        MethodDeclaration getter = fieldDeclaration.createGetter();
        MethodDeclaration setter = fieldDeclaration.createSetter();

        String setterName = setter.getNameAsString();
        if (genericType != null) {
            String singleSetterName = setterName.endsWith( "s" ) ? setterName.substring( 0, setterName.length()-1 ) : setterName + "Single";
            MethodDeclaration singleValueSetter = dtoClass.addMethod( singleSetterName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC );
            singleValueSetter.addParameter( genericType, ruleUnitVariable.getName() );
            singleValueSetter.createBody()
                    .addStatement( "this." + ruleUnitVariable.getName() + " = new java.util.ArrayList<>();")
                    .addStatement( "this." + ruleUnitVariable.getName() + ".add(" + ruleUnitVariable.getName() + ");");
        }

        if (isDataSource) {
            fieldDeclaration.getVariables().forEach(v -> v.setInitializer("java.util.Collections.emptyList()"));

            boolean isDataStream = DataStream.class.isAssignableFrom( ruleUnitVariable.getType() );
            String sourceType = isDataStream ? "Stream" : "Store";
            String addMethod = isDataStream ? "append" : "add";

            if ( ruleUnitVariable.isDataSource() ) {
                supplierBlock.addStatement( "org.kie.kogito.rules.Data" + sourceType + "<" + genericType + "> " + ruleUnitVariable.getName() + " = org.kie.kogito.rules.DataSource.create" + sourceType + "();" );
                supplierBlock.addStatement( "this." + ruleUnitVariable.getName() + ".forEach( " + ruleUnitVariable.getName() + "::" + addMethod + " );" );
            } else {
                supplierBlock.addStatement( "this." + ruleUnitVariable.getName() + ".forEach( unit." + getter.getNameAsString() + "()::" + addMethod + " );" );
                return;
            }
        }

        supplierBlock.addStatement( "unit." + setterName + "( " + ruleUnitVariable.getName() + " );" );
    }

    private static class FieldDescriptor {
        private final Type type;
        private final String name;
        private final String getter;
        private final FieldKind kind;

        private FieldDescriptor( Type type, String name, FieldKind kind ) {
            this(type, name, null, kind);
        }

        private FieldDescriptor( Type type, String name, String getter, FieldKind kind ) {
            this.type = type;
            this.name = name;
            this.getter = getter;
            this.kind = kind;
        }

        @Override
        public String toString() {
            return "FieldDescriptor{" +
                    "type=" + type +
                    ", name='" + name + '\'' +
                    ", getter='" + getter + '\'' +
                    ", kind=" + kind +
                    '}';
        }
    }

    private enum FieldKind {
        PUBLIC, GETTABLE, SETTABLE
    }
}
