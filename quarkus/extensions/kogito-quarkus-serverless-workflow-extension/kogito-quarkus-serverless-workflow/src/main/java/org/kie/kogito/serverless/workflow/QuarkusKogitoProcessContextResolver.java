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
package org.kie.kogito.serverless.workflow;

import java.util.Map;
import java.util.function.Function;

import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.serverless.workflow.utils.KogitoProcessContextResolverExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Arc;
import io.quarkus.security.identity.SecurityIdentity;

public class QuarkusKogitoProcessContextResolver implements KogitoProcessContextResolverExtension {

    private static final Logger logger = LoggerFactory.getLogger(QuarkusKogitoProcessContextResolver.class);

    @Override
    public Map<String, Function<KogitoProcessContext, Object>> getKogitoProcessContextResolver() {
        return Map.of("identity", this::resolveInitiator);
    }

    private String resolveInitiator(KogitoProcessContext context) {
        try {
            SecurityIdentity identity = Arc.container().select(SecurityIdentity.class).get();
            return identity.isAnonymous() ? "anonymous" : identity.getPrincipal().getName();
        } catch (RuntimeException ex) {
            logger.warn("Unable to resolve quarkus user identity", ex);
            return null;
        }
    }

}
