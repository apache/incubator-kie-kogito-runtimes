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
package org.kie.kogito.quarkus.serverless.workflow.otel;

import java.io.IOException;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.ContextKeys;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.RequestProperties;

/**
 * JAX-RS filter for extracting OpenTelemetry context from HTTP headers.
 *
 * This filter automatically extracts X-TRANSACTION-ID and X-TRACKER-* headers
 * from incoming HTTP requests and makes them available for OpenTelemetry span
 * attribute enrichment during workflow execution.
 *
 * The extracted context is stored in the request context properties for later
 * consumption by the NodeOtelEventListener during workflow processing.
 */
@Provider
@PreMatching
public class OtelHeaderFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    HeaderContextExtractor headerExtractor;

    @ConfigProperty(name = "kogito.sw.otel.enabled", defaultValue = "true")
    boolean otelEnabled;

    /**
     * Filter incoming requests to extract OpenTelemetry context from headers.
     *
     * This method extracts the X-TRANSACTION-ID header and any X-TRACKER-* headers
     * from the incoming request and stores them as request context properties.
     * These properties are later consumed by the OpenTelemetry event listeners
     * to enrich spans with correlation context.
     *
     * @param requestContext the incoming request context
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip processing if OpenTelemetry is disabled
        if (!otelEnabled) {
            return;
        }

        // Extract headers using the existing HeaderContextExtractor
        Map<String, String> extractedContext = headerExtractor.extractHeaders(requestContext.getHeaders());

        // Store extracted context in request properties for later consumption
        if (!extractedContext.isEmpty()) {
            requestContext.setProperty(ContextKeys.EXTRACTED_CONTEXT, extractedContext);

            // Also store in ProcessInstanceContext for access during workflow execution
            // This leverages the existing MDC-based context management
            for (Map.Entry<String, String> entry : extractedContext.entrySet()) {
                if (RequestProperties.TRANSACTION_ID.equals(entry.getKey())) {
                    // Store transaction ID in a thread-local context for access during workflow execution
                    OtelContextHolder.setTransactionId(entry.getValue());
                } else if (entry.getKey().startsWith(RequestProperties.TRACKER_PREFIX)) {
                    // Store tracker attributes for access during workflow execution
                    OtelContextHolder.setTrackerAttribute(entry.getKey(), entry.getValue());
                }
            }
        }

        // Store transaction ID separately for easy access
        String transactionId = extractedContext.get(RequestProperties.TRANSACTION_ID);
        if (transactionId != null) {
            requestContext.setProperty(ContextKeys.TRANSACTION_ID, transactionId);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        OtelContextHolder.clear();
    }
}
