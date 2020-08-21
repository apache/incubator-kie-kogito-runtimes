/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.auth;

import java.util.Collection;
import java.util.Collections;

public class IdentityProviders {

    public static final String UNKNOWN_USER_IDENTITY = "unknown";

    private static class DefaultIdentityProvider implements IdentityProvider {

        private String name;
        private Collection<String> roles;

        public DefaultIdentityProvider(String name, Collection<String> roles) {
            this.name = name;
            this.roles = roles;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Collection<String> getRoles() {
            return roles;
        }

        @Override
        public boolean hasRole(String role) {
            return roles.contains(role);
        }

    }

    public static IdentityProvider of(String name) {
        return new DefaultIdentityProvider(name, Collections.emptyList());
    }

    public static IdentityProvider of(String name, Collection<String> roles) {
        return new DefaultIdentityProvider(name, roles);
    }

    private IdentityProviders() {}

}
