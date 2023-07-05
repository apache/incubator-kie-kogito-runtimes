/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationInstanceBuilder;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jbpm.ruleflow.core.Metadata;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;

import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.Transformation;

class TagResourceGenerator implements AnnotationsTransformer {

    private KogitoWorkflowProcess process;

    public TagResourceGenerator(KogitoWorkflowProcess process) {
        this.process = process;
    }

    @Override
    public boolean appliesTo(Kind kind) {
        return kind == Kind.CLASS;
    }

    private void addDescription(Transformation ctx, String description) {
        AnnotationInstanceBuilder builder = attributes(process.getId());
        if (description != null) {
            builder.add("description", description);
        }
        ctx.add(builder.build());
    }

    private void addTag(Transformation ctx, String tag) {
        ctx.add(attributes(tag).build());
    }

    private static AnnotationInstanceBuilder attributes(String tag) {
        return AnnotationInstance.builder(Tag.class).add("name", tag);
    }

    @Override
    public void transform(TransformationContext ctx) {
        if (ctx.getTarget().asClass().hasAnnotation(Path.class)) {
            Map<String, Object> metadata = process.getMetaData();
            @SuppressWarnings("unchecked")
            Collection<String> tags = (Collection<String>) metadata.getOrDefault(Metadata.TAGS, Set.of());
            String description = (String) metadata.get(Metadata.DESCRIPTION);
            Transformation trans = ctx.transform();
            tags.forEach(tag -> addTag(trans, tag));
            addDescription(trans, description);
            trans.done();
        }
    }
}
