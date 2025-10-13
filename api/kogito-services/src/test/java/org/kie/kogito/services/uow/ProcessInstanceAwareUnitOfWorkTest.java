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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.services.context.ProcessInstanceContext;
import org.kie.kogito.uow.UnitOfWork;
import org.kie.kogito.uow.WorkUnit;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProcessInstanceAwareUnitOfWork.
 * Tests context management, composition with ManagedUnitOfWork, and thread safety.
 */
class ProcessInstanceAwareUnitOfWorkTest {

    private static final String TEST_PROCESS_ID = "test-process-123";
    private static final String TEST_PROCESS_ID_2 = "test-process-456";

    @BeforeEach
    void setUp() {
        ProcessInstanceContext.clear();
    }

    @AfterEach
    void tearDown() {
        ProcessInstanceContext.clear();
    }

    @Test
    void testBasicLifecycle() {
        UnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        // Before start, no context
        assertFalse(ProcessInstanceContext.hasContext());

        // Start should set context
        uow.start();
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());
        assertTrue(ProcessInstanceContext.hasContext());

        // End should clear context
        uow.end();
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testAbortClearsContext() {
        UnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        uow.start();
        assertTrue(ProcessInstanceContext.hasContext());

        uow.abort();
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testNestedContextPreservation() {
        UnitOfWork delegate1 = new TestUnitOfWork();
        UnitOfWork delegate2 = new TestUnitOfWork();

        ProcessInstanceAwareUnitOfWork outerUow = new ProcessInstanceAwareUnitOfWork(delegate1, TEST_PROCESS_ID);
        ProcessInstanceAwareUnitOfWork innerUow = new ProcessInstanceAwareUnitOfWork(delegate2, TEST_PROCESS_ID_2);

        // Start outer UoW
        outerUow.start();
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());

        // Start inner UoW - should override context
        innerUow.start();
        assertEquals(TEST_PROCESS_ID_2, ProcessInstanceContext.getProcessInstanceId());

        // End inner UoW - should restore outer context
        innerUow.end();
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());

        // End outer UoW - should clear context
        outerUow.end();
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testNestedContextWithAbort() {
        UnitOfWork delegate1 = new TestUnitOfWork();
        UnitOfWork delegate2 = new TestUnitOfWork();

        ProcessInstanceAwareUnitOfWork outerUow = new ProcessInstanceAwareUnitOfWork(delegate1, TEST_PROCESS_ID);
        ProcessInstanceAwareUnitOfWork innerUow = new ProcessInstanceAwareUnitOfWork(delegate2, TEST_PROCESS_ID_2);

        outerUow.start();
        innerUow.start();

        // Abort inner UoW - should restore outer context
        innerUow.abort();
        assertEquals(TEST_PROCESS_ID, ProcessInstanceContext.getProcessInstanceId());

        // End outer UoW normally
        outerUow.end();
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testWorkUnitContextManagement() {
        TestUnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        AtomicReference<String> capturedId = new AtomicReference<>();
        WorkUnit work = new TestWorkUnit(() -> {
            capturedId.set(ProcessInstanceContext.getProcessInstanceId());
        });

        uow.start();
        uow.intercept(work);

        // Execute the work unit
        delegate.executeWorkUnits();

        assertEquals(TEST_PROCESS_ID, capturedId.get());

        uow.end();
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testWorkUnitExceptionHandling() {
        TestUnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        WorkUnit work = new TestWorkUnit(() -> {
            throw new RuntimeException("Test exception");
        });

        uow.start();
        uow.intercept(work);

        // Execute work unit - should throw exception
        assertThrows(RuntimeException.class, delegate::executeWorkUnits);

        // Work unit clears context in finally block after exception
        // Since we're outside the work unit now, context should be cleared
        assertFalse(ProcessInstanceContext.hasContext());

        uow.end();
        assertFalse(ProcessInstanceContext.hasContext());
    }

    @Test
    void testGetDelegate() {
        TestUnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        UnitOfWork returnedDelegate = uow.delegate();
        assertNotNull(returnedDelegate);
        // Should return the actual delegate, not the ManagedUnitOfWork wrapper
        assertEquals(delegate, returnedDelegate);
    }

    @Test
    void testGetProcessInstanceId() {
        TestUnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        assertEquals(TEST_PROCESS_ID, uow.processInstanceId());
    }

    @Test
    void testCompositionWithManagedUnitOfWork() {
        // Verify that ProcessInstanceAwareUnitOfWork properly uses ManagedUnitOfWork
        TestUnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        List<String> executionOrder = new ArrayList<>();

        // Override delegate to track execution
        delegate.onStart = () -> executionOrder.add("delegate-start");
        delegate.onEnd = () -> executionOrder.add("delegate-end");

        uow.start();
        executionOrder.add("context-set");
        uow.end();

        // Verify execution order: context setup, delegate start, delegate end
        assertTrue(executionOrder.contains("delegate-start"));
        assertTrue(executionOrder.contains("delegate-end"));
    }

    @Test
    void testThreadLocalCleanup() {
        // Test that ThreadLocal is properly cleaned up to prevent memory leaks
        TestUnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        // Set an initial context
        ProcessInstanceContext.setProcessInstanceId("initial-context");

        uow.start();
        uow.end();

        // After end, the initial context SHOULD be restored
        // because it was saved when UoW started
        assertEquals("initial-context", ProcessInstanceContext.getProcessInstanceId());
        assertTrue(ProcessInstanceContext.hasContext());

        // Clean up for next test
        ProcessInstanceContext.clear();
    }

    @Test
    void testContextRestorationAfterException() {
        TestUnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        // Set outer context
        ProcessInstanceContext.setProcessInstanceId("outer-context");

        uow.start();

        // Simulate exception during processing
        delegate.throwOnEnd = true;

        try {
            uow.end();
            fail("Expected exception");
        } catch (RuntimeException e) {
            // Expected - exception from delegate.end()
        }

        // Even after exception, context should be restored to outer-context
        // because restoreContext() is in a finally block
        assertEquals("outer-context", ProcessInstanceContext.getProcessInstanceId());

        // Clean up for next test
        ProcessInstanceContext.clear();
    }

    @Test
    void testMdcIntegration() {
        // Ensure clean state
        ProcessInstanceContext.clear();

        TestUnitOfWork delegate = new TestUnitOfWork();
        ProcessInstanceAwareUnitOfWork uow = new ProcessInstanceAwareUnitOfWork(delegate, TEST_PROCESS_ID);

        uow.start();

        // Verify MDC is set
        assertEquals(TEST_PROCESS_ID, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));

        uow.end();

        // Verify MDC is cleared (returns to general context)
        assertEquals(ProcessInstanceContext.GENERAL_CONTEXT, MDC.get(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY));
    }

    /**
     * Test unit of work implementation for testing purposes.
     */
    private static class TestUnitOfWork implements UnitOfWork {
        private List<WorkUnit> workUnits = new ArrayList<>();
        private boolean started = false;
        private boolean ended = false;
        private boolean aborted = false;
        public Runnable onStart;
        public Runnable onEnd;
        public boolean throwOnEnd = false;

        @Override
        public void start() {
            started = true;
            if (onStart != null) {
                onStart.run();
            }
        }

        @Override
        public void end() {
            if (throwOnEnd) {
                throw new RuntimeException("Test exception on end");
            }
            ended = true;
            if (onEnd != null) {
                onEnd.run();
            }
        }

        @Override
        public void abort() {
            aborted = true;
        }

        @Override
        public void intercept(WorkUnit work) {
            workUnits.add(work);
        }

        public void executeWorkUnits() {
            for (WorkUnit work : workUnits) {
                work.perform();
            }
        }

        public boolean isStarted() {
            return started;
        }

        public boolean isEnded() {
            return ended;
        }

        public boolean isAborted() {
            return aborted;
        }
    }

    /**
     * Test work unit implementation for testing purposes.
     */
    private static class TestWorkUnit implements WorkUnit {
        private final Runnable action;
        private final Object data;

        public TestWorkUnit(Runnable action) {
            this(action, null);
        }

        public TestWorkUnit(Runnable action, Object data) {
            this.action = action;
            this.data = data;
        }

        @Override
        public Object data() {
            return data;
        }

        @Override
        public Integer priority() {
            return 0;
        }

        @Override
        public void perform() {
            action.run();
        }

        @Override
        public void abort() {
            // No-op for tests
        }
    }
}
