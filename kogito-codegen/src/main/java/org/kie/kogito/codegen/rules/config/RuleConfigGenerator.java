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

package org.kie.kogito.codegen.rules.config;

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
import org.drools.core.config.CachedRuleEventListenerConfig;
import org.drools.core.config.DefaultRuleEventListenerConfig;
import org.drools.core.config.StaticRuleConfig;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.rules.RuleEventListenerConfig;

import static org.kie.kogito.codegen.CodegenUtils.field;
import static org.kie.kogito.codegen.CodegenUtils.genericType;
import static org.kie.kogito.codegen.CodegenUtils.newObject;
import static org.kie.kogito.codegen.CodegenUtils.parameter;
import static org.kie.kogito.codegen.CodegenUtils.privateField;
import static org.kie.kogito.codegen.CodegenUtils.privateMethod;
import static org.kie.kogito.codegen.ConfigGenerator.callMerge;

public class RuleConfigGenerator {

    private static final String DEFAULT_RULE_EVENT_LISTENER_CONFIG = "defaultRuleEventListenerConfig";

    private List<BodyDeclaration<?>> members = new ArrayList<>();

    private DependencyInjectionAnnotator annotator;

    public ObjectCreationExpr newInstance() {
        if (annotator!= null) {
            return new ObjectCreationExpr()
                    .setType(StaticRuleConfig.class.getCanonicalName())
                    .addArgument(new MethodCallExpr("extract_ruleEventListenerConfig"));
        } else {
            return new ObjectCreationExpr()
                .setType(StaticRuleConfig.class.getCanonicalName())
                .addArgument(new NameExpr(DEFAULT_RULE_EVENT_LISTENER_CONFIG));
        }
    }
    public List<BodyDeclaration<?>> members() {

        if (annotator != null) {
            FieldDeclaration relcFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.multiInstanceInjectionType(), RuleEventListenerConfig.class),
                    "ruleEventListenerConfigs"
            ));
            members.add(relcFieldDeclaration);

            FieldDeclaration aelFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.multiInstanceInjectionType(), AgendaEventListener.class),
                    "agendaEventListeners"
            ));
            members.add(aelFieldDeclaration);

            FieldDeclaration rrelFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.multiInstanceInjectionType(), RuleRuntimeEventListener.class),
                    "ruleRuntimeEventListeners"
            ));
            members.add(rrelFieldDeclaration);

            members.add(generateExtractEventListenerConfigMethod());
            members.add(generateMergeEventListenerConfigMethod());
        } else {
            FieldDeclaration defaultRelcFieldDeclaration = privateField(
                    RuleEventListenerConfig.class,
                    DEFAULT_RULE_EVENT_LISTENER_CONFIG,
                    newObject(DefaultRuleEventListenerConfig.class)
            );
            members.add(defaultRelcFieldDeclaration);
        }

        return members;
    }

    public RuleConfigGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        return this;
    }

    private MethodDeclaration generateExtractEventListenerConfigMethod() {
        BlockStmt body = new BlockStmt().addStatement(new ReturnStmt(
                new MethodCallExpr(new ThisExpr(), "merge_ruleEventListenerConfig", NodeList.nodeList(
                        annotator.getMultiInstance("ruleEventListenerConfigs"),
                        annotator.getMultiInstance("agendaEventListeners"),
                        annotator.getMultiInstance("ruleRuntimeEventListeners")
                ))
        ));

        return privateMethod(RuleEventListenerConfig.class, "extract_ruleEventListenerConfig", body);
    }

    private MethodDeclaration generateMergeEventListenerConfigMethod() {
        BlockStmt body = new BlockStmt().addStatement(new ReturnStmt(newObject(CachedRuleEventListenerConfig.class,
                callMerge(
                        "ruleEventListenerConfigs",
                        RuleEventListenerConfig.class, "agendaListeners",
                        "agendaEventListeners"
                ),
                callMerge(
                        "ruleEventListenerConfigs",
                        RuleEventListenerConfig.class, "ruleRuntimeListeners",
                        "ruleRuntimeEventListeners"
                )
        )));

        return privateMethod(RuleEventListenerConfig.class, "merge_ruleEventListenerConfig",
                NodeList.nodeList(
                        parameter(genericType(List.class, RuleEventListenerConfig.class), "ruleEventListenerConfigs"),
                        parameter(genericType(List.class, AgendaEventListener.class), "agendaEventListeners"),
                        parameter(genericType(List.class, RuleRuntimeEventListener.class), "ruleRuntimeEventListeners")
                ),
                body);
    }

}
