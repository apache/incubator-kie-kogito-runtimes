/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kogito.workitem.rest;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestWorkItemHandlerConfigInjectorTest {

    private RestWorkItemHandlerConfig config;
    private RestWorkItemHandlerConfigInjector injector;
    private KogitoWorkItem workItem;
    private KogitoProcessInstance processInstance;

    @BeforeEach
    void setUp() {
        config = new RestWorkItemHandlerConfig();
        injector = new RestWorkItemHandlerConfigInjector(config);

        workItem = mock(KogitoWorkItem.class);
        processInstance = mock(KogitoProcessInstance.class);

        when(workItem.getProcessInstance()).thenReturn(processInstance);
        when(processInstance.getProcessId()).thenReturn("users");
        when(workItem.getName()).thenReturn("FindUserRestCall");
    }

    @Test
    void testInjectConfigWithAccessToken() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "mytoken123");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        assertThat(parameters)
                .containsEntry("propagateToken", "mytoken123");
    }

    @Test
    void testInjectConfigWithMultipleProperties() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "token123");
        config.registerProperty("users", "FindUserRestCall", "host", "api.example.com");
        config.registerProperty("users", "FindUserRestCall", "port", "8080");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        assertThat(parameters)
                .containsEntry("propagateToken", "token123")
                .containsEntry(RestWorkItemHandler.HOST, "api.example.com")
                .containsEntry(RestWorkItemHandler.PORT, 8080);
    }

    @Test
    void testInjectConfigDoesNotOverrideExistingParameters() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "configtoken");
        config.registerProperty("users", "FindUserRestCall", "host", "config.example.com");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("propagateToken", "runtimetoken");

        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        
        assertThat(parameters)
                .containsEntry("propagateToken", "runtimetoken")
                .containsEntry(RestWorkItemHandler.HOST, "config.example.com");
    }

    @Test
    void testInjectConfigWithNoConfiguration() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("existingParam", "value");

        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        
        assertThat(parameters)
                .hasSize(1)
                .containsEntry("existingParam", "value");
    }

    @Test
    void testInjectConfigWithNullProcessId() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "token123");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, null, "FindUserRestCall");

        
        assertThat(parameters).isEmpty();
    }

    @Test
    void testInjectConfigWithNullTaskName() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "token123");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", null);

        
        assertThat(parameters).isEmpty();
    }

    @Test
    void testPropertyMappings() {
        config.registerProperty("users", "FindUserRestCall", "accessToken", "token1");
        config.registerProperty("users", "FindUserRestCall", "username", "user1");
        config.registerProperty("users", "FindUserRestCall", "password", "pass1");
        config.registerProperty("users", "FindUserRestCall", "client_id", "client1");
        config.registerProperty("users", "FindUserRestCall", "client_secret", "secret1");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        
        assertThat(parameters)
                .containsEntry("propagateToken", "token1")
                .containsEntry(RestWorkItemHandler.USER, "user1")
                .containsEntry(RestWorkItemHandler.PASSWORD, "pass1")
                .containsEntry("clientId", "client1")
                .containsEntry("clientSecret", "secret1");
    }

    @Test
    void testPropertyMappingsWithMultipleBearerTokenKeys() {
        
        config.registerProperty("users", "FindUserRestCall", "accessToken", "token1");
        config.registerProperty("users", "FindUserRestCall", "bearer_token", "token2");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        
        assertThat(parameters)
                .containsEntry("propagateToken", "token2");
    }

    @Test
    void testPortConversion() {
        config.registerProperty("users", "FindUserRestCall", "port", "8443");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        assertThat(parameters)
                .containsEntry(RestWorkItemHandler.PORT, 8443);
        assertThat(parameters.get(RestWorkItemHandler.PORT)).isInstanceOf(Integer.class);
    }

    @Test
    void testTimeoutConversion() {
        config.registerProperty("users", "FindUserRestCall", "requestTimeout", "5000");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        assertThat(parameters)
                .containsEntry(RestWorkItemHandler.REQUEST_TIMEOUT_IN_MILLIS, 5000L);
        assertThat(parameters.get(RestWorkItemHandler.REQUEST_TIMEOUT_IN_MILLIS)).isInstanceOf(Long.class);
    }

    @Test
    void testInvalidPortConversion() {
        config.registerProperty("users", "FindUserRestCall", "port", "invalid");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        
        assertThat(parameters)
                .containsEntry(RestWorkItemHandler.PORT, "invalid");
    }

    @Test
    void testGetPropertyMappings() {
        Map<String, String> mappings = RestWorkItemHandlerConfigInjector.getPropertyMappings();

        assertThat(mappings)
                .isNotEmpty()
                .containsEntry("access_token", "propagateToken")
                .containsEntry("username", RestWorkItemHandler.USER)
                .containsEntry("password", RestWorkItemHandler.PASSWORD)
                .containsEntry("host", RestWorkItemHandler.HOST)
                .containsEntry("port", RestWorkItemHandler.PORT);
    }

    @Test
    void testAddCustomPropertyMapping() {
        RestWorkItemHandlerConfigInjector.addPropertyMapping("customKey", "customParam");

        config.registerProperty("users", "FindUserRestCall", "customKey", "customValue");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        assertThat(parameters).containsEntry("customParam", "customValue");
    }

    @Test
    void testCommaSeparatedAccessTokenStrategy() {
        config.registerProperty("users", "FindUserRestCall", "accessTokenStrategy", "propagated,configured");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        
        assertThat(parameters).containsKey("AccessTokenAcquisitionStrategy");
        Object value = parameters.get("AccessTokenAcquisitionStrategy");
        assertThat(value).isInstanceOf(java.util.List.class);

        @SuppressWarnings("unchecked")
        java.util.List<String> strategies = (java.util.List<String>) value;
        assertThat(strategies)
                .hasSize(2)
                .containsExactly("propagated", "configured");
    }

    @Test
    void testCommaSeparatedWithSpaces() {
        config.registerProperty("users", "FindUserRestCall", "accessTokenStrategy", "propagated, configured, none");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        @SuppressWarnings("unchecked")
        java.util.List<String> strategies = (java.util.List<String>) parameters.get("AccessTokenAcquisitionStrategy");
        assertThat(strategies)
                .hasSize(3)
                .containsExactly("propagated", "configured", "none");
    }

    @Test
    void testSingleValueNotParsedAsListForStrategyProperty() {
        config.registerProperty("users", "FindUserRestCall", "accessTokenStrategy", "configured");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        
        assertThat(parameters.get("AccessTokenAcquisitionStrategy"))
                .isInstanceOf(String.class)
                .isEqualTo("configured");
    }

    @Test
    void testCommaSeparatedNotAppliedToNonStrategyProperties() {
        config.registerProperty("users", "FindUserRestCall", "host", "host1,host2");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        
        assertThat(parameters.get(RestWorkItemHandler.HOST))
                .isInstanceOf(String.class)
                .isEqualTo("host1,host2");
    }

    @Test
    void testEmptyValuesInCommaSeparatedList() {
        config.registerProperty("users", "FindUserRestCall", "accessTokenStrategy", "propagated,,configured,");

        Map<String, Object> parameters = new HashMap<>();
        injector.injectConfig(workItem, parameters, "users", "FindUserRestCall");

        @SuppressWarnings("unchecked")
        java.util.List<String> strategies = (java.util.List<String>) parameters.get("AccessTokenAcquisitionStrategy");
        
        assertThat(strategies)
                .hasSize(2)
                .containsExactly("propagated", "configured");
    }
}


