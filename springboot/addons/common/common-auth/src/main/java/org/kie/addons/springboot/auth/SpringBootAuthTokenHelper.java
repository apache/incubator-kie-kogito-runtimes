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

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass({ SecurityContextHolder.class })
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SpringBootAuthTokenHelper {

    public static final String BEARER_TOKEN_TEMPLATE = "Bearer %s";

    private final List<PrincipalAuthTokenReader> authTokenReaders;

    public SpringBootAuthTokenHelper(@Autowired List<PrincipalAuthTokenReader> authTokenReaders) {
        this.authTokenReaders = authTokenReaders;
    }

    public Optional<String> getAuthToken() {
        SecurityContext securityContext = SecurityContextHolder.getContext();

        if (securityContext == null || securityContext.getAuthentication() == null) {
            return Optional.empty();
        }

        Object principal = securityContext.getAuthentication().getPrincipal();

        return this.authTokenReaders.stream()
                .filter(reader -> reader.acceptsPrincipal(principal)).findFirst()
                .map(reader -> BEARER_TOKEN_TEMPLATE.formatted(reader.readToken(principal)));
    }
}
