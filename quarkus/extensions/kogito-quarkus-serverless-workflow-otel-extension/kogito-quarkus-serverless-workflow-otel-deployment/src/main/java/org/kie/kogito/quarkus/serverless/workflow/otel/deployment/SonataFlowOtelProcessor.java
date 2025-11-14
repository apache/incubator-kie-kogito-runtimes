/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.quarkus.serverless.workflow.otel.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

/**
 * Quarkus deployment processor for SonataFlow OpenTelemetry integration.
 *
 * This processor handles build-time configuration and registration of OpenTelemetry
 * components for serverless workflow node-level tracing and monitoring.
 */
class SonataFlowOtelProcessor {

    private static final String FEATURE_NAME = "sonataflow-otel";

    /**
     * Register the SonataFlow OpenTelemetry feature.
     * This is always registered to indicate the extension is available.
     */
    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE_NAME);
    }

    /**
     * Register OpenTelemetry beans for CDI injection.
     *
     * This build step registers the core OpenTelemetry components as CDI beans
     * so they can be injected and used throughout the application.
     *
     * Components registered:
     * - OtelEventListenerFactory: Factory for creating NodeOtelEventListener instances
     * - NodeSpanManager: Manages OpenTelemetry spans for workflow nodes
     * - HeaderContextExtractor: Extracts context from HTTP headers
     * - OtelHeaderFilter: JAX-RS filter for automatic header processing
     * - ProcessEventHandler: Handles process lifecycle events (start, complete, error)
     * - TestSpanExporterProducer: In-memory span exporter for testing (test mode only)
     * - TestSpanResource: REST endpoint to access spans in tests (test mode only)
     */
    @BuildStep
    void registerOpenTelemetryBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            LaunchModeBuildItem launchMode) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass("org.kie.kogito.quarkus.serverless.workflow.otel.OtelEventListenerFactory")
                .addBeanClass("org.kie.kogito.quarkus.serverless.workflow.otel.NodeSpanManager")
                .addBeanClass("org.kie.kogito.quarkus.serverless.workflow.otel.HeaderContextExtractor")
                .addBeanClass("org.kie.kogito.quarkus.serverless.workflow.otel.OtelHeaderFilter")
                .addBeanClass("org.kie.kogito.quarkus.serverless.workflow.otel.ProcessEventHandler")
                .setUnremovable()
                .build());

        if (launchMode.getLaunchMode().isDevOrTest()) {
            additionalBeans.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass("org.kie.kogito.quarkus.serverless.workflow.otel.test.TestSpanExporterProducer")
                    .addBeanClass("org.kie.kogito.quarkus.serverless.workflow.otel.test.TestSpanResource")
                    .setUnremovable()
                    .build());
        }
    }

    /**
     * Register classes for reflection in native image builds.
     *
     * OpenTelemetry components need to be available for reflection to work
     * properly in native image mode.
     */
    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, true,
                "org.kie.kogito.quarkus.serverless.workflow.otel.OtelEventListenerFactory",
                "org.kie.kogito.quarkus.serverless.workflow.otel.NodeOtelEventListener",
                "org.kie.kogito.quarkus.serverless.workflow.otel.NodeSpanManager",
                "org.kie.kogito.quarkus.serverless.workflow.otel.HeaderContextExtractor",
                "org.kie.kogito.quarkus.serverless.workflow.otel.OtelHeaderFilter",
                "org.kie.kogito.quarkus.serverless.workflow.otel.OtelContextHolder",
                "org.kie.kogito.quarkus.serverless.workflow.otel.ProcessEventHandler"));
    }

}
