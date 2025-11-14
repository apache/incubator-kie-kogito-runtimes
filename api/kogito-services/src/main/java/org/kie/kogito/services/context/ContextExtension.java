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
package org.kie.kogito.services.context;

import java.util.Map;

/**
 * Interface for context extensions that need to participate in MDC context preservation
 * during async operations and context switches in ProcessInstanceContext.
 *
 * Extensions can register themselves with ProcessInstanceContext to preserve their
 * MDC keys during context restoration operations.
 *
 * Thread Safety: Implementations must be thread-safe as they may be invoked
 * concurrently from multiple threads.
 */
public interface ContextExtension {

    /**
     * Returns the unique identifier for this extension.
     * This ID is used for registration and must be unique across all extensions.
     *
     * @return the extension ID, must not be null or empty
     */
    String getExtensionId();

    /**
     * Returns the MDC key prefix that this extension uses.
     * All MDC keys with this prefix will be preserved during context restoration.
     *
     * @return the MDC key prefix (e.g., "otel.", "custom."), must not be null or empty
     */
    String getMdcKeyPrefix();

    /**
     * Called before context restoration with the incoming context map.
     * Extensions should extract and preserve their keys from the incoming context
     * for restoration in afterContextRestore().
     *
     * @param incomingContext the context map that will be restored, may be null
     */
    void beforeContextRestore(Map<String, String> incomingContext);

    /**
     * Called after context restoration.
     * Extensions can use this to restore their state after the MDC has been updated.
     */
    void afterContextRestore();
}
