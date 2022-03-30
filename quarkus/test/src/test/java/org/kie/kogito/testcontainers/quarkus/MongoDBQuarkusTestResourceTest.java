/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.testcontainers.quarkus;

import org.junit.jupiter.api.Test;
import org.kie.kogito.testcontainers.Constants;
import org.kie.kogito.testcontainers.KogitoMongoDBContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

class MongoDBQuarkusTestResourceTest {

    private MongoDBQuarkusTestResource resource;

    @Test
    void shouldGetProperty() {
        givenResource();
        assertThrows(IllegalStateException.class, () -> resource.getProperties().get(MongoDBQuarkusTestResource.MONGODB_CONNECTION_PROPERTY));
    }

    @Test
    void shouldConditionalBeDisabledByDefault() {
        givenResource();
        thenConditionalIsDisabled();
    }

    @Test
    void shouldConditionalBeEnabled() {
        givenConditionalResource();
        thenConditionalIsEnabled();
    }

    @Test
    void shouldGetDockerImageName() {
        givenResource();
        assertEquals(System.getProperty(Constants.CONTAINER_NAME_PREFIX + KogitoMongoDBContainer.NAME), resource.getTestResource().getDockerImageName());
    }

    private void givenConditionalResource() {
        resource = spy(new MongoDBQuarkusTestResource.Conditional());
    }

    private void givenResource() {
        resource = spy(new MongoDBQuarkusTestResource());
    }

    private void thenConditionalIsEnabled() {
        assertTrue(resource.isConditionalEnabled());
    }

    private void thenConditionalIsDisabled() {
        assertFalse(resource.isConditionalEnabled());
    }
}
