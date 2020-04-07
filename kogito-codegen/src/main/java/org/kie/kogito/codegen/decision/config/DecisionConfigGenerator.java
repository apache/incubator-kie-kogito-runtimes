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

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.decision.DecisionEventListenerConfig;
import org.kie.kogito.dmn.config.CachedDecisionEventListenerConfig;
import org.kie.kogito.dmn.config.DefaultDecisionEventListenerConfig;
import org.kie.kogito.dmn.config.StaticDecisionConfig;

import static org.kie.kogito.codegen.CodegenUtils.field;
import static org.kie.kogito.codegen.CodegenUtils.genericType;
import static org.kie.kogito.codegen.CodegenUtils.newObject;
import static org.kie.kogito.codegen.CodegenUtils.parameter;
import static org.kie.kogito.codegen.CodegenUtils.privateField;
import static org.kie.kogito.codegen.CodegenUtils.privateMethod;
import static org.kie.kogito.codegen.ConfigGenerator.callMerge;

public class DecisionConfigGenerator {

    private static final String DEFAULT_DECISION_EVENT_LISTENER_CONFIG = "defaultDecisionEventListenerConfig";

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
                    .addArgument(new NameExpr(DEFAULT_DECISION_EVENT_LISTENER_CONFIG));
        }
    }

    public List<BodyDeclaration<?>> members() {

        if (annotator != null) {
            FieldDeclaration delcFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.multiInstanceInjectionType(), DecisionEventListenerConfig.class),
                    "decisionEventListenerConfigs"
            ));
            members.add(delcFieldDeclaration);

            FieldDeclaration drelFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.multiInstanceInjectionType(), DMNRuntimeEventListener.class),
                    "dmnRuntimeEventListeners"
            ));
            members.add(drelFieldDeclaration);

            members.add(generateExtractEventListenerConfigMethod());
            members.add(generateMergeEventListenerConfigMethod());
        } else {
            FieldDeclaration defaultDelcFieldDeclaration = privateField(
                    DecisionEventListenerConfig.class,
                    DEFAULT_DECISION_EVENT_LISTENER_CONFIG,
                    newObject(DefaultDecisionEventListenerConfig.class)
            );
            members.add(defaultDelcFieldDeclaration);
        }

        return members;
    }

    public DecisionConfigGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        return this;
    }

    private MethodDeclaration generateExtractEventListenerConfigMethod() {
        BlockStmt body = new BlockStmt().addStatement(new ReturnStmt(
                new MethodCallExpr(new ThisExpr(), "merge_decisionEventListenerConfig", NodeList.nodeList(
                        annotator.getMultiInstance("decisionEventListenerConfigs"),
                        annotator.getMultiInstance("dmnRuntimeEventListeners")
                ))
        ));

        return privateMethod(DecisionEventListenerConfig.class, "extract_decisionEventListenerConfig", body);
    }

    private MethodDeclaration generateMergeEventListenerConfigMethod() {
        BlockStmt body = new BlockStmt().addStatement(new ReturnStmt(newObject(CachedDecisionEventListenerConfig.class,
                callMerge(
                        "decisionEventListenerConfigs",
                        DecisionEventListenerConfig.class, "listeners",
                        "dmnRuntimeEventListeners"
                )
        )));

        return privateMethod(DecisionEventListenerConfig.class, "merge_decisionEventListenerConfig",
                NodeList.nodeList(
                        parameter(genericType(List.class, DecisionEventListenerConfig.class), "decisionEventListenerConfigs"),
                        parameter(genericType(List.class, DMNRuntimeEventListener.class), "dmnRuntimeEventListeners")
                ),
                body);
    }

}
