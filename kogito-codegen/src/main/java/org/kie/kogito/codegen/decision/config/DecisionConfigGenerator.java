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

package org.kie.kogito.codegen.decision.config;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.kie.kogito.codegen.CodegenUtils;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.decision.DecisionEventListenerConfig;
import org.kie.kogito.dmn.config.DefaultDecisionEventListenerConfig;
import org.kie.kogito.dmn.config.StaticDecisionConfig;

public class DecisionConfigGenerator {

    private static final String DEFAULT_PROCESS_EVENT_LISTENER_CONFIG = "defaultDecisionEventListenerConfig";

    private DependencyInjectionAnnotator annotator;

    private List<BodyDeclaration<?>> members = new ArrayList<>();

    public ObjectCreationExpr newInstance() {
        if (annotator != null) {
            return new ObjectCreationExpr()
                    .setType(StaticDecisionConfig.class.getCanonicalName())
                    .addArgument(new MethodCallExpr("extract_decisionEventListenerConfig"));
        } else {
            return new ObjectCreationExpr()
                    .setType(StaticDecisionConfig.class.getCanonicalName())
                    .addArgument(new NameExpr(DEFAULT_PROCESS_EVENT_LISTENER_CONFIG));
        }
    }

    public List<BodyDeclaration<?>> members() {

        FieldDeclaration defaultDelcFieldDeclaration = new FieldDeclaration()
                .setModifiers(Keyword.PRIVATE)
                .addVariable(new VariableDeclarator(new ClassOrInterfaceType(null, DecisionEventListenerConfig.class.getCanonicalName()),
                        DEFAULT_PROCESS_EVENT_LISTENER_CONFIG,
                        new ObjectCreationExpr(null, new ClassOrInterfaceType(null, DefaultDecisionEventListenerConfig.class.getCanonicalName()), NodeList.nodeList())));
        members.add(defaultDelcFieldDeclaration);

        if (annotator != null) {
            FieldDeclaration delcFieldDeclaration = new FieldDeclaration()
                    .addVariable(new VariableDeclarator(new ClassOrInterfaceType(null, new SimpleName(annotator.optionalInstanceInjectionType()), NodeList.nodeList(new ClassOrInterfaceType(null, DecisionEventListenerConfig.class.getCanonicalName()))), "decisionEventListenerConfig"));
            annotator.withInjection(delcFieldDeclaration);

            members.add(delcFieldDeclaration);

            members.add(CodegenUtils.extractOptionalInjection(DecisionEventListenerConfig.class.getCanonicalName(), "decisionEventListenerConfig", DEFAULT_PROCESS_EVENT_LISTENER_CONFIG, annotator));
        }

        return members;
    }

    public DecisionConfigGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        return this;
    }

}
