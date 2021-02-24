/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.testcontainers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class KogitoRedisSearchContainerTest {

    private static final int MAPPED_PORT = 6379;
    private static final String IMAGE = "my-redis-image";

    private KogitoRedisSearchContainer container;

    @BeforeEach
    public void setup() {
        System.setProperty(KogitoRedisSearchContainer.REDIS_PROPERTY, IMAGE);
        container = spy(new KogitoRedisSearchContainer());
        doNothing().when(container).start();
        doNothing().when(container).setDockerImageName(IMAGE);
    }

    @Test
    public void shouldAddDefaultSettings() {
        assertTrue(container.getExposedPorts().contains(KogitoRedisSearchContainer.PORT));
    }

    @Test
    public void shouldGetResourceName() {
        assertEquals(KogitoRedisSearchContainer.NAME, container.getResourceName());
    }

    @Test
    public void shouldGetMapperPort() {
        doReturn(MAPPED_PORT).when(container).getMappedPort(KogitoRedisSearchContainer.PORT);
        assertEquals(MAPPED_PORT, container.getMappedPort());
    }

}
