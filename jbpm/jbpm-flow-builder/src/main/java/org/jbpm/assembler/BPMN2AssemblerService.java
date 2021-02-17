/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.assembler;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.BPMN2ProcessFactory;
import org.kie.api.io.ResourceType;

public class BPMN2AssemblerService extends AbstractProcessAssembler {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.BPMN2;
    }

    @Override
    protected void configurePackageBuilder(KnowledgeBuilderImpl kb) {
        BPMN2ProcessFactory.configurePackageBuilder(kb);
    }
}
