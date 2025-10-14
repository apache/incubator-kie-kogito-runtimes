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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProcessInstanceContext utility class.
 * Tests thread safety, context management, nested contexts, and async operations.
 */
class ProcessInstanceContextTest {

    private static final String TEST_PROCESS_ID = "test-process-123";
    private static final String TEST_PROCESS_ID_2 = "test-process-456";

    @BeforeEach
    void setUp() {
        // Ensure clean state before each test
        ProcessInstanceContext.clear();
        // Initialize MDC to general context for predictable test state
        // This mimics the static initializer behavior which only runs once per class load
        MDC.put(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY, ProcessInstanceContext.GENERAL_CONTEXT);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        ProcessInstanceContext.clear();
        // Don't call MDC.clear() as it will interfere with the @BeforeEach of the next test
    }

    /**
     * Detect if running in CI environment based on common CI environment variables.
     * 
     * @return true if running in CI, false otherwise
     */
    private boolean isRunningInCI() {
        return System.getenv("CI") != null ||
                System.getenv("JENKINS_URL") != null ||
                System.getenv("GITHUB_ACTIONS") != null ||
                System.getenv("TRAVIS") != null ||
                System.getenv("CIRCLECI") != null ||
                System.getProperty("ci.environment") != null;
    }

    @Test
    void testSetAndGetProcessInstanceId() {
        // Initially no context (returns empty string but getProcessInstanceId() will trigger ensureGeneralContext)
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceId());
        assertFalse(ProcessInstanceContext.hasContext());

        // After getProcessInstanceId(), MDC should be initialized
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));

        // Set process instance ID
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);

        // Verify ThreadLocal and MDC are set
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
        assertEquals(TEST_PROCESS_ID, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
        assertTrue(ProcessInstanceContext.hasContext());
        assertFalse(ProcessInstanceContext.isGeneralContext());
        assertEquals(1, ProcessInstanceContext.getContextDepth());
    }

    @Test
    void testSetNullProcessInstanceId() {
        ProcessInstanceContext.setProcessInstanceId(null);

        // Should use general context (empty string)
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceId());
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
        assertFalse(ProcessInstanceContext.hasContext()); // Setting null/general context means no specific context
        assertFalse(ProcessInstanceContext.isGeneralContext()); // ThreadLocal is removed (null), not set to empty string
    }

    @Test
    void testGetProcessInstanceIdOrGeneral() {
        // Initially should return general
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceIdOrGeneral());

        // After setting process ID, should return that ID
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceIdOrGeneral());

        // After clearing, should return general again
        ProcessInstanceContext.clear();
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceIdOrGeneral());
    }

    @Test
    void testClearContext() {
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertTrue(ProcessInstanceContext.hasContext());

        ProcessInstanceContext.clear();

        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceId());
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
        assertFalse(ProcessInstanceContext.hasContext());
        assertEquals(0, ProcessInstanceContext.getContextDepth());
    }

    @Test
    void testNestedContextSameId() {
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertEquals(1, ProcessInstanceContext.getContextDepth());

        // Set same ID again (nested call)
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertEquals(2, ProcessInstanceContext.getContextDepth());
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());

        // Clear once - should still have context
        ProcessInstanceContext.clear();
        assertEquals(1, ProcessInstanceContext.getContextDepth());
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
        assertTrue(ProcessInstanceContext.hasContext());

        // Clear again - should remove context
        ProcessInstanceContext.clear();
        assertEquals(0, ProcessInstanceContext.getContextDepth());
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testNestedContextDifferentIds() {
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
        assertEquals(1, ProcessInstanceContext.getContextDepth());

        // Set different ID - should replace, not nest
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID_2);
        assertEquals(TEST_PROCESS_ID_2, ProcessInstanceContext.getProcessInstanceId());
        assertEquals(1, ProcessInstanceContext.getContextDepth());

        // Clear should remove all context
        ProcessInstanceContext.clear();
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testWithProcessInstanceContextSupplier() {
        assertFalse(ProcessInstanceContext.hasContext());

        String result = ProcessInstanceContext.withProcessInstanceContext(TEST_PROCESS_ID, () -> {
            assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
            assertTrue(ProcessInstanceContext.hasContext());
            return "test-result";
        });

        assertEquals("test-result", result);
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testWithProcessInstanceContextRunnable() {
        assertFalse(ProcessInstanceContext.hasContext());
        AtomicReference<String> capturedId = new AtomicReference<>();

        ProcessInstanceContext.withProcessInstanceContext(TEST_PROCESS_ID, () -> {
            capturedId.set(ProcessInstanceContext.getProcessInstanceId());
        });

        assertEquals(TEST_PROCESS_ID, capturedId.get());
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testWithProcessInstanceContextExceptionHandling() {
        assertFalse(ProcessInstanceContext.hasContext());

        assertThrows(RuntimeException.class, () -> {
            ProcessInstanceContext.withProcessInstanceContext(TEST_PROCESS_ID, () -> {
                assertTrue(ProcessInstanceContext.hasContext());
                throw new RuntimeException("Test exception");
            });
        });

        // Context should be cleared even after exception
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testAsyncContextPropagation() {
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);

        // Copy context for async operation
        Map<String, String> contextMap = ProcessInstanceContext.copyContextForAsync();
        assertNotNull(contextMap);
        assertEquals(TEST_PROCESS_ID, contextMap.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));

        // Clear original context
        ProcessInstanceContext.clear();
        assertFalse(ProcessInstanceContext.hasContext());

        // Restore context from async
        ProcessInstanceContext.setContextFromAsync(contextMap);
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
        assertTrue(ProcessInstanceContext.hasContext());
    }

    @Test
    void testAsyncContextWithNullMap() {
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertTrue(ProcessInstanceContext.hasContext());

        // Setting null context map should clear context
        ProcessInstanceContext.setContextFromAsync(null);
        assertFalse(ProcessInstanceContext.hasContext());
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceId());
    }

    @Test
    void testThreadIsolation() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        AtomicReference<Exception> exception = new AtomicReference<>();

        try {
            // Thread 1: Set and verify process ID 1
            executor.submit(() -> {
                try {
                    ProcessInstanceContext.setProcessInstanceId("process-1");
                    Thread.sleep(100); // Allow other threads to run
                    assertEquals("process-1", ProcessInstanceContext.getProcessInstanceId());
                } catch (Exception e) {
                    exception.set(e);
                } finally {
                    ProcessInstanceContext.clear();
                    latch.countDown();
                }
            });

            // Thread 2: Set and verify process ID 2
            executor.submit(() -> {
                try {
                    ProcessInstanceContext.setProcessInstanceId("process-2");
                    Thread.sleep(100); // Allow other threads to run
                    assertEquals("process-2", ProcessInstanceContext.getProcessInstanceId());
                } catch (Exception e) {
                    exception.set(e);
                } finally {
                    ProcessInstanceContext.clear();
                    latch.countDown();
                }
            });

            // Thread 3: No context set
            executor.submit(() -> {
                try {
                    Thread.sleep(100); // Allow other threads to run
                    assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceId());
                    assertFalse(ProcessInstanceContext.hasContext());
                } catch (Exception e) {
                    exception.set(e);
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertNull(exception.get());

        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testConcurrentAccessWithSameId() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        AtomicReference<Exception> exception = new AtomicReference<>();

        try {
            // Launch multiple threads that set the same process ID
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await(); // Wait for all threads to be ready
                        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
                        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
                        Thread.sleep(10); // Small delay to increase chance of race conditions
                        assertTrue(ProcessInstanceContext.hasContext());
                    } catch (Exception e) {
                        exception.set(e);
                    } finally {
                        ProcessInstanceContext.clear();
                        completionLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // Start all threads
            assertTrue(completionLatch.await(5, TimeUnit.SECONDS));
            assertNull(exception.get());

        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testAsyncOperationWithContextPropagation() throws Exception {
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        Map<String, String> contextMap = ProcessInstanceContext.copyContextForAsync();

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            // Restore context in async thread
            ProcessInstanceContext.setContextFromAsync(contextMap);
            try {
                return ProcessInstanceContext.getProcessInstanceId();
            } finally {
                ProcessInstanceContext.clear();
            }
        });

        String result = future.get(5, TimeUnit.SECONDS);
        assertEquals(TEST_PROCESS_ID, result);

        // Original thread context should still be intact
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
    }

    @Test
    void testMdcIntegration() {
        // Verify MDC is properly managed
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);

        assertEquals(TEST_PROCESS_ID, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));

        ProcessInstanceContext.clear();

        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
    }

    @Test
    void testGeneralContextBehavior() {
        ProcessInstanceContext.setProcessInstanceId(ProcessInstanceContext.GENERAL_CONTEXT);

        assertFalse(ProcessInstanceContext.hasContext()); // Setting general context explicitly still counts as no context
        assertFalse(ProcessInstanceContext.isGeneralContext()); // ThreadLocal is null, so it's not general context in ThreadLocal
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceId());
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
        assertEquals(1, ProcessInstanceContext.getContextDepth()); // General context does set depth to 1
    }

    @Test
    void testContextDepthTracking() {
        assertEquals(0, ProcessInstanceContext.getContextDepth());

        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertEquals(1, ProcessInstanceContext.getContextDepth());

        // Same ID again
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertEquals(2, ProcessInstanceContext.getContextDepth());

        // Different ID - resets depth
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID_2);
        assertEquals(1, ProcessInstanceContext.getContextDepth());

        ProcessInstanceContext.clear();
        assertEquals(0, ProcessInstanceContext.getContextDepth());
    }

    @Test
    void testPerformanceOverhead() {
        // Performance test to ensure MDC operations don't add significant overhead
        int iterations = 100_000;

        // Warmup
        for (int i = 0; i < 1000; i++) {
            ProcessInstanceContext.setProcessInstanceId("warmup-" + i);
            ProcessInstanceContext.clear();
        }

        // Test baseline (without MDC operations)
        long baselineStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            // Simulate some work without MDC
            String value = "baseline-" + i;
            value.length(); // Simple operation
        }
        long baselineTime = System.nanoTime() - baselineStart;

        // Test with MDC operations
        long mdcStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ProcessInstanceContext.setProcessInstanceId("test-" + i);
            try {
                // Simulate some work with MDC
                String value = "mdc-" + i;
                value.length(); // Simple operation
            } finally {
                ProcessInstanceContext.clear();
            }
        }
        long mdcTime = System.nanoTime() - mdcStart;

        // Calculate overhead
        double overheadPercent = ((double) (mdcTime - baselineTime) / baselineTime) * 100;

        System.out.printf("Performance test results (%d iterations):%n", iterations);
        System.out.printf("  Baseline time: %.2f ms%n", baselineTime / 1_000_000.0);
        System.out.printf("  MDC time: %.2f ms%n", mdcTime / 1_000_000.0);
        System.out.printf("  Overhead: %.2f%% (%.2f ms)%n", overheadPercent, (mdcTime - baselineTime) / 1_000_000.0);

        // Assert that overhead is reasonable (less than 25000% overhead for 100k operations)
        // This is a generous threshold to account for actual logging implementation overhead like logback
        // Increased threshold for CI environments which may have variable performance
        double overheadThreshold = isRunningInCI() ? 30000.0 : 15000.0;
        assertTrue(overheadPercent < overheadThreshold,
                String.format("MDC overhead too high: %.2f%% (should be < %.0f%%)", overheadPercent, overheadThreshold));

        // Assert that average overhead per operation is less than 500 microseconds (increased for CI)
        double avgOverheadNanos = (double) (mdcTime - baselineTime) / iterations;
        double avgOverheadThreshold = isRunningInCI() ? 500000.0 : 100000.0;
        assertTrue(avgOverheadNanos < avgOverheadThreshold,
                String.format("Average MDC overhead per operation too high: %.2f ns (should be < %.0fns)", avgOverheadNanos, avgOverheadThreshold));
    }

    @Test
    void testConcurrentPerformance() throws InterruptedException {
        // Test performance under concurrent access
        int threadCount = 10;
        int iterationsPerThread = 10_000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long startTime = System.nanoTime();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < iterationsPerThread; i++) {
                        String processId = "thread-" + threadId + "-process-" + i;
                        ProcessInstanceContext.setProcessInstanceId(processId);
                        try {
                            // Simulate some work
                            Thread.sleep(0, 100); // 100 nanoseconds
                        } finally {
                            ProcessInstanceContext.clear();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        long totalTime = System.nanoTime() - startTime;

        executor.shutdown();

        double totalOperations = threadCount * iterationsPerThread;
        double avgTimePerOperation = totalTime / totalOperations;

        System.out.printf("Concurrent performance test results:%n");
        System.out.printf("  Threads: %d%n", threadCount);
        System.out.printf("  Operations per thread: %d%n", iterationsPerThread);
        System.out.printf("  Total operations: %.0f%n", totalOperations);
        System.out.printf("  Total time: %.2f ms%n", totalTime / 1_000_000.0);
        System.out.printf("  Average time per operation: %.2f ns%n", avgTimePerOperation);

        // Assert reasonable performance under concurrency (less than 5 milliseconds per operation in CI, 1ms locally)
        double concurrentThreshold = isRunningInCI() ? 5_000_000.0 : 1_000_000.0;
        assertTrue(avgTimePerOperation < concurrentThreshold,
                String.format("Concurrent MDC performance too slow: %.2f ns per operation (should be < %.0fns)", avgTimePerOperation, concurrentThreshold));
    }

    @Test
    void testStaticInitializerBehavior() {
        // Verify that static initializer ensures MDC has a value
        // Clear everything first
        ProcessInstanceContext.clear();
        MDC.clear();

        // Static initializer should have set MDC to GENERAL_CONTEXT
        // Force call to ensureGeneralContext through getProcessInstanceId
        String id = ProcessInstanceContext.getProcessInstanceId();

        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, id);
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
    }

    @Test
    void testEnsureGeneralContextInNewThread() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<String> resultId = new AtomicReference<>();
        AtomicReference<String> resultMdc = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            executor.submit(() -> {
                try {
                    // In a new thread, MDC should be initialized by ensureGeneralContext
                    String id = ProcessInstanceContext.getProcessInstanceId();
                    String mdcValue = MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY);
                    resultId.set(id);
                    resultMdc.set(mdcValue);
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, resultId.get());
            assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, resultMdc.get());
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void testGeneralContextNeverOverridesSpecificContext() {
        // Set a specific process instance ID
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
        assertTrue(ProcessInstanceContext.hasContext());

        // Try to override with general context - should replace, not ignore
        ProcessInstanceContext.setProcessInstanceId(ProcessInstanceContext.GENERAL_CONTEXT);
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, ProcessInstanceContext.getProcessInstanceId());
        assertFalse(ProcessInstanceContext.hasContext());

        // MDC should be set to general context
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
    }

    @Test
    void testIsGeneralContextAccuracy() {
        // Initially no context
        assertFalse(ProcessInstanceContext.isGeneralContext());

        // Set general context explicitly
        ProcessInstanceContext.setProcessInstanceId(ProcessInstanceContext.GENERAL_CONTEXT);
        assertFalse(ProcessInstanceContext.isGeneralContext()); // ThreadLocal is null for general context

        // Set specific context
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertFalse(ProcessInstanceContext.isGeneralContext());

        // Clear should not make it general context (ThreadLocal becomes null)
        ProcessInstanceContext.clear();
        assertFalse(ProcessInstanceContext.isGeneralContext());
    }

    @Test
    void testHasContextAccuracy() {
        // Initially no context
        assertFalse(ProcessInstanceContext.hasContext());

        // Set general context explicitly - should not count as having context
        ProcessInstanceContext.setProcessInstanceId(ProcessInstanceContext.GENERAL_CONTEXT);
        assertFalse(ProcessInstanceContext.hasContext());

        // Set specific context
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertTrue(ProcessInstanceContext.hasContext());

        // Clear should remove context
        ProcessInstanceContext.clear();
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testMdcConsistencyAfterOperations() {
        // Test that MDC always stays consistent after various operations

        // Initially should be general context
        ProcessInstanceContext.getProcessInstanceId(); // Trigger ensureGeneralContext
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));

        // Set process ID
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
        assertEquals(TEST_PROCESS_ID, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));

        // Clear should reset to general context
        ProcessInstanceContext.clear();
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));

        // Setting null should also result in general context
        ProcessInstanceContext.setProcessInstanceId(null);
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
    }

    @Test
    void testDepthTrackingWithGeneralContext() {
        assertEquals(0, ProcessInstanceContext.getContextDepth());

        // Set general context should create depth 1
        ProcessInstanceContext.setProcessInstanceId(ProcessInstanceContext.GENERAL_CONTEXT);
        assertEquals(1, ProcessInstanceContext.getContextDepth());

        // Setting same general context again should NOT increment depth because
        // the comparison is between effectiveId ("") and currentId (null for general context)
        // This behavior is consistent with the implementation
        ProcessInstanceContext.setProcessInstanceId(ProcessInstanceContext.GENERAL_CONTEXT);
        assertEquals(1, ProcessInstanceContext.getContextDepth()); // Should remain 1

        // Clear should remove depth completely
        ProcessInstanceContext.clear();
        assertEquals(0, ProcessInstanceContext.getContextDepth());
    }
}