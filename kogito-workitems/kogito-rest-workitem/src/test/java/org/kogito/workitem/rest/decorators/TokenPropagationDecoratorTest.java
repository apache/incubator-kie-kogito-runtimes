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
package org.kogito.workitem.rest.decorators;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.Application;
import org.kie.kogito.Config;
import org.kie.kogito.auth.AuthTokenProvider;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.process.ProcessConfig;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vertx.mutiny.ext.web.client.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenPropagationDecoratorTest {

    private static final String CONFIGURED_TOKEN = "configured-token-12345";
    private static final String PROPAGATED_TOKEN = "propagated-token-67890";
    private static final String ACCESS_TOKEN_ACQUISITION_STRATEGY = "AccessTokenAcquisitionStrategy";
    private static final String PROPAGATE_TOKEN_PARAM = "propagateToken";

    @Mock
    private KogitoWorkItem workItem;

    @Mock
    private KogitoNodeInstance nodeInstance;

    @Mock
    private HttpRequest<?> httpRequest;

    @Mock
    private KogitoWorkItemHandler handler;

    @Mock
    private Application application;

    @Mock
    private Config config;

    @Mock
    private ProcessConfig processConfig;

    @Mock
    private AuthTokenProvider authTokenProvider;

    private TokenPropagationDecorator decorator;
    private Map<String, Object> parameters;

    @BeforeEach
    void setUp() {
        decorator = new TokenPropagationDecorator();
        parameters = new HashMap<>();

        // Setup mock chain with lenient() to avoid unnecessary stubbing warnings
        lenient().when(workItem.getNodeInstance()).thenReturn(nodeInstance);
        lenient().when(nodeInstance.getId()).thenReturn("test-node-123");
        lenient().when(handler.getApplication()).thenReturn(application);
        lenient().when(application.config()).thenReturn(config);
        lenient().when(config.get(ProcessConfig.class)).thenReturn(processConfig);
        lenient().when(processConfig.authTokenProvider()).thenReturn(authTokenProvider);
    }

    @Test
    void testGetBearerToken_ConfiguredStrategy_WithToken() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "configured");
        parameters.put(PROPAGATE_TOKEN_PARAM, CONFIGURED_TOKEN);

        Optional<String> result = decorator.getBearerToken(parameters, handler);

        assertThat(result).isPresent().contains(CONFIGURED_TOKEN);
        verify(authTokenProvider, never()).getAuthToken();
    }

    @Test
    void testGetBearerToken_ConfiguredStrategy_WithoutToken() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "configured");

        Optional<String> result = decorator.getBearerToken(parameters, handler);

        assertThat(result).isEmpty();
        verify(authTokenProvider, never()).getAuthToken();
    }

    @Test
    void testGetBearerToken_PropagateStrategy_WithPropagatedToken() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "propagate");
        when(authTokenProvider.getAuthToken()).thenReturn(Optional.of(PROPAGATED_TOKEN));

        Optional<String> result = decorator.getBearerToken(parameters, handler);

        assertThat(result).isPresent().contains(PROPAGATED_TOKEN);
        verify(authTokenProvider).getAuthToken();
    }

    @Test
    void testGetBearerToken_PropagateStrategy_FallbackToConfigured() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "propagate");
        parameters.put(PROPAGATE_TOKEN_PARAM, CONFIGURED_TOKEN);
        when(authTokenProvider.getAuthToken()).thenReturn(Optional.empty());

        Optional<String> result = decorator.getBearerToken(parameters, handler);

        assertThat(result).isPresent().contains(CONFIGURED_TOKEN);
        verify(authTokenProvider).getAuthToken();
    }

    @Test
    void testGetBearerToken_PropagateStrategy_NoTokensAvailable() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "propagate");
        when(authTokenProvider.getAuthToken()).thenReturn(Optional.empty());

        Optional<String> result = decorator.getBearerToken(parameters, handler);

        assertThat(result).isEmpty();
        verify(authTokenProvider).getAuthToken();
    }

    @Test
    void testGetBearerToken_NoneStrategy() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "none");
        parameters.put(PROPAGATE_TOKEN_PARAM, CONFIGURED_TOKEN);

        Optional<String> result = decorator.getBearerToken(parameters, handler);

        assertThat(result).isEmpty();
        verify(authTokenProvider, never()).getAuthToken();
    }

    @Test
    void testGetBearerToken_UnknownStrategy_DefaultsToNone() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "unknown-strategy");
        parameters.put(PROPAGATE_TOKEN_PARAM, CONFIGURED_TOKEN);

        Optional<String> result = decorator.getBearerToken(parameters, handler);

        assertThat(result).isEmpty();
        verify(authTokenProvider, never()).getAuthToken();
    }

    @Test
    void testGetBearerToken_NoStrategySpecified_DefaultsToNone() {
        // No strategy specified
        parameters.put(PROPAGATE_TOKEN_PARAM, CONFIGURED_TOKEN);

        Optional<String> result = decorator.getBearerToken(parameters, handler);

        assertThat(result).isEmpty();
        verify(authTokenProvider, never()).getAuthToken();
    }

    @Test
    void testDecorate_WithToken_AddsAuthenticationHeader() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "configured");
        parameters.put(PROPAGATE_TOKEN_PARAM, CONFIGURED_TOKEN);

        decorator.decorate(workItem, parameters, httpRequest, handler);

        verify(httpRequest).bearerTokenAuthentication(CONFIGURED_TOKEN);
    }

    @Test
    void testDecorate_WithoutToken_DoesNotAddAuthenticationHeader() {

        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "none");

        decorator.decorate(workItem, parameters, httpRequest, handler);

        verify(httpRequest, never()).bearerTokenAuthentication(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void testDecorate_PropagateStrategy_UsesPropagatedToken() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "propagate");
        when(authTokenProvider.getAuthToken()).thenReturn(Optional.of(PROPAGATED_TOKEN));

        decorator.decorate(workItem, parameters, httpRequest, handler);

        verify(httpRequest).bearerTokenAuthentication(PROPAGATED_TOKEN);
        verify(authTokenProvider).getAuthToken();
    }

    @Test
    void testDecorate_PropagateStrategy_FallsBackToConfiguredToken() {
        parameters.put(ACCESS_TOKEN_ACQUISITION_STRATEGY, "propagate");
        parameters.put(PROPAGATE_TOKEN_PARAM, CONFIGURED_TOKEN);
        when(authTokenProvider.getAuthToken()).thenReturn(Optional.empty());

        decorator.decorate(workItem, parameters, httpRequest, handler);

        verify(httpRequest).bearerTokenAuthentication(CONFIGURED_TOKEN);
        verify(authTokenProvider).getAuthToken();
    }

    @Test
    void testAccessTokenAcquisitionStrategy_FromName() {
        assertThat(AccessTokenAcquisitionStrategy.fromName("none")).isEqualTo(AccessTokenAcquisitionStrategy.NONE);
        assertThat(AccessTokenAcquisitionStrategy.fromName("configured")).isEqualTo(AccessTokenAcquisitionStrategy.CONFIGURED);
        assertThat(AccessTokenAcquisitionStrategy.fromName("propagate")).isEqualTo(AccessTokenAcquisitionStrategy.PROPAGATE);
        assertThat(AccessTokenAcquisitionStrategy.fromName("invalid")).isEqualTo(AccessTokenAcquisitionStrategy.NONE);
        assertThat(AccessTokenAcquisitionStrategy.fromName(null)).isEqualTo(AccessTokenAcquisitionStrategy.NONE);
    }
}
