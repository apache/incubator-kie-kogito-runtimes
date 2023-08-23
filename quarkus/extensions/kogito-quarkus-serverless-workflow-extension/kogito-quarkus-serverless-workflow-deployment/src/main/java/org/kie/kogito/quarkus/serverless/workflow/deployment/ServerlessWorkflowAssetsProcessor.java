/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.quarkus.serverless.workflow.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.codegen.common.GeneratedFile;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.process.ProcessContainerGenerator;
import org.kie.kogito.codegen.process.ProcessGenerator;
import org.kie.kogito.event.process.NodeInstanceEventBody;
import org.kie.kogito.event.process.ProcessDataEvent;
import org.kie.kogito.event.process.ProcessErrorEventBody;
import org.kie.kogito.event.process.ProcessInstanceDataEvent;
import org.kie.kogito.event.process.ProcessInstanceEventBody;
import org.kie.kogito.event.process.VariableInstanceDataEvent;
import org.kie.kogito.event.process.VariableInstanceEventBody;
import org.kie.kogito.process.expr.ExpressionHandler;
import org.kie.kogito.quarkus.common.deployment.KogitoAddonsPreGeneratedSourcesBuildItem;
import org.kie.kogito.quarkus.common.deployment.KogitoBuildContextBuildItem;
import org.kie.kogito.quarkus.common.deployment.LiveReloadExecutionBuildItem;
import org.kie.kogito.quarkus.extensions.spi.deployment.KogitoProcessContainerGeneratorBuildItem;
import org.kie.kogito.quarkus.serverless.workflow.WorkflowHandlerGeneratedFile;
import org.kie.kogito.quarkus.serverless.workflow.WorkflowHandlerGenerator;
import org.kie.kogito.quarkus.serverless.workflow.openapi.ServerlessWorkflowOASFilter;
import org.kie.kogito.quarkus.serverless.workflow.openapi.WorkflowOpenApiHandlerGenerator;
import org.kie.kogito.quarkus.serverless.workflow.rpc.WorkflowRPCHandlerGenerator;
import org.kie.kogito.quarkus.workflow.deployment.WorkflowProcessor;
import org.kie.kogito.serverless.workflow.operationid.WorkflowOperationIdFactory;
import org.kie.kogito.serverless.workflow.parser.FunctionNamespace;
import org.kie.kogito.serverless.workflow.parser.FunctionTypeHandler;
import org.kie.kogito.serverless.workflow.parser.schema.OpenApiModelSchemaGenerator;
import org.kie.kogito.serverless.workflow.rpc.FileDescriptorHolder;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.pkg.steps.NativeOrNativeSourcesBuild;
import io.quarkus.smallrye.openapi.deployment.spi.AddToOpenAPIDefinitionBuildItem;

/**
 * Main class of the Kogito Serverless Workflow extension
 */
public class ServerlessWorkflowAssetsProcessor extends WorkflowProcessor {

    // Injecting Instance<WorkflowOpenApiHandlerGenerator> does not work here
    private static WorkflowHandlerGenerator[] generators = { WorkflowOpenApiHandlerGenerator.instance, WorkflowRPCHandlerGenerator.instance };

    @BuildStep
    @Override
    public FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("kogito-serverless-workflow");
    }

    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    NativeImageResourceBuildItem addExpressionHandlers(BuildProducer<ServiceProviderBuildItem> serviceProvider) {
        serviceProvider.produce(ServiceProviderBuildItem.allProvidersFromClassPath(ExpressionHandler.class.getCanonicalName()));
        serviceProvider.produce(ServiceProviderBuildItem.allProvidersFromClassPath(FunctionNamespace.class.getCanonicalName()));
        serviceProvider.produce(ServiceProviderBuildItem.allProvidersFromClassPath(FunctionTypeHandler.class.getCanonicalName()));
        serviceProvider.produce(ServiceProviderBuildItem.allProvidersFromClassPath(WorkflowOperationIdFactory.class.getCanonicalName()));
        return new NativeImageResourceBuildItem(FileDescriptorHolder.DESCRIPTOR_PATH);
    }

    @BuildStep
    void addWorkItemHandlers(KogitoBuildContextBuildItem contextBI, LiveReloadExecutionBuildItem liveReloadExecutionBuildItem, BuildProducer<KogitoAddonsPreGeneratedSourcesBuildItem> sources) {
        KogitoBuildContext context = contextBI.getKogitoBuildContext();
        IndexView index = liveReloadExecutionBuildItem.getIndexView();
        Collection<GeneratedFile> generatedFiles = new ArrayList<>();
        for (WorkflowHandlerGenerator generator : generators) {
            for (WorkflowHandlerGeneratedFile generated : generator.generateHandlerClasses(context, index)) {
                generatedFiles.add(generated);
                context.addGeneratedHandler(generated.getWorkItemHandlerName());
            }
        }
        sources.produce(new KogitoAddonsPreGeneratedSourcesBuildItem(generatedFiles));
    }

    @BuildStep
    void addOpenAPIModelSchema(List<KogitoProcessContainerGeneratorBuildItem> processBuildItem, BuildProducer<AddToOpenAPIDefinitionBuildItem> openAPIProducer) {
        Map<String, Schema> schemasInfo = new HashMap<>();
        Map<String, Schema> defsSchemas = new HashMap<>();
        processBuildItem.stream().flatMap(it -> it.getProcessContainerGenerators().stream())
                .map(ProcessContainerGenerator::getProcesses).flatMap(Collection::stream).map(ProcessGenerator::getProcess)
                .forEach(process -> OpenApiModelSchemaGenerator.addOpenAPIModelSchema(process, schemasInfo, defsSchemas));
        if (!schemasInfo.isEmpty()) {
            openAPIProducer.produce(new AddToOpenAPIDefinitionBuildItem(new ServerlessWorkflowOASFilter(schemasInfo, defsSchemas)));
        }
    }

    @BuildStep
    public ReflectiveClassBuildItem eventsApiReflection() {
        return new ReflectiveClassBuildItem(true, true,
                NodeInstanceEventBody.class.getName(),
                ProcessDataEvent.class.getName(),
                ProcessErrorEventBody.class.getName(),
                ProcessInstanceDataEvent.class.getName(),
                ProcessInstanceEventBody.class.getName(),
                VariableInstanceDataEvent.class.getName(),
                VariableInstanceEventBody.class.getName());
    }

    @BuildStep
    IndexDependencyBuildItem addJsonSchemaValidatorToIndex() {
        return new IndexDependencyBuildItem("com.networknt", "json-schema-validator");
    }

    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    public void registerJsonValidatorSubclassesForReflection(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        combinedIndexBuildItem.getComputingIndex().getAllKnownImplementors(DotName.createSimple("com.networknt.schema.JsonValidator"))
                .forEach(c -> reflectiveClass.produce(new ReflectiveClassBuildItem(true, true, c.name().toString())));
    }
}
