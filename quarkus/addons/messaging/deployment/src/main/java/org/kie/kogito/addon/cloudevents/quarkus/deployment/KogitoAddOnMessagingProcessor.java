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
package org.kie.kogito.addon.cloudevents.quarkus.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.kie.kogito.codegen.api.GeneratedFile;
import org.kie.kogito.codegen.api.GeneratedFileType;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.quarkus.addons.common.deployment.AnyEngineKogitoAddOnProcessor;
import org.kie.kogito.quarkus.common.deployment.KogitoAddonsGeneratedSourcesBuildItem;
import org.kie.kogito.quarkus.common.deployment.KogitoBuildContextBuildItem;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;

public class KogitoAddOnMessagingProcessor extends AnyEngineKogitoAddOnProcessor {

    private static final String FEATURE = "kogito-addon-messaging-extension";

    @Inject
    CurateOutcomeBuildItem curateOutcomeBuildItem;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    KogitoAddonsGeneratedSourcesBuildItem generate(BuildProducer<KogitoMessagingMetadataBuildItem> metadataProducer, KogitoBuildContextBuildItem kogitoContext) {
        Collection<EventGenerator> generators =
                ChannelMappingStrategy.getChannelMapping().stream().map(channelInfo -> buildEventGenerator(kogitoContext.getKogitoBuildContext(), channelInfo)).collect(Collectors.toList());
        metadataProducer.produce(new KogitoMessagingMetadataBuildItem(generators));
        List<GeneratedFile> generatedFiles = new ArrayList<>();
        for (EventGenerator generator : generators) {
            generatedFiles.add(new GeneratedFile(GeneratedFileType.SOURCE, generator.getPath(), generator.getCode()));
            Optional<String> annotationName = generator.getAnnotationName();
            if (annotationName.isPresent()) {
                AnnotationGenerator annotationGen = new AnnotationGenerator(kogitoContext.getKogitoBuildContext(), annotationName.get());
                generatedFiles.add(new GeneratedFile(GeneratedFileType.SOURCE, annotationGen.getPath(), annotationGen.getCode()));
            }
        }
        return new KogitoAddonsGeneratedSourcesBuildItem(generatedFiles);
    }

    private EventGenerator buildEventGenerator(KogitoBuildContext context, ChannelInfo channelInfo) {
        return channelInfo.isInput() ? new EventGenerator(context, channelInfo, "EventReceiver") : new EventGenerator(context, channelInfo, "EventEmitter");
    }
}
