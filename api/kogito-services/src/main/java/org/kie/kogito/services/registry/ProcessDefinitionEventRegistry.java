/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.services.registry;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kie.kogito.Application;
import org.kie.kogito.event.EventBatch;
import org.kie.kogito.event.impl.ProcessEventBatch;
import org.kie.kogito.event.process.NodeDefinitionEventBody;
import org.kie.kogito.event.process.ProcessDefinitionDataEvent;
import org.kie.kogito.event.process.ProcessDefinitionEventBody;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.Processes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDefinitionEventRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDefinitionEventRegistry.class);

    private Application app;
    private String serviceUrl;

    public ProcessDefinitionEventRegistry(Application app, String serviceUrl) {
        this.app = app;
        this.serviceUrl = serviceUrl;
    }

    public void register(Processes processes) {
        EventBatch eventBatch = new ProcessEventBatch();
        processes.processIds().stream()
                .map(processes::processById)
                .map(mapProcessDefinition(app.config().addons().availableAddons(), serviceUrl))
                .forEach(process -> {
                    LOGGER.debug("Registering process definition with id: {}", process.getId());
                    eventBatch.append(process);
                });
        LOGGER.debug("Publishing all processes definitions");
        app.unitOfWorkManager().eventManager().publish(eventBatch);
    }

    private Function<Process<?>, ProcessDefinitionDataEvent> mapProcessDefinition(Set<String> addons, String endpoint) {
        return p -> new ProcessDefinitionDataEvent(ProcessDefinitionEventBody.builder()
                .setId(p.id())
                .setName(p.name())
                .setVersion(p.version())
                .setType(ProcessDefinitionDataEvent.PROCESS_DEFINITION_EVENT)
                .setAddons(addons)
                .setEndpoint(getEndpoint(endpoint, p))
                .setSource(getProcessSource())
                .setNodes(getNodesDefinitions(p))
                .build());
    }

    private static String getEndpoint(String endpoint, Process<?> p) {
        return endpoint + "/" + (p.id().contains(".") ? p.id().substring(p.id().lastIndexOf('.') + 1) : p.id());
    }
    private List<NodeDefinitionEventBody> getNodesDefinitions(Process<?> p) {
        return p.findNodes(n -> true).stream()
                .map(node -> NodeDefinitionEventBody.builder()
                        .setId(String.valueOf(node.getId()))
                        .setNodeName(node.getName())
                        .setNodeType(node.getNodeType().name())
                        .setUniqueId(node.getNodeUniqueId())
                        .setMetadata(node.getMetaData())
                        .build())
                .collect(Collectors.toList());
    }

    private String getProcessSource() {
        return null;
    }
}
