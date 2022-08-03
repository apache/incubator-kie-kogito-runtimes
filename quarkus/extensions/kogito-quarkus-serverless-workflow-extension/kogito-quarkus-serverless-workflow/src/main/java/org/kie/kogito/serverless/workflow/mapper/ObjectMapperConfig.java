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
package org.kie.kogito.serverless.workflow.mapper;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jbpm.process.instance.ProcessInstanceHolder;
import org.jbpm.process.instance.event.KogitoProcessVariableChangedEventImpl;
import org.kie.kogito.jackson.utils.JsonNodeFactoryListener;
import org.kie.kogito.jackson.utils.JsonNodeListener;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.uow.WorkUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.jackson.ObjectMapperCustomizer;

@ApplicationScoped
public class ObjectMapperConfig implements ObjectMapperCustomizer {

    @Inject
    ProcessConfig processConfig;

    @Override
    public void customize(ObjectMapper objectMapper) {
        objectMapper.setNodeFactory(new JsonNodeFactoryListener(Collections.singletonList(new JsonNodeListener() {

            @Override
            public void onValueChanged(JsonNode container, String property, JsonNode oldValue, JsonNode newValue) {
                processConfig.unitOfWorkManager().currentUnitOfWork().intercept(WorkUnit.create(new KogitoProcessVariableChangedEventImpl(property, property, oldValue, newValue,
                        Collections.emptyList(), ProcessInstanceHolder.get(), null, ProcessInstanceHolder.get().getKnowledgeRuntime()), e -> {
                        }));
                ;
            }
        })));
    }
}
