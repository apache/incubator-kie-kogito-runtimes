/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.codegen.rules;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import org.kie.kogito.codegen.AbstractApplicationSection;
import org.kie.kogito.codegen.BodyDeclarationComparator;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.rules.KieRuntimeBuilder;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.units.impl.AbstractRuleUnits;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.WildcardType;

public class RuleUnitContainerGenerator extends AbstractApplicationSection {

    private final List<RuleUnitGenerator> ruleUnits;
    private String targetTypeName;
    private DependencyInjectionAnnotator annotator;
    private List<BodyDeclaration<?>> factoryMethods = new ArrayList<>();

    public RuleUnitContainerGenerator() {
        super("RuleUnits", "ruleUnits", AbstractRuleUnits.class);
        this.targetTypeName = "Module";
        this.ruleUnits = new ArrayList<>();
    }

    void addRuleUnit(RuleUnitGenerator rusc) {
        ruleUnits.add(rusc);
    }

    private MethodDeclaration genericFactoryById() {
        ClassOrInterfaceType returnType = new ClassOrInterfaceType(null, RuleUnit.class.getCanonicalName())
                .setTypeArguments(new WildcardType());

        SwitchStmt switchStmt = new SwitchStmt();
        switchStmt.setSelector(new NameExpr("fqcn"));

        for (RuleUnitGenerator ruleUnit : ruleUnits) {
            SwitchEntry switchEntry = new SwitchEntry();
            switchEntry.getLabels().add(new StringLiteralExpr(ruleUnit.getRuleUnitDescription().getCanonicalName()));
            ObjectCreationExpr ruleUnitConstructor = new ObjectCreationExpr()
                    .setType(ruleUnit.targetCanonicalName())
                    .addArgument("application");
            switchEntry.getStatements().add(new ReturnStmt(ruleUnitConstructor));
            switchStmt.getEntries().add(switchEntry);
        }

        SwitchEntry defaultEntry = new SwitchEntry();
        defaultEntry.getStatements().add(new ThrowStmt(new ObjectCreationExpr().setType(UnsupportedOperationException.class.getCanonicalName())));
        switchStmt.getEntries().add(defaultEntry);

        return new MethodDeclaration()
                .addModifier(Modifier.Keyword.PROTECTED)
                .setType(returnType)
                .setName("create")
                .addParameter(String.class, "fqcn")
                .setBody(new BlockStmt().addStatement(switchStmt));
    }

    @Override
    public ClassOrInterfaceDeclaration classDeclaration() {

        NodeList<BodyDeclaration<?>> declarations = new NodeList<>();

        // declare field `application`
        FieldDeclaration applicationFieldDeclaration = new FieldDeclaration();
        applicationFieldDeclaration
                .addVariable( new VariableDeclarator( new ClassOrInterfaceType(null, "Application"), "application") )
                .setModifiers( Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL );
        declarations.add(applicationFieldDeclaration);

        ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration("RuleUnits")
                .addModifier(Modifier.Keyword.PUBLIC)
                .addParameter( "Application", "application" )
                .setBody( new BlockStmt().addStatement( "this.application = application;" ) );
        declarations.add(constructorDeclaration);

        // declare field `ruleRuntimeBuilder`
        FieldDeclaration kieRuntimeFieldDeclaration = new FieldDeclaration();
        kieRuntimeFieldDeclaration
                .addVariable(new VariableDeclarator( new ClassOrInterfaceType(null, KieRuntimeBuilder.class.getCanonicalName()), "ruleRuntimeBuilder")
                .setInitializer(new ObjectCreationExpr().setType(ProjectSourceClass.PROJECT_RUNTIME_CLASS)))
                .setModifiers( Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL );
        declarations.add(kieRuntimeFieldDeclaration);

        // declare method ruleRuntimeBuilder()
        MethodDeclaration methodDeclaration = new MethodDeclaration()
                .addModifier(Modifier.Keyword.PUBLIC)
                .setName("ruleRuntimeBuilder")
                .setType(KieRuntimeBuilder.class.getCanonicalName())
                .setBody(new BlockStmt().addStatement(new ReturnStmt(new FieldAccessExpr(new ThisExpr(), "ruleRuntimeBuilder"))));
        declarations.add(methodDeclaration);

        declarations.addAll(factoryMethods);
        declarations.add(genericFactoryById());

        ClassOrInterfaceDeclaration cls = super.classDeclaration()
                .setMembers(declarations);

        cls.getMembers().sort(new BodyDeclarationComparator());

        return cls;
    }

    public RuleUnitContainerGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        return this;
    }

    List<RuleUnitGenerator> getRuleUnits() {
        return ruleUnits;
    }
}
