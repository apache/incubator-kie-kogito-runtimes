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
package org.kie.kogito.addons.quarkus.kubernetes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logmanager.Level;
import org.kie.kogito.addons.quarkus.k8s.EndpointCallerProducer;
import org.kie.kogito.addons.quarkus.k8s.EndpointDiscoveryProducer;
import org.kie.kogito.addons.quarkus.k8s.config.ServiceDiscoveryConfigBuilder;
import org.kie.kogito.addons.quarkus.k8s.functions.knative.KnativeServiceRegistryProducer;
import org.kie.kogito.addons.quarkus.k8s.functions.knative.KnativeServiceRegistryRecorder;
import org.kie.kogito.codegen.core.io.CollectedResourceProducer;
import org.kie.kogito.quarkus.addons.common.deployment.AnyEngineKogitoAddOnProcessor;
import org.kie.kogito.quarkus.common.deployment.KogitoBuildContextBuildItem;
import org.kie.kogito.quarkus.serverless.workflow.WorkflowCodeGenUtils;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LogCategoryBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigBuilderBuildItem;
import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

class KogitoAddOnKubernetesProcessor extends AnyEngineKogitoAddOnProcessor {

    private static final String FEATURE = "kogito-addon-kubernetes-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem endpointDiscoveryProducer() {
        return new AdditionalBeanBuildItem(EndpointDiscoveryProducer.class);
    }

    @BuildStep
    public AdditionalBeanBuildItem endpointCallerProducer() {
        return new AdditionalBeanBuildItem(EndpointCallerProducer.class);
    }

    @BuildStep
    void runtimeInitConfigBuilderProducer(BuildProducer<RunTimeConfigBuilderBuildItem> rcb) {
        rcb.produce(new RunTimeConfigBuilderBuildItem(ServiceDiscoveryConfigBuilder.class.getName()));
    }

    /**
     * Defaults the logger to warn to no print it at STATIC_INIT time
     * To enable back just set quarkus.log.category."okhttp3.OkHttpClient".level=INFO
     * 
     * @param categories
     */
    @BuildStep
    public void produceLoggingCategories(BuildProducer<LogCategoryBuildItem> categories) {
        categories.produce(new LogCategoryBuildItem("okhttp3.OkHttpClient", Level.WARN));
    }

    @BuildStep
    AdditionalBeanBuildItem producer() {
        return new AdditionalBeanBuildItem(KnativeServiceRegistryProducer.class);
    }

    @BuildStep
    WorkflowsBuildItem workflowsBuildItem(KogitoBuildContextBuildItem contextBuildItem) {
        Path[] paths = contextBuildItem.getKogitoBuildContext().getAppPaths().getPaths();

        Stream<Path> workflowFiles = CollectedResourceProducer.fromPaths(paths).stream()
                .map(collectedResource -> collectedResource.resource().getSourcePath())
                .map(Paths::get);

        return new WorkflowsBuildItem(WorkflowCodeGenUtils.getWorkflows(workflowFiles));
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    public void registerKnativeServices(WorkflowsBuildItem workflowsBuildItem, KnativeServiceRegistryRecorder knativeServiceRegistryRecorder) {
        List<String> operations = workflowsBuildItem.getWorkflows().stream()
                .map(Workflow::getFunctions)
                .filter(Objects::nonNull)
                .flatMap(functions -> functions.getFunctionDefs().stream())
                .filter(functionDefinition -> functionDefinition.getType() == FunctionDefinition.Type.CUSTOM)
                .map(FunctionDefinition::getOperation)
                .filter(operation -> operation.startsWith("knative:"))
                .map(operation -> operation.replace("knative:", ""))
                .map(operation -> {
                    int index = operation.lastIndexOf('?');

                    if (index >= 0) {
                        return operation.substring(0, index);
                    } else {
                        return operation;
                    }
                })
                .collect(Collectors.toList());

        knativeServiceRegistryRecorder.registerKnativeServices(operations);
    }
}
