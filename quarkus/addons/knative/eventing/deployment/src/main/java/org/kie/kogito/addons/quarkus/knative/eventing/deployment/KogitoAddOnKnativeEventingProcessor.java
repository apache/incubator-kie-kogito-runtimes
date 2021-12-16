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

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jbpm.ruleflow.core.Metadata;
import org.kie.api.definition.process.Node;
import org.kie.kogito.codegen.process.ProcessContainerGenerator;
import org.kie.kogito.event.CloudEventMeta;
import org.kie.kogito.event.EventKind;
import org.kie.kogito.quarkus.addons.common.deployment.AnyEngineKogitoAddOnProcessor;
import org.kie.kogito.quarkus.processes.deployment.KogitoProcessContainerGeneratorBuildItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.knative.eventing.v1.Broker;
import io.fabric8.knative.eventing.v1.BrokerBuilder;
import io.fabric8.knative.eventing.v1.Trigger;
import io.fabric8.knative.eventing.v1.TriggerBuilder;
import io.fabric8.knative.sources.v1.SinkBinding;
import io.fabric8.knative.sources.v1.SinkBindingBuilder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedFileSystemResourceBuildItem;
import io.quarkus.deployment.pkg.builditem.OutputTargetBuildItem;
import io.quarkus.kubernetes.deployment.SelectedKubernetesDeploymentTargetBuildItem;
import io.quarkus.kubernetes.spi.KubernetesResourceMetadataBuildItem;

import static io.quarkus.kubernetes.deployment.Constants.KUBERNETES;

public class KogitoAddOnKnativeEventingProcessor extends AnyEngineKogitoAddOnProcessor {

    private static final String FEATURE = "kogito-addon-knative-eventing-extension";

    private static final String FILE_NAME = "kogito.yml";

    private static final Logger LOGGER = LoggerFactory.getLogger(KogitoAddOnKnativeEventingProcessor.class);

    EventingConfiguration config;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void buildMetadata(KogitoProcessContainerGeneratorBuildItem processContainerBuildItem,
            SelectedKubernetesDeploymentTargetBuildItem selectedDeployment,
            List<KubernetesResourceMetadataBuildItem> kubernetesResourceMetadataBuildItems,
            BuildProducer<KogitoKnativeResourcesMetadataBuildItem> metadataProducer) {
        final Set<CloudEventMeta> cloudEvents = this.extractCloudEvents(processContainerBuildItem.getProcessContainerGenerators());
        if (cloudEvents != null && !cloudEvents.isEmpty()) {
            final Optional<KogitoServiceDeploymentTarget> target =
                    kubernetesResourceMetadataBuildItems.stream()
                            .filter(r -> selectedDeployment.getEntry().getKind().equals(r.getKind()) && selectedDeployment.getEntry().getName().equals(r.getTarget()))
                            .map(r -> new KogitoServiceDeploymentTarget(r.getGroup(), r.getVersion(), r.getKind(), r.getName()))
                            .findFirst();
            if (target.isEmpty()) {
                throw new IllegalStateException("Impossible to get the Kubernetes deployment target for this Kogito service. Target: " + selectedDeployment.getEntry().getName());
            }

            metadataProducer.produce(new KogitoKnativeResourcesMetadataBuildItem(cloudEvents, target.get()));
        }
    }

    @BuildStep
    void generate(OutputTargetBuildItem outputTarget,
            KogitoKnativeResourcesMetadataBuildItem resourcesMetadata,
            BuildProducer<GeneratedFileSystemResourceBuildItem> generatedResources) {
        final Optional<SinkBinding> sinkBinding = this.generateSinkBinding(resourcesMetadata);
        final List<Trigger> triggers = this.generateTriggers(resourcesMetadata);
        final Optional<Broker> broker = this.generateBroker(resourcesMetadata);

        final Path outputDir = outputTarget.getOutputDirectory().resolve(KUBERNETES);
        final byte[] resourcesBytes =
                new KogitoKnativeGenerator()
                        .addResources(triggers)
                        .addOptionalResource(sinkBinding)
                        .addOptionalResource(broker)
                        .getResourcesBytes();
        if (resourcesBytes == null || resourcesBytes.length == 0) {
            LOGGER.info("Couldn't generate Kogito Knative resources for service {}", resourcesMetadata.getDeployment().getName());
        } else {
            generatedResources.produce(new GeneratedFileSystemResourceBuildItem(Path.of(KUBERNETES, FILE_NAME).toString(), resourcesBytes));
            LOGGER.info("Generated Knative resources for Kogito Service {} in {}", resourcesMetadata.getDeployment().getName(), outputDir.resolve(FILE_NAME));
        }
    }

    private Optional<Broker> generateBroker(KogitoKnativeResourcesMetadataBuildItem resourcesMetadata) {
        if (config.autoGenerateBroker) {
            return Optional.of(new BrokerBuilder().withNewMetadata()
                    .withName(SinkConfiguration.DEFAULT_SINK_NAME)
                    .endMetadata().build());
        }
        return Optional.empty();
    }

    private Optional<SinkBinding> generateSinkBinding(KogitoKnativeResourcesMetadataBuildItem metadata) {
        if (metadata.getCloudEvents().stream().anyMatch(ce -> ce.getKind() == EventKind.PRODUCED)) {
            return Optional.of(new SinkBindingBuilder()
                    .withNewMetadata().withName(KnativeResourcesUtil.generateSinkBindingName(metadata.getDeployment().getName())).endMetadata()
                    .withNewSpec()
                    .withNewSubject()
                    .withName(metadata.getDeployment().getName())
                    .withKind(metadata.getDeployment().getKind())
                    .withApiVersion(metadata.getDeployment().getApiVersion())
                    .endSubject()
                    .withNewSink().withNewRef()
                    .withName(config.sink.name) // from properties
                    .withApiVersion(config.sink.apiVersion)
                    .withKind(config.sink.kind)
                    .withNamespace(config.sink.namespace.orElse(""))
                    .endRef().endSink().endSpec()
                    .build());
        }
        return Optional.empty();
    }

    private List<Trigger> generateTriggers(KogitoKnativeResourcesMetadataBuildItem metadata) {
        return metadata.getCloudEvents().stream()
                .filter(ce -> ce.getKind() == EventKind.CONSUMED)
                .map(ce -> new TriggerBuilder()
                        .withNewMetadata()
                        .withName(KnativeResourcesUtil.generateTriggerName(ce.getType(), metadata.getDeployment().getName()))
                        .endMetadata()
                        .withNewSpec()
                        .withNewFilter()
                        .addToAttributes(Collections.singletonMap("type", ce.getType()))
                        .endFilter()
                        .withBroker(config.broker)
                        .withNewSubscriber()
                        .withNewRef()
                        .withKind(metadata.getDeployment().getKind())
                        .withName(metadata.getDeployment().getName())
                        .withApiVersion(metadata.getDeployment().getApiVersion())
                        .endRef().endSubscriber().endSpec()
                        .build())
                .collect(Collectors.toList());
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
}
