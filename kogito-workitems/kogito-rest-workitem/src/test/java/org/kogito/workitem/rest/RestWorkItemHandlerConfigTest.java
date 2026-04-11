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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestWorkItemHandlerConfigTest {

    private RestWorkItemHandlerConfig config;

    @BeforeEach
    void setUp() {
        config = new RestWorkItemHandlerConfig();
    }

    @Test
    void testRegisterAndGetProperty() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "mytoken123");

        Optional<String> value = config.getProperty("users", "FindUserRestCall", "access_token");

        assertThat(value).isPresent().contains("mytoken123");
    }

    @Test
    void testGetConfig() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "mytoken123");
        config.registerProperty("users", "FindUserRestCall", "accessTokenStrategy", "oauth2");

        Map<String, String> taskConfig = config.getConfig("users", "FindUserRestCall");

        assertThat(taskConfig)
                .hasSize(2)
                .containsEntry("access_token", "mytoken123")
                .containsEntry("accessTokenStrategy", "oauth2");
    }

    @Test
    void testGetConfigNotFound() {
        Map<String, String> taskConfig = config.getConfig("nonexistent", "task");

        assertThat(taskConfig).isEmpty();
    }

    @Test
    void testParseAndRegisterValidProperty() {
        boolean result = config.parseAndRegister(
                "kogito.processes.users.FindUserRestCall.access_token",
                "mytoken123");

        assertThat(result).isTrue();
        assertThat(config.getProperty("users", "FindUserRestCall", "access_token"))
                .isPresent()
                .contains("mytoken123");
    }

    @Test
    void testParseAndRegisterInvalidPrefix() {
        boolean result = config.parseAndRegister(
                "invalid.prefix.users.FindUserRestCall.access_token",
                "mytoken123");

        assertThat(result).isFalse();
    }

    @Test
    void testParseAndRegisterInvalidFormat() {
        boolean result = config.parseAndRegister(
                "kogito.processes.users",
                "value");

        assertThat(result).isFalse();
    }

    @Test
    void testParseAndRegisterMultipleProperties() {
        config.parseAndRegister("kogito.processes.users.FindUserRestCall.access_token", "token123");
        config.parseAndRegister("kogito.processes.users.FindUserRestCall.clientId", "client123");
        config.parseAndRegister("kogito.processes.users.FindUserRestCall.clientSecret", "secret123");
        config.parseAndRegister("kogito.processes.orders.CreateOrder.host", "api.example.com");

        Map<String, String> usersConfig = config.getConfig("users", "FindUserRestCall");
        assertThat(usersConfig)
                .hasSize(3)
                .containsEntry("access_token", "token123")
                .containsEntry("clientId", "client123")
                .containsEntry("clientSecret", "secret123");

        Map<String, String> ordersConfig = config.getConfig("orders", "CreateOrder");
        assertThat(ordersConfig)
                .hasSize(1)
                .containsEntry("host", "api.example.com");
    }

    @Test
    void testClear() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "mytoken123");
        config.registerProperty("orders", "CreateOrder", "host", "api.example.com");

        config.clear();

        assertThat(config.getConfig("users", "FindUserRestCall")).isEmpty();
        assertThat(config.getConfig("orders", "CreateOrder")).isEmpty();
    }

    @Test
    void testOverwriteProperty() {
        config.registerProperty("users", "FindUserRestCall", "access_token", "oldtoken");
        config.registerProperty("users", "FindUserRestCall", "access_token", "newtoken");

        Optional<String> value = config.getProperty("users", "FindUserRestCall", "access_token");

        assertThat(value).isPresent().contains("newtoken");
    }

    @Test
    void testGetConfigPrefix() {
        assertThat(RestWorkItemHandlerConfig.getConfigPrefix()).isEqualTo("kogito.processes.");
    }

    @Test
    void testGetAllProcessIds() {
        config.registerProperty("users", "FindUser", "url", "http://example.com");
        config.registerProperty("users", "CreateUser", "url", "http://example.com");
        config.registerProperty("orders", "CreateOrder", "url", "http://example.com");

        Set<String> processIds = config.getAllProcessIds();

        assertThat(processIds).hasSize(2);
        assertThat(processIds).contains("users", "orders");
    }

    @Test
    void testGetAllProcessTaskKeys() {
        config.registerProperty("users", "FindUser", "url", "http://example.com");
        config.registerProperty("users", "CreateUser", "url", "http://example.com");
        config.registerProperty("orders", "CreateOrder", "url", "http://example.com");

        Set<String> keys = config.getAllProcessTaskKeys();

        assertThat(keys).hasSize(3);
        assertThat(keys).contains("users.FindUser", "users.CreateUser", "orders.CreateOrder");
    }
}


