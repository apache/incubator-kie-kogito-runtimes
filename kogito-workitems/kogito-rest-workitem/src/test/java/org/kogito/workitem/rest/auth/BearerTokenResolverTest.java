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
package org.kogito.workitem.rest.auth;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.WorkItemExecutionException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BearerTokenResolverTest {

    @Mock(lenient = true)
    private KogitoWorkItem workItem;

    @Mock(lenient = true)
    private KogitoNodeInstance nodeInstance;

    @Mock(lenient = true)
    private AuthTokenProvider authTokenProvider;

    private BearerTokenResolver resolver;

    private static final String PROCESS_ID = "testProcess";
    private static final String TASK_NAME = "TestTask";
    private static final String TASK_ID = "task_123_456";

    @BeforeEach
    void setUp() {
        resolver = new BearerTokenResolver(PROCESS_ID, TASK_NAME);

        when(workItem.getNodeInstance()).thenReturn(nodeInstance);
        when(nodeInstance.getNodeDefinitionId()).thenReturn(TASK_ID);

        AuthTokenProviderHolder.setInstance(authTokenProvider);
    }

    @AfterEach
    void tearDown() {
        AuthTokenProviderHolder.setInstance(null);
    }

    @Test
    void testApplyWithTokenFromProvider() {
        // Given
        String expectedToken = "test-token-123";
        when(authTokenProvider.getToken(eq(PROCESS_ID), eq(TASK_NAME), eq(TASK_ID)))
                .thenReturn(Optional.of(expectedToken));

        // When
        String result = resolver.apply(workItem);

        // Then
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    void testApplyRemovesBearerPrefix() {
        // Given
        String tokenWithBearer = "Bearer test-token-123";
        String expectedToken = "test-token-123";
        when(authTokenProvider.getToken(eq(PROCESS_ID), eq(TASK_NAME), eq(TASK_ID)))
                .thenReturn(Optional.of(tokenWithBearer));

        // When
        String result = resolver.apply(workItem);

        // Then
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    void testApplyWithTokenWithoutBearerPrefix() {
        // Given
        String expectedToken = "test-token-123";
        when(authTokenProvider.getToken(eq(PROCESS_ID), eq(TASK_NAME), eq(TASK_ID)))
                .thenReturn(Optional.of(expectedToken));

        // When
        String result = resolver.apply(workItem);

        // Then
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    void testApplyThrowsExceptionWhenNoProviderAvailable() {
        // Given
        AuthTokenProviderHolder.setInstance(null);

        // When/Then
        assertThatThrownBy(() -> resolver.apply(workItem))
                .isInstanceOf(WorkItemExecutionException.class)
                .hasMessageContaining("No AuthTokenProvider available")
                .hasMessageContaining(PROCESS_ID)
                .hasMessageContaining(TASK_NAME);
    }

    @Test
    void testApplyThrowsExceptionWhenTokenNotFound() {
        // Given
        when(authTokenProvider.getToken(eq(PROCESS_ID), eq(TASK_NAME), eq(TASK_ID)))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> resolver.apply(workItem))
                .isInstanceOf(WorkItemExecutionException.class)
                .hasMessageContaining("Authentication token not found")
                .hasMessageContaining(PROCESS_ID)
                .hasMessageContaining(TASK_NAME)
                .hasMessageContaining(TASK_ID);
    }

    @Test
    void testApplyThrowsExceptionWhenTokenIsEmpty() {
        // Given
        when(authTokenProvider.getToken(eq(PROCESS_ID), eq(TASK_NAME), eq(TASK_ID)))
                .thenReturn(Optional.of(""));

        // When/Then
        assertThatThrownBy(() -> resolver.apply(workItem))
                .isInstanceOf(WorkItemExecutionException.class)
                .hasMessageContaining("Authentication token not found");
    }

    @Test
    void testApplyWithNullTaskId() {
        // Given
        when(workItem.getNodeInstance()).thenReturn(null);
        String expectedToken = "test-token-123";
        when(authTokenProvider.getToken(eq(PROCESS_ID), eq(TASK_NAME), eq(null)))
                .thenReturn(Optional.of(expectedToken));

        // When
        String result = resolver.apply(workItem);

        // Then
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    void testGetProcessId() {
        // When
        String result = resolver.getProcessId();

        // Then
        assertThat(result).isEqualTo(PROCESS_ID);
    }

    @Test
    void testGetTaskName() {
        // When
        String result = resolver.getTaskName();

        // Then
        assertThat(result).isEqualTo(TASK_NAME);
    }

    @Test
    void testGetConfigProperty() {
        // Given
        String expectedToken = "config-token-123";
        when(authTokenProvider.getToken(eq(PROCESS_ID), eq(TASK_NAME)))
                .thenReturn(Optional.of(expectedToken));

        // When
        Optional<String> result = resolver.getConfigProperty("access_token", String.class);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedToken);
    }

    @Test
    void testGetConfigPropertyReturnsEmptyWhenNoProvider() {
        // Given
        AuthTokenProviderHolder.setInstance(null);

        // When
        Optional<String> result = resolver.getConfigProperty("access_token", String.class);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testGetPropertyNames() {
        // When
        Iterable<String> result = resolver.getPropertyNames();

        // Then
        assertThat(result).containsExactly(
                String.format("kogito.processes.%s.%s.access_token", PROCESS_ID, TASK_NAME));
    }

    @Test
    void testGetIndexedConfigProperty() {
        // When
        var result = resolver.getIndexedConfigProperty("access_token", String.class);

        // Then
        assertThat(result).isEmpty();
    }
}
