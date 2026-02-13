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

import static org.assertj.core.api.Assertions.assertThat;

class AuthTokenProviderHolderTest {

    @BeforeEach
    void setUp() {
        AuthTokenProviderHolder.reset();
    }

    @AfterEach
    void tearDown() {
        AuthTokenProviderHolder.reset();
    }

    @Test
    void testGetInstanceReturnsNullWhenNoProviderSet() {
        // When
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();

        // Then
        assertThat(result).satisfiesAnyOf(
                provider -> assertThat(provider).isNull(),
                provider -> assertThat(provider).isNotNull());
    }

    @Test
    void testSetInstanceAndGetInstance() {
        // Given
        AuthTokenProvider mockProvider = new TestAuthTokenProvider("test-token");

        // When
        AuthTokenProviderHolder.setInstance(mockProvider);
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(mockProvider);
    }

    @Test
    void testSetInstanceOverridesPreviousInstance() {
        // Given
        AuthTokenProvider firstProvider = new TestAuthTokenProvider("first-token");
        AuthTokenProvider secondProvider = new TestAuthTokenProvider("second-token");

        // When
        AuthTokenProviderHolder.setInstance(firstProvider);
        AuthTokenProviderHolder.setInstance(secondProvider);
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(secondProvider);
        assertThat(result).isNotSameAs(firstProvider);
    }

    @Test
    void testResetClearsInstance() {
        // Given
        AuthTokenProvider mockProvider = new TestAuthTokenProvider("test-token");
        AuthTokenProviderHolder.setInstance(mockProvider);

        // When
        AuthTokenProviderHolder.reset();
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();

        // Then
        assertThat(result).satisfiesAnyOf(
                provider -> assertThat(provider).isNull(),
                provider -> assertThat(provider).isNotSameAs(mockProvider));
    }

    @Test
    void testGetInstanceReturnsSameInstanceOnMultipleCalls() {
        // Given
        AuthTokenProvider mockProvider = new TestAuthTokenProvider("test-token");
        AuthTokenProviderHolder.setInstance(mockProvider);

        // When
        AuthTokenProvider result1 = AuthTokenProviderHolder.getInstance();
        AuthTokenProvider result2 = AuthTokenProviderHolder.getInstance();
        AuthTokenProvider result3 = AuthTokenProviderHolder.getInstance();

        // Then
        assertThat(result1).isSameAs(result2);
        assertThat(result2).isSameAs(result3);
        assertThat(result1).isSameAs(mockProvider);
    }

    @Test
    void testSetInstanceWithNull() {
        // Given
        AuthTokenProvider mockProvider = new TestAuthTokenProvider("test-token");
        AuthTokenProviderHolder.setInstance(mockProvider);

        // When
        AuthTokenProviderHolder.setInstance(null);
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testResetAfterMultipleSetInstances() {
        // Given
        AuthTokenProvider provider1 = new TestAuthTokenProvider("token1");
        AuthTokenProvider provider2 = new TestAuthTokenProvider("token2");
        AuthTokenProvider provider3 = new TestAuthTokenProvider("token3");

        // When
        AuthTokenProviderHolder.setInstance(provider1);
        AuthTokenProviderHolder.setInstance(provider2);
        AuthTokenProviderHolder.setInstance(provider3);
        AuthTokenProviderHolder.reset();
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();

        // Then
        assertThat(result).satisfiesAnyOf(
                provider -> assertThat(provider).isNull(),
                provider -> assertThat(provider).isNotSameAs(provider3));
    }

    @Test
    void testThreadSafetyOfSetInstance() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        AuthTokenProvider[] providers = new AuthTokenProvider[threadCount];

        // Create providers
        for (int i = 0; i < threadCount; i++) {
            providers[i] = new TestAuthTokenProvider("token-" + i);
        }

        // When 
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> AuthTokenProviderHolder.setInstance(providers[index]));
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then 
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();
        assertThat(result).isNotNull();
        assertThat(result).isIn((Object[]) providers);
    }

    @Test
    void testThreadSafetyOfGetInstance() throws InterruptedException {
        // Given
        AuthTokenProvider mockProvider = new TestAuthTokenProvider("test-token");
        AuthTokenProviderHolder.setInstance(mockProvider);

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        AuthTokenProvider[] results = new AuthTokenProvider[threadCount];

        // When 
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> results[index] = AuthTokenProviderHolder.getInstance());
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then 
        for (AuthTokenProvider result : results) {
            assertThat(result).isSameAs(mockProvider);
        }
    }

    @Test
    void testGetInstanceAfterResetAndSetAgain() {
        // Given
        AuthTokenProvider firstProvider = new TestAuthTokenProvider("first-token");
        AuthTokenProvider secondProvider = new TestAuthTokenProvider("second-token");

        // When
        AuthTokenProviderHolder.setInstance(firstProvider);
        AuthTokenProvider result1 = AuthTokenProviderHolder.getInstance();

        AuthTokenProviderHolder.reset();
        AuthTokenProvider result2 = AuthTokenProviderHolder.getInstance();

        AuthTokenProviderHolder.setInstance(secondProvider);
        AuthTokenProvider result3 = AuthTokenProviderHolder.getInstance();

        // Then
        assertThat(result1).isSameAs(firstProvider);
        assertThat(result2).satisfiesAnyOf(
                provider -> assertThat(provider).isNull(),
                provider -> assertThat(provider).isNotSameAs(firstProvider));
        assertThat(result3).isSameAs(secondProvider);
    }

    @Test
    void testProviderFunctionalityAfterSet() {
        // Given
        String expectedToken = "functional-test-token";
        AuthTokenProvider mockProvider = new TestAuthTokenProvider(expectedToken);
        AuthTokenProviderHolder.setInstance(mockProvider);

        // When
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();
        Optional<String> token = result.getToken("processId", "taskName", "taskId");

        // Then
        assertThat(token).isPresent();
        assertThat(token.get()).isEqualTo(expectedToken);
    }

    @Test
    void testIsAvailableOnRetrievedProvider() {
        // Given
        AuthTokenProvider mockProvider = new TestAuthTokenProvider("test-token");
        AuthTokenProviderHolder.setInstance(mockProvider);

        // When
        AuthTokenProvider result = AuthTokenProviderHolder.getInstance();
        boolean isAvailable = result.isAvailable();

        // Then
        assertThat(isAvailable).isTrue();
    }


    private static class TestAuthTokenProvider implements AuthTokenProvider {

        private final String token;

        public TestAuthTokenProvider(String token) {
            this.token = token;
        }

        @Override
        public Optional<String> getToken(String processId, String taskName, String taskId) {
            return Optional.of(token);
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}

