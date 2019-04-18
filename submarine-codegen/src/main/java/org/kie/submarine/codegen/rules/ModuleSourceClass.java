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

package org.kie.submarine.codegen.rules;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.kie.submarine.rules.RuleUnit;

public class ModuleSourceClass {

    private final String packageName;
    private final String generatedFilePath;
    private final String targetCanonicalName;
    private final List<RuleUnitSourceClass> ruleUnits;
    private String targetTypeName;
    private boolean hasCdi;

    public ModuleSourceClass() {
        this.packageName = "org.drools.project.model";
        this.targetTypeName = "Module";
        this.targetCanonicalName = packageName + "." + targetTypeName;
        this.generatedFilePath = targetCanonicalName.replace('.', '/') + ".java";
        this.ruleUnits = new ArrayList<>();
    }

    public String generatedFilePath() {
        return generatedFilePath;
    }

    public void addRuleUnit(RuleUnitSourceClass rusc) {
        ruleUnits.add(rusc);
    }

    public String generate() {
        return compilationUnit().toString();
    }

    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit = new CompilationUnit(packageName);
        ClassOrInterfaceDeclaration cls =
                compilationUnit.addClass(targetTypeName);

        for (RuleUnitSourceClass r : ruleUnits) {
            cls.addMember(ruleUnitFactoryMethod(r));
        }

        return compilationUnit;
    }

    public static MethodDeclaration ruleUnitFactoryMethod( RuleUnitSourceClass r) {
        return new MethodDeclaration()
                .addModifier( Modifier.Keyword.PUBLIC)
                .setName("create" + r.targetTypeName())
                .setType(r.targetCanonicalName())
                .setBody(new BlockStmt().addStatement(new ReturnStmt(
                        new ObjectCreationExpr()
                                .setType(r.targetCanonicalName()))));
    }

    public static ClassOrInterfaceType ruleUnitType( String canonicalName) {
        return new ClassOrInterfaceType(null, RuleUnit.class.getCanonicalName())
                .setTypeArguments(new ClassOrInterfaceType(null, canonicalName));
    }

    public ModuleSourceClass withCdi(boolean hasCdi) {
        this.hasCdi = hasCdi;
        return this;
    }

    public List<RuleUnitSourceClass> getRuleUnits() {
        return ruleUnits;
    }
}
