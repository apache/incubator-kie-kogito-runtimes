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

package org.kie.kogito.codegen.decision;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.codegen.AbstractApplicationSection;

public class DecisionContainerGenerator extends AbstractApplicationSection {

    public DecisionContainerGenerator() {
        super(null, null, Void.class);
    }

    @Override
    public ClassOrInterfaceDeclaration classDeclaration() {
        return null;
    }

    @Override
    public FieldDeclaration fieldDeclaration() {
        FieldDeclaration dmnRuntimeField = new FieldDeclaration().addModifier(Modifier.Keyword.STATIC)
                                                                 .addVariable(new VariableDeclarator().setType(DMNRuntime.class.getCanonicalName())
                                                                                                      .setName("dmnRuntime")
                                                                                                      .setInitializer(new MethodCallExpr("org.kie.dmn.kogito.rest.quarkus.DMNKogitoQuarkus.createGenericDMNRuntime")));
        return dmnRuntimeField;
    }

    @Override
    public MethodDeclaration factoryMethod() {
        MethodDeclaration dmnRuntimeMethod = new MethodDeclaration().addModifier(Modifier.Keyword.PUBLIC)
                                                                    .setName("decisions")
                                                                    .setType(DMNRuntime.class.getCanonicalName())
                                                                    .setBody(new BlockStmt().addStatement(new ReturnStmt(new NameExpr("dmnRuntime"))));
        return dmnRuntimeMethod;
    }

}
