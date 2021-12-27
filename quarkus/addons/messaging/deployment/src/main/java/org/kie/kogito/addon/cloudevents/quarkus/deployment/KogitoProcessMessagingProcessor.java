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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jbpm.compiler.canonical.ProcessMetaData;
import org.kie.kogito.codegen.process.ProcessGenerator;
import org.kie.kogito.event.EventEmitter;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.quarkus.addons.common.deployment.KogitoCapability;
import org.kie.kogito.quarkus.addons.common.deployment.RequireCapabilityKogitoAddOnProcessor;
import org.kie.kogito.quarkus.extensions.spi.deployment.KogitoProcessContainerGeneratorBuildItem;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildStep;

public class KogitoProcessMessagingProcessor extends RequireCapabilityKogitoAddOnProcessor {

    public KogitoProcessMessagingProcessor() {
        super(KogitoCapability.PROCESSES);
    }

    private static class MessagingAnnotationTransfomer implements AnnotationsTransformer {
        private Map<DotName, DotName> consumerMapping;
        private Map<DotName, DotName> producerMapping;
        private Map<DotName, DotName> classMapping;

        public MessagingAnnotationTransfomer(Map<DotName, DotName> consumerMapping,
                Map<DotName, DotName> producerMapping, Map<DotName, DotName> classMapping) {
            this.consumerMapping = consumerMapping;
            this.producerMapping = producerMapping;
            this.classMapping = classMapping;
        }

        @Override
        public boolean appliesTo(Kind kind) {
            return kind == Kind.FIELD || kind == Kind.CLASS;
        }

        @Override
        public void transform(TransformationContext ctx) {
            if (ctx.isField()) {
                boolean found = false;
                for (AnnotationInstance annotation : ctx.getAnnotations()) {
                    if (annotation.name().equals(DotNames.INJECT)) {
                        found = true;
                        break;
                    }
                }
                FieldInfo field = ctx.getTarget().asField();
                Type type = field.type();
                if (found && type.kind() == Type.Kind.CLASS) {
                    String className = type.name().toString();
                    if (className.equals(EventEmitter.class.getName())) {
                        addAnnotation(producerMapping, field, ctx);
                    } else if (className.equals(EventReceiver.class.getName())) {
                        addAnnotation(consumerMapping, field, ctx);
                    }
                }
            } else if (ctx.isClass()) {
                addAnnotation(classMapping, ctx.getTarget().asClass().name(), ctx);
            }
        }

        private static void addAnnotation(Map<DotName, DotName> mapping, FieldInfo field, TransformationContext ctx) {
            addAnnotation(mapping, field.declaringClass().name(), ctx);
        }

        private static void addAnnotation(Map<DotName, DotName> mapping, DotName name, TransformationContext ctx) {
            DotName annotation = mapping.get(name);
            if (annotation != null) {
                ctx.transform().add(annotation).done();
            }
        }
    }

    @BuildStep
    AnnotationsTransformerBuildItem annotate(Optional<KogitoProcessContainerGeneratorBuildItem> processBuildItem, KogitoMessagingMetadataBuildItem messagingMetadata) {
        Map<DotName, DotName> consumerMapping = new HashMap<>();
        Map<DotName, DotName> producerMapping = new HashMap<>();
        if (processBuildItem.isPresent()) {
            processBuildItem.get().getProcessContainerGenerators()
                    .forEach(containerGenerator -> containerGenerator.getProcesses().forEach(process -> collect(process, consumerMapping, producerMapping, messagingMetadata.generators())));
        }
        return new AnnotationsTransformerBuildItem(new MessagingAnnotationTransfomer(consumerMapping, producerMapping,
                messagingMetadata.generators().stream().filter(generator -> generator.getFullAnnotationName().isPresent())
                        .collect(Collectors.toMap(DotNamesHelper::createClassName, DotNamesHelper::createAnnotationName))));
    }

    private void collect(ProcessGenerator process, Map<DotName, DotName> consumerMapping, Map<DotName, DotName> producerMapping, Collection<EventGenerator> generators) {
        ProcessMetaData processMetadata = process.getProcessExecutable().generate();
        collect(processMetadata.getConsumers(), consumerMapping, generators);
        collect(processMetadata.getProducers(), producerMapping, generators);

    }

    private void collect(Map<String, CompilationUnit> trigger2CU,
            Map<DotName, DotName> className2Annotation,
            Collection<EventGenerator> generators) {
        for (EventGenerator generator : generators) {
            if (generator.getFullAnnotationName().isPresent()) {
                CompilationUnit cu = trigger2CU.get(generator.getChannelInfo().getChannelName());
                if (cu != null) {
                    className2Annotation.put(getName(cu), DotNamesHelper.createAnnotationName(generator));
                }
            }
        }
    }

    private DotName getName(CompilationUnit cu) {
        return DotNamesHelper.createDotName(cu.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse("") + "."
                + cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow(() -> new IllegalStateException("cannnot find class")).getNameAsString());
    }

}
