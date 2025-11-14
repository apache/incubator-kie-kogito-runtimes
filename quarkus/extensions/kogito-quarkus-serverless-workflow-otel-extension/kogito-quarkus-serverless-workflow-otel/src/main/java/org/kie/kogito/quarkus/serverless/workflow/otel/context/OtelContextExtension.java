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
package org.kie.kogito.quarkus.serverless.workflow.otel.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.kogito.services.context.ContextExtension;
import org.kie.kogito.services.context.ProcessInstanceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * OpenTelemetry context extension for ProcessInstanceContext integration.
 *
 * This extension preserves OTel-specific MDC keys (transaction.id, tracker.* attributes)
 * during async operations and context switches managed by ProcessInstanceContext.
 *
 * The extension is automatically registered via CDI lifecycle (@PostConstruct) and
 * participates in the 3-phase context restoration lifecycle:
 * 1. beforeContextRestore - Preserve OTel keys from incoming context
 * 2. Core restoration - ProcessInstanceContext filters and restores MDC
 * 3. afterContextRestore - Restore preserved OTel keys to MDC
 *
 * Thread Safety: This class is thread-safe, using ThreadLocal-backed storage for
 * preserved keys and MDC's inherent thread-local isolation.
 */
@ApplicationScoped
public class OtelContextExtension implements ContextExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(OtelContextExtension.class);
    private static final String EXTENSION_ID = "otel";
    private static final String MDC_KEY_PREFIX = "otel.";

    private final ThreadLocal<Map<String, String>> preservedKeys = ThreadLocal.withInitial(ConcurrentHashMap::new);

    @PostConstruct
    void register() {
        ProcessInstanceContext.registerContextExtension(this);
        LOGGER.debug("OtelContextExtension registered with ProcessInstanceContext");
    }

    @Override
    public String getExtensionId() {
        return EXTENSION_ID;
    }

    @Override
    public String getMdcKeyPrefix() {
        return MDC_KEY_PREFIX;
    }

    @Override
    public void beforeContextRestore(Map<String, String> incomingContext) {
        Map<String, String> preserved = preservedKeys.get();
        preserved.clear();

        if (incomingContext != null) {
            incomingContext.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(MDC_KEY_PREFIX))
                    .forEach(entry -> preserved.put(entry.getKey(), entry.getValue()));

            if (!preserved.isEmpty()) {
                LOGGER.trace("Preserved {} OTel MDC keys for restoration", preserved.size());
            }
        }
    }

    @Override
    public void afterContextRestore() {
        Map<String, String> preserved = preservedKeys.get();

        if (!preserved.isEmpty()) {
            preserved.forEach(MDC::put);
            LOGGER.trace("Restored {} OTel MDC keys after context restoration", preserved.size());
            preserved.clear();
        }
    }

    /**
     * Cleans up the ThreadLocal storage for the current thread.
     * This should be called when a thread is finished processing to prevent memory leaks.
     */
    public void cleanup() {
        preservedKeys.remove();
    }
}
