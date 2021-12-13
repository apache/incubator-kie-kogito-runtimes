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
package org.kie.kogito.addons.quarkus.knative.eventing.deployment;

import java.util.List;

import org.kie.kogito.quarkus.common.deployment.KogitoApplicationSectionBuildItem;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedFileSystemResourceBuildItem;
import io.quarkus.kubernetes.spi.GeneratedKubernetesResourceBuildItem;

public class KogitoAddOnKnativeEventingProcessor {

    private static final String FEATURE = "kogito-addon-knative-eventing-extension";

    private static final String KNATIVE = "knative";

    @BuildStep
    FeatureBuildItem feature() {
        System.out.println("***************** feature ************");
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void buildKnativeResources(List<GeneratedKubernetesResourceBuildItem> generatedKubernetesManifests,
            BuildProducer<GeneratedFileSystemResourceBuildItem> generatedCSVs,
            List<KogitoApplicationSectionBuildItem> applicationSections) {
        generatedKubernetesManifests.size();
        System.out.println("***************** buildKnativeResources ************");
    }

    @BuildStep
    void doSomeCoolStuff(Capabilities capabilities) {
        System.out.println("***************** doSomeCoolStuff ************" + capabilities.getCapabilities());
        if (capabilities.isPresent(Capability.TRANSACTIONS)) {
            // do something only if JTA transactions are in...
        }
    }

    // gets the vanilla

    // gets the openshift ones

    // generate the KogitoSource or SinkBindings

    // generate the Knative Triggers
}
