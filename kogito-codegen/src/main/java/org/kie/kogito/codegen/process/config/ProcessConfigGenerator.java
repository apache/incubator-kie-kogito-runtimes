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

package org.kie.kogito.codegen.process.config;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.kogito.codegen.di.DependencyInjectionAnnotator;
import org.kie.kogito.jobs.JobsService;
import org.kie.kogito.process.ProcessEventListenerConfig;
import org.kie.kogito.process.WorkItemHandlerConfig;
import org.kie.kogito.process.impl.CachedProcessEventListenerConfig;
import org.kie.kogito.process.impl.DefaultProcessEventListenerConfig;
import org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig;
import org.kie.kogito.process.impl.StaticProcessConfig;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;
import org.kie.kogito.uow.UnitOfWorkManager;

import static org.kie.kogito.codegen.CodegenUtils.extractOptionalInjection;
import static org.kie.kogito.codegen.CodegenUtils.field;
import static org.kie.kogito.codegen.CodegenUtils.genericType;
import static org.kie.kogito.codegen.CodegenUtils.newObject;
import static org.kie.kogito.codegen.CodegenUtils.parameter;
import static org.kie.kogito.codegen.CodegenUtils.privateField;
import static org.kie.kogito.codegen.CodegenUtils.privateMethod;
import static org.kie.kogito.codegen.ConfigGenerator.callMerge;

public class ProcessConfigGenerator {

    private static final String DEFAULT_WORKITEM_HANDLER_CONFIG = "defaultWorkItemHandlerConfig";
    private static final String DEFAULT_PROCESS_EVENT_LISTENER_CONFIG = "defaultProcessEventListenerConfig";
    private static final String DEFAULT_UNIT_OF_WORK_MANAGER = "defaultUnitOfWorkManager";
    private static final String DEFAULT_JOBS_SEVICE = "defaultJobsService";

    private DependencyInjectionAnnotator annotator;

    private List<BodyDeclaration<?>> members = new ArrayList<>();

    public ObjectCreationExpr newInstance() {
        if (annotator!= null) {
            return new ObjectCreationExpr()
                    .setType(StaticProcessConfig.class.getCanonicalName())
                    .addArgument(new MethodCallExpr("extract_workItemHandlerConfig"))
                    .addArgument(new MethodCallExpr("extract_processEventListenerConfig"))
                    .addArgument(new MethodCallExpr("extract_unitOfWorkManager"))
                    .addArgument(new MethodCallExpr("extract_jobsService"));
        } else {
            return new ObjectCreationExpr()
                .setType(StaticProcessConfig.class.getCanonicalName())
                .addArgument(new NameExpr(DEFAULT_WORKITEM_HANDLER_CONFIG))
                .addArgument(new NameExpr(DEFAULT_PROCESS_EVENT_LISTENER_CONFIG))
                .addArgument(new NameExpr(DEFAULT_UNIT_OF_WORK_MANAGER))
                .addArgument(new NameExpr(DEFAULT_JOBS_SEVICE));
        }
    }

    public List<BodyDeclaration<?>> members() {

        FieldDeclaration defaultWihcFieldDeclaration = privateField(
                WorkItemHandlerConfig.class,
                DEFAULT_WORKITEM_HANDLER_CONFIG,
                newObject(DefaultWorkItemHandlerConfig.class)
        );
        members.add(defaultWihcFieldDeclaration);

        FieldDeclaration defaultUowFieldDeclaration = privateField(
                UnitOfWorkManager.class,
                DEFAULT_UNIT_OF_WORK_MANAGER,
                newObject(DefaultUnitOfWorkManager.class, newObject(CollectingUnitOfWorkFactory.class))
        );
        members.add(defaultUowFieldDeclaration);

        FieldDeclaration defaultJobsServiceFieldDeclaration = privateField(
                JobsService.class,
                DEFAULT_JOBS_SEVICE,
                new NullLiteralExpr()
        );
        members.add(defaultJobsServiceFieldDeclaration);

        if (annotator != null) {
            FieldDeclaration wihcFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.optionalInstanceInjectionType(), WorkItemHandlerConfig.class),
                    "workItemHandlerConfig"
            ));
            members.add(wihcFieldDeclaration);

            FieldDeclaration uowmFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.optionalInstanceInjectionType(), UnitOfWorkManager.class),
                    "unitOfWorkManager"
            ));
            members.add(uowmFieldDeclaration);

            FieldDeclaration jobsServiceFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.optionalInstanceInjectionType(), JobsService.class),
                    "jobsService"
            ));
            members.add(jobsServiceFieldDeclaration);

            FieldDeclaration pelcFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.multiInstanceInjectionType(), ProcessEventListenerConfig.class),
                    "processEventListenerConfigs"
            ));
            members.add(pelcFieldDeclaration);

            FieldDeclaration pelFieldDeclaration = annotator.withInjection(field(
                    genericType(annotator.multiInstanceInjectionType(), ProcessEventListener.class),
                    "processEventListeners"
            ));
            members.add(pelFieldDeclaration);

            members.add(extractOptionalInjection(WorkItemHandlerConfig.class.getCanonicalName(), "workItemHandlerConfig", DEFAULT_WORKITEM_HANDLER_CONFIG, annotator));
            members.add(extractOptionalInjection(UnitOfWorkManager.class.getCanonicalName(), "unitOfWorkManager", DEFAULT_UNIT_OF_WORK_MANAGER, annotator));
            members.add(extractOptionalInjection(JobsService.class.getCanonicalName(), "jobsService", DEFAULT_JOBS_SEVICE, annotator));

            members.add(generateExtractEventListenerConfigMethod());
            members.add(generateMergeEventListenerConfigMethod());
        } else {
            FieldDeclaration defaultPelcFieldDeclaration = privateField(
                    ProcessEventListenerConfig.class,
                    DEFAULT_PROCESS_EVENT_LISTENER_CONFIG,
                    newObject(DefaultProcessEventListenerConfig.class)
            );
            members.add(defaultPelcFieldDeclaration);
        }

        return members;
    }

    public ProcessConfigGenerator withDependencyInjection(DependencyInjectionAnnotator annotator) {
        this.annotator = annotator;
        return this;
    }

    private MethodDeclaration generateExtractEventListenerConfigMethod() {
        BlockStmt body = new BlockStmt().addStatement(new ReturnStmt(
                new MethodCallExpr(new ThisExpr(), "merge_processEventListenerConfig", NodeList.nodeList(
                        annotator.getMultiInstance("processEventListenerConfigs"),
                        annotator.getMultiInstance("processEventListeners")
                ))
        ));

        return privateMethod(ProcessEventListenerConfig.class, "extract_processEventListenerConfig", body);
    }

    private MethodDeclaration generateMergeEventListenerConfigMethod() {
        BlockStmt body = new BlockStmt().addStatement(new ReturnStmt(newObject(CachedProcessEventListenerConfig.class,
                callMerge(
                        "processEventListenerConfigs",
                        ProcessEventListenerConfig.class, "listeners",
                        "processEventListeners"
                )
        )));

        return privateMethod(ProcessEventListenerConfig.class, "merge_processEventListenerConfig",
                NodeList.nodeList(
                        parameter(genericType(List.class, ProcessEventListenerConfig.class), "processEventListenerConfigs"),
                        parameter(genericType(List.class, ProcessEventListener.class), "processEventListeners")
                ),
                body);
    }

}
