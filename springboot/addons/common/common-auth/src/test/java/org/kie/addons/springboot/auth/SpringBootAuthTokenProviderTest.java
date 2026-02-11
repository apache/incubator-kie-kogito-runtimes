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
package org.kie.addons.springboot.auth;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringBootAuthTokenProviderTest {

    @Mock
    private SpringBootAuthTokenHelper authTokenHelper;

    @Mock
    private Environment environment;

    private SpringBootAuthTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SpringBootAuthTokenProvider(authTokenHelper, environment);
    }

    @Test
    void testGetTokenFromSecurityContext() {
        // Given
        String processId = "testProcess";
        String taskName = "TestTask";
        String taskId = "task123";
        String expectedToken = "security-context-token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.of(expectedToken));

        // When
        Optional<String> result = provider.getToken(processId, taskName, taskId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
    }

    @Test
    void testGetTokenFromSecurityContextWithBearerPrefix() {
        // Given
        String processId = "testProcess";
        String taskName = "TestTask";
        String taskId = "task123";
        String tokenWithBearer = "Bearer security-context-token";
        String expectedToken = "security-context-token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.of(tokenWithBearer));

        // When
        Optional<String> result = provider.getToken(processId, taskName, taskId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
    }

    @Test
    void testGetTokenWithTaskNameFromConfig() {
        // Given
        String processId = "testProcess";
        String taskName = "TestTask";
        String taskId = "task123";
        String expectedToken = "config-token-123";
        String configKey = "kogito.processes.testProcess.TestTask.access_token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.empty());
        when(environment.getProperty(eq(configKey))).thenReturn(expectedToken);

        // When
        Optional<String> result = provider.getToken(processId, taskName, taskId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
    }

    @Test
    void testGetTokenWithTaskIdFromConfigWhenTaskNameNotAvailable() {
        // Given
        String processId = "testProcess";
        String taskName = null;
        String taskId = "task123";
        String expectedToken = "config-token-456";
        String configKey = "kogito.processes.testProcess.task123.access_token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.empty());
        when(environment.getProperty(eq(configKey))).thenReturn(expectedToken);

        // When
        Optional<String> result = provider.getToken(processId, taskName, taskId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
    }

    @Test
    void testGetTokenFallbackToTaskIdWhenTaskNameConfigNotFound() {
        // Given
        String processId = "testProcess";
        String taskName = "TestTask";
        String taskId = "task123";
        String expectedToken = "config-token-789";
        String taskNameConfigKey = "kogito.processes.testProcess.TestTask.access_token";
        String taskIdConfigKey = "kogito.processes.testProcess.task123.access_token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.empty());
        when(environment.getProperty(eq(taskNameConfigKey))).thenReturn(null);
        when(environment.getProperty(eq(taskIdConfigKey))).thenReturn(expectedToken);

        // When
        Optional<String> result = provider.getToken(processId, taskName, taskId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
    }

    @Test
    void testGetTokenReturnsEmptyWhenNoTokenAvailable() {
        // Given
        String processId = "testProcess";
        String taskName = "TestTask";
        String taskId = "task123";
        String taskNameConfigKey = "kogito.processes.testProcess.TestTask.access_token";
        String taskIdConfigKey = "kogito.processes.testProcess.task123.access_token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.empty());
        when(environment.getProperty(eq(taskNameConfigKey))).thenReturn(null);
        when(environment.getProperty(eq(taskIdConfigKey))).thenReturn(null);

        // When
        Optional<String> result = provider.getToken(processId, taskName, taskId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testGetTokenIgnoresEmptyConfigValue() {
        // Given
        String processId = "testProcess";
        String taskName = "TestTask";
        String taskId = "task123";
        String taskNameConfigKey = "kogito.processes.testProcess.TestTask.access_token";
        String taskIdConfigKey = "kogito.processes.testProcess.task123.access_token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.empty());
        when(environment.getProperty(eq(taskNameConfigKey))).thenReturn("  ");
        when(environment.getProperty(eq(taskIdConfigKey))).thenReturn("");

        // When
        Optional<String> result = provider.getToken(processId, taskName, taskId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testGetTokenWithEmptyTaskName() {
        // Given
        String processId = "testProcess";
        String taskName = "";
        String taskId = "task123";
        String expectedToken = "config-token-456";
        String configKey = "kogito.processes.testProcess.task123.access_token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.empty());
        when(environment.getProperty(eq(configKey))).thenReturn(expectedToken);

        // When
        Optional<String> result = provider.getToken(processId, taskName, taskId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
    }

    @Test
    void testDeprecatedGetTokenMethod() {
        // Given
        String processId = "testProcess";
        String taskName = "TestTask";
        String expectedToken = "config-token-123";
        String configKey = "kogito.processes.testProcess.TestTask.access_token";

        when(authTokenHelper.getAuthToken()).thenReturn(Optional.empty());
        when(environment.getProperty(eq(configKey))).thenReturn(expectedToken);

        // When
        Optional<String> result = provider.getToken(processId, taskName);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
    }

    @Test
    void testIsAvailable() {
        // When
        boolean result = provider.isAvailable();

        // Then
        assertThat(result).isTrue();
    }
}
