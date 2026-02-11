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

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthTokenProviderHolder {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenProviderHolder.class);
    private static volatile AuthTokenProvider instance;

    private AuthTokenProviderHolder() {
    }

    public static AuthTokenProvider getInstance() {
        if (instance == null) {
            synchronized (AuthTokenProviderHolder.class) {
                if (instance == null) {
                    instance = loadProvider();
                }
            }
        }
        return instance;
    }

    public static void setInstance(AuthTokenProvider provider) {
        synchronized (AuthTokenProviderHolder.class) {
            instance = provider;
        }
    }

    public static void reset() {
        synchronized (AuthTokenProviderHolder.class) {
            instance = null;
        }
    }

    private static AuthTokenProvider loadProvider() {
        try {
            ServiceLoader<AuthTokenProvider> loader = ServiceLoader.load(AuthTokenProvider.class);
            Iterator<AuthTokenProvider> iterator = loader.iterator();

            while (iterator.hasNext()) {
                try {
                    AuthTokenProvider provider = iterator.next();
                    if (provider.isAvailable()) {
                        logger.info("Loaded AuthTokenProvider: {}", provider.getClass().getName());
                        return provider;
                    } else {
                        logger.debug("AuthTokenProvider {} is not available in current environment",
                                provider.getClass().getName());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to load AuthTokenProvider", e);
                }
            }

            logger.warn("No AuthTokenProvider found via ServiceLoader. Token resolution will not be available.");
            return null;
        } catch (Exception e) {
            logger.error("Error loading AuthTokenProvider via ServiceLoader", e);
            return null;
        }
    }
}
