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

import org.kie.kogito.process.expr.ExpressionHandler;
import org.kie.kogito.quarkus.common.deployment.KogitoAddonsPreGeneratedSourcesBuildItem;
import org.kie.kogito.quarkus.common.deployment.KogitoBuildContextBuildItem;
import org.kie.kogito.quarkus.serverless.openapi.WorkflowOpenApiHandlerGenerator;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.pkg.steps.NativeOrNativeSourcesBuild;

/**
 * Main class of the Kogito Serverless Workflow extension
 */
public class ServerlessWorkflowAssetsProcessor {

    @BuildStep
    FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("kogito-serverless-workflow");
    }

    @BuildStep(onlyIf = NativeOrNativeSourcesBuild.class)
    void addExpressionHandlers(BuildProducer<ServiceProviderBuildItem> serviceProvider) {
        serviceProvider.produce(ServiceProviderBuildItem.allProvidersFromClassPath(ExpressionHandler.class.getCanonicalName()));
    }

    @BuildStep
    void addOpenAPIWorkItemHandler(KogitoBuildContextBuildItem contextBI, CombinedIndexBuildItem indexBuildItem, BuildProducer<KogitoAddonsPreGeneratedSourcesBuildItem> sources) {
        sources.produce(new KogitoAddonsPreGeneratedSourcesBuildItem(WorkflowOpenApiHandlerGenerator.generateHandlerClasses(contextBI.getKogitoBuildContext(), indexBuildItem.getIndex())));
    }

}
