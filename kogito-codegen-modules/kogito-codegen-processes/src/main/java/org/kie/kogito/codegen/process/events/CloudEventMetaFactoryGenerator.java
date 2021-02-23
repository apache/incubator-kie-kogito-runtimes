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
package org.kie.kogito.codegen.process.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.jbpm.compiler.canonical.TriggerMetaData;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.process.ProcessExecutableModelGenerator;
import org.kie.kogito.event.CloudEventMeta;
import org.kie.kogito.event.EventKind;
import org.kie.kogito.services.event.DataEventAttrBuilder;

public class CloudEventMetaFactoryGenerator extends AbstractEventResourceGenerator {

    private static final String CLASS_NAME = "CloudEventMetaFactory";

    private final KogitoBuildContext context;
    private final Map<String, List<TriggerMetaData>> triggers;

    public CloudEventMetaFactoryGenerator(final KogitoBuildContext context,
                                          final List<ProcessExecutableModelGenerator> generators) {
        super(TemplatedGenerator.builder()
                .withTemplateBasePath(TEMPLATE_EVENT_FOLDER)
                .withFallbackContext(JavaKogitoBuildContext.CONTEXT_NAME)
                .build(context, CLASS_NAME));
        this.context = context;
        this.triggers = this.filterTriggers(generators);
    }

    Map<String, List<TriggerMetaData>> getTriggers() {
        return triggers;
    }

    public String generate() {
        final CompilationUnit clazz = generator.compilationUnitOrThrow("Cannot generate TopicInformation REST Resource");

        final ClassOrInterfaceDeclaration template = clazz
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface declaration!"));

        this.triggers.forEach((processId, triggers) -> triggers.forEach(trigger -> {
            EventKind eventKind = TriggerMetaData.TriggerType.ProduceMessage.equals(trigger.getType())
                    ? EventKind.PRODUCED
                    : EventKind.CONSUMED;

            String strEventKind = String.format("%s.%s", EventKind.class.getName(), eventKind.name());
            String strEventType = eventKind == EventKind.PRODUCED
                    ? DataEventAttrBuilder.toType(trigger.getName(), processId)
                    : trigger.getName();
            String strEventSource = eventKind == EventKind.PRODUCED
                    ? DataEventAttrBuilder.toSource(processId)
                    : "";

            BlockStmt builderBody = new BlockStmt();
            builderBody.addStatement(new ReturnStmt(String.format("new CloudEventMeta(\"%s\", \"%s\", %s)",
                    strEventType, strEventSource, strEventKind
            )));

            String builderMethodName = String.format("buildCloudEventMeta_%s_%s", eventKind.name(), trigger.getName());
            MethodDeclaration builderMethod = template.addMethod(builderMethodName, Modifier.Keyword.PUBLIC);
            builderMethod.setType(CloudEventMeta.class);
            builderMethod.setBody(builderBody);

            if (context.hasDI()) {
                context.getDependencyInjectionAnnotator().withFactoryMethod(builderMethod);
            }
        }));

        if (context.hasDI()) {
            context.getDependencyInjectionAnnotator().withFactoryClass(template);
        }

        return clazz.toString();
    }

    private Map<String, List<TriggerMetaData>> filterTriggers(final List<ProcessExecutableModelGenerator> generators) {
        if (generators != null) {
            final Map<String, List<TriggerMetaData>> filteredTriggers = new HashMap<>();
            generators
                    .stream()
                    .filter(m -> m.generate().getTriggers() != null && !m.generate().getTriggers().isEmpty())
                    .forEach(m -> filteredTriggers.put(m.getProcessId(),
                                                   m.generate().getTriggers().stream()
                                                 .filter(t -> !TriggerMetaData.TriggerType.Signal.equals(t.getType()))
                                                 .collect(Collectors.toList())));
            return filteredTriggers;
        }
        return Collections.emptyMap();
    }
}
