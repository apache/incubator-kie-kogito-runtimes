/**
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kie.kogito.resources;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConditionalSpringBootTestResourceTest {

    private static final int MAPPED_PORT = 8800;
    private static final String RESOURCE_NAME = "my-resource";
    private static final String KOGITO_PROPERTY = "my-kogito-property";
    private static final String KOGITO_PROPERTY_VALUE = "localhost:" + MAPPED_PORT;

    @Mock
    private TestResource resource;

    @Mock
    private ConditionHolder conditional;

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    private ConditionalSpringBootTestResource instance;

    private Map<String, String> actualOutput;

    @BeforeEach
    public void setup() {
        actualOutput = new HashMap<>();
        instance = new ConditionalSpringBootTestResource(resource, conditional) {

            @Override
            protected String getKogitoProperty() {
                return KOGITO_PROPERTY;
            }

            @Override
            protected void updateContextProperty(ConfigurableApplicationContext applicationContext, String key, String value) {
                actualOutput.put(key, value);
            }

        };

        lenient().when(resource.getResourceName()).thenReturn(RESOURCE_NAME);
        lenient().when(resource.getMappedPort()).thenReturn(MAPPED_PORT);
    }

    @Test
    public void shouldReturnResource() {
        assertEquals(resource, instance.getTestResource());
    }

    @Test
    public void shouldInvokeConditional() {
        instance.enableConditional();
        verify(conditional).enableConditional();
    }

    @Test
    public void shouldStartIfConditionalIsEnabled() {
        givenConditionalEnabled();
        whenStartInstance();
        thenResourceIsStarted();
        thenConfigMapIsUpdated();
        thenShutdownHookIsRegistered();
    }

    @Test
    public void shouldNotStartIfConditionalIsDisabled() {
        givenConditionalDisabled();
        whenStartInstance();
        thenResourceIsNotUsed();
    }

    @Test
    public void shouldStopIfConditionalIsEnabled() {
        givenConditionalEnabled();
        whenStopInstance();
        thenResourceIsStopped();
    }

    @Test
    public void shouldNotStopIfConditionalIsDisabled() {
        givenConditionalDisabled();
        whenStopInstance();
        thenResourceIsNotUsed();
    }

    private void givenConditionalEnabled() {
        when(conditional.isEnabled()).thenReturn(true);
    }

    private void givenConditionalDisabled() {
        when(conditional.isEnabled()).thenReturn(false);
    }

    private void whenStartInstance() {
        ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);
        lenient().when(applicationContext.getBeanFactory()).thenReturn(beanFactory);
        instance.initialize(applicationContext);
    }

    private void whenStopInstance() {
        instance.onApplicationEvent(mock(ContextClosedEvent.class));
    }

    private void thenResourceIsStarted() {
        verify(resource).start();
    }

    private void thenResourceIsStopped() {
        verify(resource).stop();
    }

    private void thenResourceIsNotUsed() {
        verifyNoInteractions(resource);
    }

    private void thenConfigMapIsUpdated() {
        String actual = actualOutput.get(KOGITO_PROPERTY);
        assertEquals(KOGITO_PROPERTY_VALUE, actual);
    }

    private void thenShutdownHookIsRegistered() {
        verify(beanFactory).registerSingleton(RESOURCE_NAME + "ShutDownHook", instance);
    }
}
