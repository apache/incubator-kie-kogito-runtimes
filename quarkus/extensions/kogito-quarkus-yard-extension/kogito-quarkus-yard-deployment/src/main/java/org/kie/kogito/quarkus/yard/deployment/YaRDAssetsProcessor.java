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
package org.kie.kogito.quarkus.yard.deployment;

import java.util.ArrayList;
import java.util.List;

import org.kie.kogito.quarkus.common.deployment.KogitoBuildContextBuildItem;
import org.kie.kogito.quarkus.common.deployment.KogitoGeneratedSourcesBuildItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyIgnoreWarningBuildItem;

/**
 * Main class of the Kogito decisions extension
 */
public class YaRDAssetsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(YaRDAssetsProcessor.class);

    @BuildStep
    FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("kogito-yard");
    }

    @BuildStep
    public List<ReflectiveHierarchyIgnoreWarningBuildItem> reflectiveDMNREST() {
        List<ReflectiveHierarchyIgnoreWarningBuildItem> result = new ArrayList<>();
        return result;
    }

    /**
     * Constrained:
     * 1. conflicted with having a separate BuildStep with signature: public List<ReflectiveClassBuildItem>
     * reflectiveClassBuildItems() {
     * so it includes the code from that original method.
     * 2. need to be triggered by Quarkus AFTER the Kogito Codegen, hence this BuildStep "depends" on
     * KogitoGeneratedSourcesBuildItem.
     */
    @BuildStep
    public void stronglyTypeAdditionalClassesForReflection(KogitoGeneratedSourcesBuildItem generatedKogitoClasses, //
            // Constrain 1
            BuildProducer<ReflectiveClassBuildItem> additionalClassesForReflection,
            KogitoBuildContextBuildItem kogitoBuildContextBuildItem,
            Capabilities capabilities) {
    }

    @BuildStep
    public ReflectiveClassBuildItem dmnRuntimeServiceReflectiveClass() {
        logger.debug("dmnRuntimeServiceReflectiveClass()");
        return null;
    }
}
