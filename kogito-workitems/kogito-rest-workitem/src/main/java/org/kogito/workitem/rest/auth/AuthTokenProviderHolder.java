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

import org.kie.kogito.auth.AuthTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthTokenProviderHolder {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenProviderHolder.class);
    private static AuthTokenProvider instance;

    private static final String QUARKUS_PROVIDER_CLASS = "org.kie.kogito.quarkus.auth.QuarkusAuthTokenProvider";
    private static final String SPRING_BOOT_PROVIDER_CLASS = "org.kogito.addons.springboot.common.rest.workitem.SpringBootAuthTokenProvider";

    static {
        loadProvider();
    }

    private static void loadProvider() {
        instance = tryLoadProvider(QUARKUS_PROVIDER_CLASS);

        if (instance == null) {
            instance = tryLoadProvider(SPRING_BOOT_PROVIDER_CLASS);
        }

        if (instance == null) {
            logger.debug("No AuthTokenProvider implementation found. Token propagation from security context will not be available.");
        }
    }

    private static AuthTokenProvider tryLoadProvider(String className) {
        try {
            Class<?> providerClass = Class.forName(className);
            AuthTokenProvider provider = (AuthTokenProvider) providerClass.getDeclaredConstructor().newInstance();
            logger.info("Loaded AuthTokenProvider implementation: {}", className);
            return provider;
        } catch (ClassNotFoundException e) {
            logger.debug("AuthTokenProvider implementation not found: {}", className);
            return null;
        } catch (Exception e) {
            logger.warn("Failed to instantiate AuthTokenProvider {}: {}", className, e.getMessage());
            return null;
        }
    }

    public static Optional<AuthTokenProvider> getProvider() {
        return Optional.ofNullable(instance);
    }

    public static void setProvider(AuthTokenProvider provider) {
        instance = provider;
        logger.debug("AuthTokenProvider manually set to: {}", provider != null ? provider.getClass().getName() : "null");
    }

    public static void reset() {
        instance = null;
        loadProvider();
    }

    private AuthTokenProviderHolder() {

    }
}
