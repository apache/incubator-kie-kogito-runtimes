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

import org.kie.kogito.quarkus.addons.common.deployment.AnyEngineKogitoAddOnProcessor;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ApplicationInfoBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.kubernetes.deployment.KnativeConfig;
import io.quarkus.kubernetes.deployment.KubernetesConfigUtil;
import io.quarkus.kubernetes.spi.KubernetesResourceMetadataBuildItem;

class KogitoAddOnKnativeEventingProcessor extends AnyEngineKogitoAddOnProcessor {

    private static final String FEATURE = "kogito-addon-knative-eventing-extension";

    private static final String KNATIVE = "knative";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void checkKnativeEnabled(ApplicationInfoBuildItem applicationInfo, KnativeConfig config,
            BuildProducer<KubernetesResourceMetadataBuildItem> resourceMeta) {
        List<String> targets = KubernetesConfigUtil.getUserSpecifiedDeploymentTargets();
        boolean knativeEnabled = targets.contains(KNATIVE);

        // gets the knative target service
    }

    // gets the vanilla

    // gets the openshift ones

    // generate the KogitoSource or SinkBindings

    // generate the Knative Triggers


}
