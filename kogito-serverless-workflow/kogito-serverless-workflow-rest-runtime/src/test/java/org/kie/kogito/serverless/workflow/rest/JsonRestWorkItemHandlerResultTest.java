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
package org.kie.kogito.serverless.workflow.rest;

import java.util.Collections;
import java.util.Map;

import org.jbpm.process.core.Process;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.workflow.core.impl.IOSpecification;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;
import org.kie.kogito.process.workitems.impl.KogitoWorkItemImpl;
import org.kogito.workitem.rest.RestWorkItemHandler;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JsonRestWorkItemHandlerResultTest {

    private static Vertx vertx;
    private static WebClient webClient;
    private static RestWorkItemHandler restHandler;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeAll
    static void init() {
        vertx = Vertx.vertx();
        webClient = WebClient.create(vertx);
        restHandler = new RestWorkItemHandler(webClient);
    }

    @AfterAll
    static void cleanup() {
        webClient.close();
        vertx.closeAndAwait();
    }

    private KogitoWorkItemImpl workItem;
    private Map<String, Object> parameters;
    @Mock
    private KogitoWorkItemManager manager;
    @Captor
    private ArgumentCaptor<Map<String, Object>> argCaptor;
    @Mock
    private VariableScope variableScope;

    @Mock
    private WorkItemNodeInstance nodeInstance;

    @Mock
    private IOSpecification ioSpecification;

    @Mock
    private WorkItemNode node;

    @BeforeEach
    void setup() {
        workItem = new KogitoWorkItemImpl();
        workItem.setId("2");
        parameters = workItem.getParameters();
        parameters.put(RestWorkItemHandler.HOST, "localhost");
        parameters.put(RestWorkItemHandler.PORT, wm.getPort());
        parameters.put(RestWorkItemHandler.METHOD, "get");
    //    parameters.put(RestWorkItemHandler.RESULT_HANDLER, new JsonRestWorkItemHandlerResult());

        Process process = mock(Process.class);
        ProcessInstance processInstance = mock(ProcessInstance.class);
        workItem.setProcessInstance(processInstance);
        when(processInstance.getProcess()).thenReturn(process);
        when(processInstance.getVariables()).thenReturn(Collections.emptyMap());
        when(process.getDefaultContext(VariableScope.VARIABLE_SCOPE)).thenReturn(variableScope);
        when(node.getIoSpecification()).thenReturn(ioSpecification);
        workItem.setNodeInstance(nodeInstance);
        when(nodeInstance.getNode()).thenReturn(node);
        when(ioSpecification.getOutputMappingBySources()).thenReturn(Collections.emptyMap());
    }

    @Test
    void testObject() {
        JsonNode expectedOutput = ObjectMapperFactory.get().createObjectNode().put("name", "Javierito");
        final String path = "/object";
        wm.stubFor(get(path).willReturn(aResponse().withStatus(200).withJsonBody(expectedOutput)));
        parameters.put(RestWorkItemHandler.URL, path);
        restHandler.executeWorkItem(workItem, manager);
        Object result = getResult();
        assertThat(result).isInstanceOf(ObjectNode.class);
        assertThat(result).isEqualTo(expectedOutput).isNotSameAs(expectedOutput);
    }
    
    @Test
    void testArray() {
        JsonNode expectedOutput = ObjectMapperFactory.get().createArrayNode().add("Javierito").add("fulanito");
        final String path = "/array";
        wm.stubFor(get(path).willReturn(aResponse().withStatus(200).withJsonBody(expectedOutput)));
        parameters.put(RestWorkItemHandler.URL, path);
        restHandler.executeWorkItem(workItem, manager);
        Object result = getResult();
        assertThat(result).isInstanceOf(ArrayNode.class);
        assertThat(result).isEqualTo(expectedOutput).isNotSameAs(expectedOutput);
    }
    
    private  Object getResult() {
    	verify(manager).completeWorkItem(anyString(), argCaptor.capture());
        Map<String, Object> results = argCaptor.getValue();
        assertThat(results).hasSize(1).containsKey(RestWorkItemHandler.RESULT);
        return results.get(RestWorkItemHandler.RESULT);
    }
}
