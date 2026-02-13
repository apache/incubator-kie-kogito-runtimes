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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTokenProviderTest {

    private AuthTokenProvider provider;

    private static final String PROCESS_ID = "testProcess";
    private static final String TASK_NAME = "TestTask";
    private static final String TASK_ID = "task_123_456";
    private static final String TEST_TOKEN = "test-token-123";

    @BeforeEach
    void setUp() {
        provider = new TestAuthTokenProvider();
    }

    @Test
    void testGetTokenWithAllParameters() {
        // When
        Optional<String> result = provider.getToken(PROCESS_ID, TASK_NAME, TASK_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TEST_TOKEN);
    }

    @Test
    void testGetTokenWithNullTaskId() {
        // When
        Optional<String> result = provider.getToken(PROCESS_ID, TASK_NAME, null);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TEST_TOKEN);
    }

    @Test
    void testGetTokenDeprecatedMethod() {
        // When
        Optional<String> result = provider.getToken(PROCESS_ID, TASK_NAME);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(TEST_TOKEN);
    }

    @Test
    void testGetTokenDeprecatedMethodCallsNewMethod() {
        // Given
        AuthTokenProvider customProvider = new AuthTokenProvider() {
            @Override
            public Optional<String> getToken(String processId, String taskName, String taskId) {
                assertThat(taskId).isNull();
                return Optional.of("custom-token");
            }
        };

        // When
        Optional<String> result = customProvider.getToken(PROCESS_ID, TASK_NAME);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("custom-token");
    }

    @Test
    void testIsAvailableDefaultImplementation() {
        // When
        boolean result = provider.isAvailable();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testIsAvailableCustomImplementation() {
        // Given
        AuthTokenProvider unavailableProvider = new AuthTokenProvider() {
            @Override
            public Optional<String> getToken(String processId, String taskName, String taskId) {
                return Optional.empty();
            }

            @Override
            public boolean isAvailable() {
                return false;
            }
        };

        // When
        boolean result = unavailableProvider.isAvailable();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testGetTokenReturnsEmpty() {
        // Given
        AuthTokenProvider emptyProvider = new AuthTokenProvider() {
            @Override
            public Optional<String> getToken(String processId, String taskName, String taskId) {
                return Optional.empty();
            }
        };

        // When
        Optional<String> result = emptyProvider.getToken(PROCESS_ID, TASK_NAME, TASK_ID);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testGetTokenWithEmptyString() {
        // Given
        AuthTokenProvider emptyStringProvider = new AuthTokenProvider() {
            @Override
            public Optional<String> getToken(String processId, String taskName, String taskId) {
                return Optional.of("");
            }
        };

        // When
        Optional<String> result = emptyStringProvider.getToken(PROCESS_ID, TASK_NAME, TASK_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void testGetTokenWithNullProcessId() {
        // When
        Optional<String> result = provider.getToken(null, TASK_NAME, TASK_ID);

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void testGetTokenWithNullTaskName() {
        // When
        Optional<String> result = provider.getToken(PROCESS_ID, null, TASK_ID);

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void testGetTokenWithAllNullParameters() {
        // When
        Optional<String> result = provider.getToken(null, null, null);

        // Then
        assertThat(result).isPresent();
    }

    @Test
    void testGetTokenWithBearerPrefix() {
        // Given
        AuthTokenProvider bearerProvider = new AuthTokenProvider() {
            @Override
            public Optional<String> getToken(String processId, String taskName, String taskId) {
                return Optional.of("Bearer " + TEST_TOKEN);
            }
        };

        // When
        Optional<String> result = bearerProvider.getToken(PROCESS_ID, TASK_NAME, TASK_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).startsWith("Bearer ");
        assertThat(result.get()).contains(TEST_TOKEN);
    }

    @Test
    void testMultipleCallsReturnConsistentResults() {
        // When
        Optional<String> result1 = provider.getToken(PROCESS_ID, TASK_NAME, TASK_ID);
        Optional<String> result2 = provider.getToken(PROCESS_ID, TASK_NAME, TASK_ID);

        // Then
        assertThat(result1).isEqualTo(result2);
    }


    private static class TestAuthTokenProvider implements AuthTokenProvider {

        @Override
        public Optional<String> getToken(String processId, String taskName, String taskId) {
            return Optional.of(TEST_TOKEN);
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}

