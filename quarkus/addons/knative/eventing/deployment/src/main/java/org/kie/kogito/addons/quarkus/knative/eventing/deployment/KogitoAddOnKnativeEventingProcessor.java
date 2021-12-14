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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jbpm.ruleflow.core.Metadata;
import org.kie.api.definition.process.Node;
import org.kie.kogito.codegen.process.ProcessContainerGenerator;
import org.kie.kogito.event.CloudEventMeta;
import org.kie.kogito.event.EventKind;
import org.kie.kogito.quarkus.processes.deployment.KogitoProcessContainerGeneratorBuildItem;

import io.fabric8.knative.eventing.v1.Trigger;
import io.fabric8.knative.sources.v1.SinkBinding;
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
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void buildProcessKnativeResources(List<GeneratedKubernetesResourceBuildItem> generatedKubernetesManifests,
            BuildProducer<GeneratedFileSystemResourceBuildItem> generatedKogitoK8sRes,
            KogitoProcessContainerGeneratorBuildItem processContainerBuildItem) {
        final Set<CloudEventMeta> cloudEvents = this.extractCloudEvents(processContainerBuildItem.getProcessContainerGenerators());
        // generate knative resources
    }

    private List<SinkBinding> generateSinkBindings(Set<CloudEventMeta> cloudEvents) {
        return null;
    }

    private List<Trigger> generateTriggers(Set<CloudEventMeta> cloudEvents) {
        return null;
    }

    private Set<CloudEventMeta> extractCloudEvents(final Set<ProcessContainerGenerator> containers) {
        return containers.stream().flatMap(container -> container
                .getProcesses()
                .stream()
                .flatMap(processor -> processor.getProcessExecutable().process().getNodesRecursively().stream()))
                .filter(node -> node.getMetaData().get(Metadata.TRIGGER_TYPE) != null)
                .map(this::buildCloudEventMetaFromNode)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private CloudEventMeta buildCloudEventMetaFromNode(final Node node) {
        final CloudEventMeta cloudEventMeta = new CloudEventMeta();
        cloudEventMeta.setType((String) node.getMetaData().get(Metadata.TRIGGER_REF));
        if (Metadata.PRODUCE_MESSAGE.equals(node.getMetaData().get(Metadata.TRIGGER_TYPE))) {
            cloudEventMeta.setKind(EventKind.PRODUCED);
        } else if (Metadata.CONSUME_MESSAGE.equals(node.getMetaData().get(Metadata.TRIGGER_TYPE))) {
            cloudEventMeta.setKind(EventKind.CONSUMED);
        }
        return cloudEventMeta;
    }

    /*
     * apiVersion: eventing.knative.dev/v1
     * kind: Trigger
     * metadata:
     * name: shipping-international-trigger
     * spec:
     * broker: default
     * filter:
     * attributes:
     * type: internationalShipping
     * subscriber:
     * ref:
     * apiVersion: serving.knative.dev/v1
     * kind: Service
     * name: shipping-international
     */
}
