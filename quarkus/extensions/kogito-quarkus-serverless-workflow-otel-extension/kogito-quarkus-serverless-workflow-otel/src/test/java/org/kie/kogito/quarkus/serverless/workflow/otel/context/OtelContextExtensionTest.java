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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.services.context.ProcessInstanceContext;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtelContextExtensionTest {

    private OtelContextExtension extension;

    @BeforeEach
    void setUp() {
        MDC.clear();
        extension = new OtelContextExtension();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        extension.cleanup();
    }

    @Test
    void shouldHaveCorrectExtensionId() {
        assertEquals("otel", extension.getExtensionId());
    }

    @Test
    void shouldHaveCorrectMdcKeyPrefix() {
        assertEquals("otel.", extension.getMdcKeyPrefix());
    }

    @Test
    void shouldRegisterWithProcessInstanceContext() {
        extension.register();

        assertNotNull(ProcessInstanceContext.getExtension("otel"));
        assertEquals(extension, ProcessInstanceContext.getExtension("otel"));
    }

    @Test
    void shouldPreserveOtelKeysBeforeContextRestore() {
        Map<String, String> incomingContext = new HashMap<>();
        incomingContext.put("otel.transaction.id", "tx-123");
        incomingContext.put("otel.tracker.customer.id", "cust-456");
        incomingContext.put("processInstanceId", "proc-789");

        extension.beforeContextRestore(incomingContext);

        MDC.put("otel.transaction.id", "should-be-replaced");
        MDC.put("otel.tracker.customer.id", "should-be-replaced");
    }

    @Test
    void shouldRestorePreservedKeysAfterContextRestore() {
        Map<String, String> incomingContext = new HashMap<>();
        incomingContext.put("otel.transaction.id", "tx-123");
        incomingContext.put("otel.tracker.customer.id", "cust-456");

        extension.beforeContextRestore(incomingContext);

        MDC.clear();
        MDC.put("processInstanceId", "proc-789");

        extension.afterContextRestore();

        assertEquals("tx-123", MDC.get("otel.transaction.id"));
        assertEquals("cust-456", MDC.get("otel.tracker.customer.id"));
        assertEquals("proc-789", MDC.get("processInstanceId"));
    }

    @Test
    void shouldHandleNullIncomingContext() {
        extension.beforeContextRestore(null);
        extension.afterContextRestore();

        assertNull(MDC.get("otel.transaction.id"));
    }

    @Test
    void shouldHandleEmptyIncomingContext() {
        Map<String, String> emptyContext = new HashMap<>();

        extension.beforeContextRestore(emptyContext);
        extension.afterContextRestore();

        assertNull(MDC.get("otel.transaction.id"));
    }

    @Test
    void shouldOnlyPreserveOtelPrefixedKeys() {
        Map<String, String> incomingContext = new HashMap<>();
        incomingContext.put("otel.transaction.id", "tx-123");
        incomingContext.put("otel.tracker.customer.id", "cust-456");
        incomingContext.put("processInstanceId", "proc-789");
        incomingContext.put("other.key", "value");

        extension.beforeContextRestore(incomingContext);

        MDC.clear();

        extension.afterContextRestore();

        assertEquals("tx-123", MDC.get("otel.transaction.id"));
        assertEquals("cust-456", MDC.get("otel.tracker.customer.id"));
        assertNull(MDC.get("processInstanceId"));
        assertNull(MDC.get("other.key"));
    }

    @Test
    void shouldPreserveMultipleTrackerAttributes() {
        Map<String, String> incomingContext = new HashMap<>();
        incomingContext.put("otel.transaction.id", "tx-123");
        incomingContext.put("otel.tracker.customer.id", "cust-1");
        incomingContext.put("otel.tracker.order.id", "order-2");
        incomingContext.put("otel.tracker.region", "us-east-1");

        extension.beforeContextRestore(incomingContext);

        MDC.clear();

        extension.afterContextRestore();

        assertEquals("tx-123", MDC.get("otel.transaction.id"));
        assertEquals("cust-1", MDC.get("otel.tracker.customer.id"));
        assertEquals("order-2", MDC.get("otel.tracker.order.id"));
        assertEquals("us-east-1", MDC.get("otel.tracker.region"));
    }

    @Test
    void shouldWorkWithProcessInstanceContextIntegration() {
        extension.register();

        MDC.put("otel.transaction.id", "tx-original");
        MDC.put("otel.tracker.customer.id", "cust-original");
        MDC.put("processInstanceId", "proc-original");

        Map<String, String> copiedContext = ProcessInstanceContext.copyContextForAsync();

        MDC.clear();
        MDC.put("processInstanceId", "proc-new");
        MDC.put("otel.transaction.id", "tx-new");

        ProcessInstanceContext.setContextFromAsync(copiedContext);

        assertEquals("proc-original", MDC.get("processInstanceId"));
        assertEquals("tx-original", MDC.get("otel.transaction.id"));
        assertEquals("cust-original", MDC.get("otel.tracker.customer.id"));
    }

    @Test
    void shouldBeThreadSafe() throws InterruptedException {
        extension.register();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean failed = new AtomicBoolean(false);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    MDC.put("otel.transaction.id", "tx-" + threadId);
                    MDC.put("otel.tracker.thread.id", "thread-" + threadId);
                    MDC.put("processInstanceId", "proc-" + threadId);

                    Map<String, String> context = ProcessInstanceContext.copyContextForAsync();

                    MDC.clear();

                    ProcessInstanceContext.setContextFromAsync(context);

                    String txId = MDC.get("otel.transaction.id");
                    String trackerId = MDC.get("otel.tracker.thread.id");
                    String procId = MDC.get("processInstanceId");

                    if (!("tx-" + threadId).equals(txId) ||
                            !("thread-" + threadId).equals(trackerId) ||
                            !("proc-" + threadId).equals(procId)) {
                        failed.set(true);
                    }
                } finally {
                    MDC.clear();
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(!failed.get(), "Thread safety check failed");
    }

    @Test
    void shouldHandleSubsequentContextRestoration() {
        extension.register();

        MDC.put("otel.transaction.id", "tx-1");
        MDC.put("otel.tracker.step", "step-1");
        Map<String, String> context1 = ProcessInstanceContext.copyContextForAsync();

        MDC.clear();
        MDC.put("otel.transaction.id", "tx-2");
        MDC.put("otel.tracker.step", "step-2");
        Map<String, String> context2 = ProcessInstanceContext.copyContextForAsync();

        MDC.clear();

        ProcessInstanceContext.setContextFromAsync(context1);
        assertEquals("tx-1", MDC.get("otel.transaction.id"));
        assertEquals("step-1", MDC.get("otel.tracker.step"));

        ProcessInstanceContext.setContextFromAsync(context2);
        assertEquals("tx-2", MDC.get("otel.transaction.id"));
        assertEquals("step-2", MDC.get("otel.tracker.step"));
    }

    @Test
    void shouldCleanupThreadLocalStorage() {
        Map<String, String> incomingContext = new HashMap<>();
        incomingContext.put("otel.transaction.id", "tx-123");

        extension.beforeContextRestore(incomingContext);
        extension.cleanup();

        extension.afterContextRestore();

        assertNull(MDC.get("otel.transaction.id"));
    }

    @Test
    void shouldHandleContextRestorationWithNullMap() {
        extension.register();

        MDC.put("otel.transaction.id", "tx-123");
        MDC.put("otel.tracker.customer.id", "cust-456");
        MDC.put("processInstanceId", "proc-789");

        ProcessInstanceContext.setContextFromAsync(null);

        // When null is passed, extensions preserve from current MDC before it's cleared
        // This is the expected behavior for preserving context during thread transitions
        assertEquals("tx-123", MDC.get("otel.transaction.id"));
        assertEquals("cust-456", MDC.get("otel.tracker.customer.id"));
        assertEquals("", MDC.get("processInstanceId"));
    }

    @Test
    void shouldPreserveContextAcrossAsyncBoundary() {
        extension.register();

        MDC.put("processInstanceId", "proc-123");
        MDC.put("otel.transaction.id", "tx-456");
        MDC.put("otel.tracker.customer.id", "cust-789");

        Map<String, String> context = MDC.getCopyOfContextMap();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(() -> {
                MDC.setContextMap(context);

                ProcessInstanceContext.setContextFromAsync(context);

                assertEquals("proc-123", MDC.get("processInstanceId"));
                assertEquals("tx-456", MDC.get("otel.transaction.id"));
                assertEquals("cust-789", MDC.get("otel.tracker.customer.id"));

                MDC.clear();
            }).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }
}
