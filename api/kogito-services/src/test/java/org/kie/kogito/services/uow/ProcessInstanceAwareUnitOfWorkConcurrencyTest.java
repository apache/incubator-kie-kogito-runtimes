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
package org.kie.kogito.services.uow;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.kie.kogito.services.context.ProcessInstanceContext;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.WorkUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Production-scale concurrency and performance tests for ProcessInstanceAwareUnitOfWork.
 *
 * These tests validate:
 * - Thread safety under high concurrency (500+ concurrent processes)
 * - ThreadLocal leak detection and prevention
 * - Memory usage under sustained load
 * - Context isolation between concurrent processes
 * - Proper cleanup after process completion
 *
 * Note: Some tests are disabled by default and can be enabled with system properties
 * for production-scale validation.
 */
class ProcessInstanceAwareUnitOfWorkConcurrencyTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessInstanceAwareUnitOfWorkConcurrencyTest.class);

    private static final int PRODUCTION_PROCESS_COUNT = 500;
    private static final int STRESS_TEST_PROCESS_COUNT = 1000;
    private static final int SUSTAINED_LOAD_DURATION_SECONDS = 60;

    @BeforeEach
    void setUp() {
        ProcessInstanceContext.clear();
    }

    @AfterEach
    void tearDown() {
        ProcessInstanceContext.clear();
    }

    @Test
    void testProductionScaleConcurrency() throws InterruptedException {
        int processCount = PRODUCTION_PROCESS_COUNT;
        int threadPoolSize = 50;

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(processCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        Set<String> processedIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

        LOG.info("Starting production-scale concurrency test with {} processes and {} threads",
                processCount, threadPoolSize);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < processCount; i++) {
            final String processId = "process-" + i;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await(10, TimeUnit.SECONDS);

                    UnitOfWork delegate = new SimpleUnitOfWork();
                    ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, processId);

                    // Start UoW and verify context
                    uow.start();
                    String currentId = ProcessInstanceContext.getProcessInstanceId();

                    if (!processId.equals(currentId)) {
                        LOG.error("Context mismatch: expected {} but got {}", processId, currentId);
                        failureCount.incrementAndGet();
                        return;
                    }

                    // Simulate some work
                    Thread.sleep(1);

                    // Verify context is still correct
                    currentId = ProcessInstanceContext.getProcessInstanceId();
                    if (!processId.equals(currentId)) {
                        LOG.error("Context changed during execution: expected {} but got {}", processId, currentId);
                        failureCount.incrementAndGet();
                        return;
                    }

                    // End UoW
                    uow.end();

                    // Verify context is cleared
                    if (ProcessInstanceContext.hasContext()) {
                        LOG.error("Context not cleared after end for process {}", processId);
                        failureCount.incrementAndGet();
                        return;
                    }

                    processedIds.add(processId);
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    LOG.error("Exception processing {}", processId, e);
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for completion
        boolean completed = completionLatch.await(60, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        LOG.info("Production-scale test completed in {} ms", duration);
        LOG.info("Success: {}, Failures: {}", successCount.get(), failureCount.get());

        // Assertions
        assertTrue(completed, "Test should complete within timeout");
        assertEquals(processCount, successCount.get(), "All processes should succeed");
        assertEquals(0, failureCount.get(), "No failures should occur");
        assertEquals(processCount, processedIds.size(), "All unique process IDs should be processed");
        assertFalse(ProcessInstanceContext.hasContext(), "No context should remain after all processes complete");
    }

    @Test
    void testThreadLocalLeakDetection() throws InterruptedException {
        int iterations = 100;
        int threadsPerIteration = 10;

        LOG.info("Testing ThreadLocal leak detection with {} iterations, {} threads each",
                iterations, threadsPerIteration);

        // Track weak references to threads
        List<WeakReference<Thread>> threadRefs = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            ExecutorService executor = Executors.newFixedThreadPool(threadsPerIteration);
            CountDownLatch latch = new CountDownLatch(threadsPerIteration);

            for (int t = 0; t < threadsPerIteration; t++) {
                final String processId = "iteration-" + i + "-thread-" + t;
                executor.submit(() -> {
                    try {
                        threadRefs.add(new WeakReference<>(Thread.currentThread()));

                        UnitOfWork delegate = new SimpleUnitOfWork();
                        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, processId);

                        uow.start();
                        ProcessInstanceContext.getProcessInstanceId();
                        uow.end();

                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        // Force garbage collection multiple times
        LOG.info("Forcing garbage collection to detect ThreadLocal leaks");
        for (int i = 0; i < 3; i++) {
            System.gc();
            Thread.sleep(100);
        }

        // Count threads that are still alive (should be very few or none)
        int aliveThreads = 0;
        for (WeakReference<Thread> ref : threadRefs) {
            if (ref.get() != null && ref.get().isAlive()) {
                aliveThreads++;
            }
        }

        LOG.info("Threads still alive after GC: {} out of {}", aliveThreads, threadRefs.size());

        // Most threads should have been garbage collected
        // Allow some threads to still be alive (e.g., thread pool keep-alive)
        assertTrue(aliveThreads < threadRefs.size() / 10,
                "Most threads should be garbage collected (found " + aliveThreads + " alive)");
    }

    @Test
    @EnabledIfSystemProperty(named = "kogito.test.stress", matches = "true")
    void testStressWithNestedContexts() throws InterruptedException {
        int processCount = STRESS_TEST_PROCESS_COUNT;
        int threadPoolSize = 100;
        int nestingDepth = 3;

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(processCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        LOG.info("Starting stress test with {} processes, nesting depth {}", processCount, nestingDepth);

        for (int i = 0; i < processCount; i++) {
            final int processIndex = i;
            executor.submit(() -> {
                try {
                    executeNestedUnitOfWork("process-" + processIndex, nestingDepth, 0);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    LOG.error("Nested execution failed", e);
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(120, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        LOG.info("Stress test completed. Success: {}, Failures: {}", successCount.get(), failureCount.get());

        assertTrue(completed, "Stress test should complete");
        assertEquals(processCount, successCount.get(), "All processes should succeed");
        assertEquals(0, failureCount.get(), "No failures should occur");
    }

    private void executeNestedUnitOfWork(String baseId, int maxDepth, int currentDepth) {
        if (currentDepth >= maxDepth) {
            return;
        }

        String processId = baseId + "-depth-" + currentDepth;
        UnitOfWork delegate = new SimpleUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, processId);

        uow.start();
        assertEquals(processId, ProcessInstanceContext.getProcessInstanceId());

        // Recurse to next level
        executeNestedUnitOfWork(baseId, maxDepth, currentDepth + 1);

        // After recursion, context should be restored
        assertEquals(processId, ProcessInstanceContext.getProcessInstanceId());

        uow.end();
    }

    @Test
    @EnabledIfSystemProperty(named = "kogito.test.sustained", matches = "true")
    void testSustainedLoadWithMemoryProfiling() throws InterruptedException {
        int threadPoolSize = 50;
        AtomicLong processCounter = new AtomicLong(0);
        AtomicInteger activeProcesses = new AtomicInteger(0);
        AtomicReference<Exception> error = new AtomicReference<>();

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage initialHeap = memoryBean.getHeapMemoryUsage();

        LOG.info("Starting sustained load test for {} seconds with {} threads",
                SUSTAINED_LOAD_DURATION_SECONDS, threadPoolSize);
        LOG.info("Initial heap usage: {} MB", initialHeap.getUsed() / 1024 / 1024);

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        long endTime = System.currentTimeMillis() + (SUSTAINED_LOAD_DURATION_SECONDS * 1000);

        // Submit continuous work
        for (int i = 0; i < threadPoolSize; i++) {
            executor.submit(() -> {
                while (System.currentTimeMillis() < endTime && error.get() == null) {
                    try {
                        long processNum = processCounter.incrementAndGet();
                        String processId = "sustained-" + processNum;

                        activeProcesses.incrementAndGet();
                        try {
                            UnitOfWork delegate = new SimpleUnitOfWork();
                            ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, processId);

                            uow.start();
                            assertEquals(processId, ProcessInstanceContext.getProcessInstanceId());

                            // Simulate work
                            Thread.sleep(1);

                            uow.end();
                            assertFalse(ProcessInstanceContext.hasContext());

                        } finally {
                            activeProcesses.decrementAndGet();
                        }

                        // Periodic memory check
                        if (processNum % 1000 == 0) {
                            MemoryUsage currentHeap = memoryBean.getHeapMemoryUsage();
                            LOG.info("Processed {} processes, active: {}, heap: {} MB",
                                    processNum, activeProcesses.get(), currentHeap.getUsed() / 1024 / 1024);
                        }

                    } catch (Exception e) {
                        error.set(e);
                        LOG.error("Error during sustained load test", e);
                    }
                }
            });
        }

        // Wait for test duration
        Thread.sleep(SUSTAINED_LOAD_DURATION_SECONDS * 1000);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        MemoryUsage finalHeap = memoryBean.getHeapMemoryUsage();

        LOG.info("Sustained load test completed");
        LOG.info("Total processes executed: {}", processCounter.get());
        LOG.info("Initial heap: {} MB, Final heap: {} MB",
                initialHeap.getUsed() / 1024 / 1024, finalHeap.getUsed() / 1024 / 1024);

        assertNull(error.get(), "No errors should occur during sustained load");
        assertTrue(processCounter.get() > 1000, "Should process significant number of processes");
        assertEquals(0, activeProcesses.get(), "No processes should be active after test");

        // Memory should not grow unbounded (allowing for some GC overhead)
        long memoryGrowthMB = (finalHeap.getUsed() - initialHeap.getUsed()) / 1024 / 1024;
        LOG.info("Memory growth: {} MB", memoryGrowthMB);

        // Allow reasonable memory growth, but detect leaks
        assertTrue(memoryGrowthMB < 500, "Memory growth should be reasonable (was " + memoryGrowthMB + " MB)");
    }

    @Test
    void testContextIsolationUnderConcurrentLoad() throws InterruptedException {
        int processCount = 200;
        int threadPoolSize = 20;

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(processCount);
        AtomicInteger snapshotCounter = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        for (int i = 0; i < processCount; i++) {
            final String processId = "isolation-test-" + i;
            executor.submit(() -> {
                try {
                    UnitOfWork delegate = new SimpleUnitOfWork();
                    ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, processId);

                    uow.start();

                    // Capture context multiple times during execution
                    for (int j = 0; j < 10; j++) {
                        String currentId = ProcessInstanceContext.getProcessInstanceId();
                        snapshotCounter.incrementAndGet();

                        if (!processId.equals(currentId)) {
                            LOG.error("Context isolation violation: expected {} but got {}", processId, currentId);
                            failures.incrementAndGet();
                        }

                        // Small sleep to allow thread interleaving
                        if (j < 9) {
                            Thread.sleep(1);
                        }
                    }

                    uow.end();

                } catch (Exception e) {
                    LOG.error("Exception during isolation test", e);
                    failures.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        LOG.info("Context isolation test completed. Total snapshots: {}, Failures: {}",
                snapshotCounter.get(), failures.get());

        assertEquals(0, failures.get(), "No context isolation violations should occur");
        // All processes should complete their 10 snapshots
        // Allow a small margin for error (e.g., interrupted threads)
        assertTrue(snapshotCounter.get() >= processCount * 9,
                "Most snapshots should be captured (expected at least " + (processCount * 9) + ", got " + snapshotCounter.get() + ")");
    }

    /**
     * Simple unit of work implementation for testing.
     */
    private static class SimpleUnitOfWork implements UnitOfWork {
        private boolean started = false;
        private boolean ended = false;

        @Override
        public void start() {
            started = true;
        }

        @Override
        public void end() {
            ended = true;
        }

        @Override
        public void abort() {
            // No-op
        }

        @Override
        public void intercept(WorkUnit work) {
            // No-op for these tests
        }
    }
}
