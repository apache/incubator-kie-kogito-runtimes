/*
 * Copyright 2005 JBoss Inc
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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.kie.internal.ruleunit.RuleUnitDescription;
import org.kie.internal.ruleunit.RuleUnitVariable;
import org.kie.kogito.codegen.FileGenerator;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitData;

import static com.github.javaparser.StaticJavaParser.parseExpression;
import static org.drools.core.util.StringUtils.ucFirst;

public class RuleUnitDataGenerator implements FileGenerator {

    private final RuleUnitDescription ruleUnit;
    private final String packageName;
    private final String typeName;
    private final String generatedFilePath;

    public RuleUnitDataGenerator( RuleUnitDescription ruleUnit ) {
        this.ruleUnit = ruleUnit;
        this.packageName = ruleUnit.getPackageName();
        this.typeName = ruleUnit.getSimpleName();
        this.generatedFilePath = (packageName + "." + typeName).replace('.', '/') + ".java";
    }


    @Override
    public String generatedFilePath() {
        return generatedFilePath;
    }

    @Override
    public String generate() {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration( packageName );

        ClassOrInterfaceDeclaration ruDataClass = cu.addClass( typeName, Modifier.Keyword.PUBLIC );
        ruDataClass.addImplementedType( RuleUnitData.class.getCanonicalName() );

        ruleUnit.getUnitVarDeclarations().forEach( v -> processVar(ruDataClass, v) );

        return cu.toString();
    }

    private void processVar( ClassOrInterfaceDeclaration ruDataClass, RuleUnitVariable ruVar ) {
        String typeName = ruVar.isDataSource() ?
                DataStore.class.getCanonicalName() + "<" + ruVar.getDataSourceParameterType().getCanonicalName() + ">" :
                ruVar.getType().getCanonicalName();
        String varName = ruVar.getName();

        if (ruVar.isDataSource()) {
            ClassOrInterfaceType dataSourceType = new ClassOrInterfaceType(null, new SimpleName(DataStore.class.getCanonicalName()),
                    new NodeList<Type>(new ClassOrInterfaceType(null, ruVar.getDataSourceParameterType().getCanonicalName())));
            Expression initializer = parseExpression( DataSource.class.getCanonicalName() + ".createStore()" );
            ruDataClass.addFieldWithInitializer( dataSourceType, varName, initializer, Modifier.Keyword.PRIVATE );
        } else {
            ruDataClass.addField( typeName, varName, Modifier.Keyword.PRIVATE );
        }

        MethodDeclaration getter = ruDataClass.addMethod( "get" + ucFirst(varName), Modifier.Keyword.PUBLIC );
        getter.setType( typeName );
        getter.createBody().addStatement( "return this." + varName + ";");

        String setterName = "set" + ucFirst(varName);
        MethodDeclaration setter = ruDataClass.addMethod( setterName, Modifier.Keyword.PUBLIC );
        setter.addParameter( typeName, varName );
        setter.createBody().addStatement( "this." + varName + " = " + varName + ";");
    }
}
