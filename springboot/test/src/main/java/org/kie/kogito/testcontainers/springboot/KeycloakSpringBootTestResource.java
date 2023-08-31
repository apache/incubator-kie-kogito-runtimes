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
package org.kie.kogito.testcontainers.springboot;

import java.util.Map;

import org.kie.kogito.test.resources.ConditionalSpringBootTestResource;
import org.kie.kogito.testcontainers.KogitoKeycloakContainer;

import static java.util.Collections.singletonMap;

/**
 * Keycloak spring boot resource that works within the test lifecycle.
 *
 */
public class KeycloakSpringBootTestResource extends ConditionalSpringBootTestResource<KogitoKeycloakContainer> {

    public static final String KOGITO_KEYCLOAK_PROPERTY = "keycloak.auth-server-url";

    public KeycloakSpringBootTestResource() {
        super(new KogitoKeycloakContainer());
    }

    @Override
    protected Map<String, String> getProperties() {
        return singletonMap(KOGITO_KEYCLOAK_PROPERTY, String.format("http://localhost:%s", getTestResource().getMappedPort()));
    }

    public static class Conditional extends KeycloakSpringBootTestResource {

        public Conditional() {
            super();
            enableConditional();
        }
    }

}
