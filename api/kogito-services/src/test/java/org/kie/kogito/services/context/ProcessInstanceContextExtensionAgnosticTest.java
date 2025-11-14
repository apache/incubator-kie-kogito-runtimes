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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.*;

class ProcessInstanceContextExtensionAgnosticTest {

    private static final String TEST_PROCESS_ID = "test-process-123";

    @BeforeEach
    void setUp() {
        ProcessInstanceContext.clear();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        ProcessInstanceContext.clear();
        ProcessInstanceContext.clearUserExtensions();
        MDC.clear();
    }

    @Test
    void coreHasNoHardcodedExtensionSpecificMethodNames() {
        Set<String> methodNames = Arrays.stream(ProcessInstanceContext.class.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        String violatingMethods = methodNames.stream()
                .filter(name -> name.toLowerCase().contains("otel"))
                .collect(Collectors.joining(", "));

        assertThat(violatingMethods)
                .as("Core ProcessInstanceContext should not contain extension-specific method names like 'preserveOtelMdcKeys' or 'restoreOtelMdcKeys'. " +
                        "Found methods: " + violatingMethods)
                .isEmpty();
    }

    @Test
    void coreHasNoHardcodedExtensionPrefixes() throws Exception {
        Method setContextFromAsyncMethod = ProcessInstanceContext.class.getDeclaredMethod("setContextFromAsync", Map.class);
        assertThat(setContextFromAsyncMethod).isNotNull();

        MDC.put("otel.transaction.id", "tx-123");
        MDC.put("otel.tracker.id", "track-456");
        MDC.put("custom.myext.value", "custom-789");
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);

        Map<String, String> contextMap = new HashMap<>();
        contextMap.put(ProcessInstanceContext.MDC_PROCESS_INSTANCE_KEY, "new-process-456");

        ProcessInstanceContext.setContextFromAsync(contextMap);

        String otelTransactionId = MDC.get("otel.transaction.id");
        String customValue = MDC.get("custom.myext.value");

        assertThat(otelTransactionId)
                .as("Core should preserve 'otel.' keys through generic mechanism, not hardcoded. " +
                        "If this test fails, it proves hardcoded 'otel.' prefix exists in core.")
                .isNull();

        assertThat(customValue)
                .as("Core should preserve 'custom.myext.' keys through generic mechanism. " +
                        "If this test fails, it proves only 'otel.' is hardcoded, not a generic solution.")
                .isNull();
    }

    @Test
    void coreSupportsGenericExtensionRegistration() {
        try {
            Method registerMethod = ProcessInstanceContext.class.getDeclaredMethod("registerContextExtension", String.class, ContextExtension.class);
            assertThat(registerMethod)
                    .as("ProcessInstanceContext should provide a generic extension registration API")
                    .isNotNull();
        } catch (NoSuchMethodException e) {
            fail("ProcessInstanceContext should provide a method like 'registerContextExtension(String extensionId, ContextExtension extension)' " +
                    "to allow extensions to register themselves without hardcoding. This test proves the API does not exist.");
        }
    }

    @Test
    void multipleExtensionsCanRegisterSimultaneously() {
        MockContextExtension otelExtension = new MockContextExtension("otel");
        MockContextExtension micrometerExtension = new MockContextExtension("micrometer");
        MockContextExtension customExtension = new MockContextExtension("custom");

        try {
            Method registerMethod = ProcessInstanceContext.class.getDeclaredMethod("registerContextExtension", String.class, ContextExtension.class);
            registerMethod.invoke(null, "otel", otelExtension);
            registerMethod.invoke(null, "micrometer", micrometerExtension);
            registerMethod.invoke(null, "custom", customExtension);

            Method getExtensionMethod = ProcessInstanceContext.class.getDeclaredMethod("getExtension", String.class);
            Object retrievedOtel = getExtensionMethod.invoke(null, "otel");
            Object retrievedMicrometer = getExtensionMethod.invoke(null, "micrometer");
            Object retrievedCustom = getExtensionMethod.invoke(null, "custom");

            assertThat(retrievedOtel).isSameAs(otelExtension);
            assertThat(retrievedMicrometer).isSameAs(micrometerExtension);
            assertThat(retrievedCustom).isSameAs(customExtension);

        } catch (NoSuchMethodException e) {
            fail("ProcessInstanceContext lacks extension registration API. Cannot test multi-extension coexistence. " +
                    "This proves the architecture does not support multiple extensions.");
        } catch (Exception e) {
            fail("Extension registration API exists but failed: " + e.getMessage());
        }
    }

    @Test
    void extensionContextPreservationDuringAsyncOperations() {
        MockContextExtension otelExtension = new MockContextExtension("otel");
        MockContextExtension customExtension = new MockContextExtension("custom");

        MDC.put("otel.transaction.id", "tx-123");
        MDC.put("otel.tracker.id", "track-456");
        MDC.put("custom.session.id", "session-789");

        try {
            Method registerMethod = ProcessInstanceContext.class.getDeclaredMethod("registerContextExtension", String.class, ContextExtension.class);
            registerMethod.invoke(null, "otel", otelExtension);
            registerMethod.invoke(null, "custom", customExtension);

            ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
            Map<String, String> contextMap = ProcessInstanceContext.copyContextForAsync();

            ProcessInstanceContext.clear();
            MDC.clear();

            ProcessInstanceContext.setContextFromAsync(contextMap);

            assertThat(MDC.get("otel.transaction.id")).isEqualTo("tx-123");
            assertThat(MDC.get("otel.tracker.id")).isEqualTo("track-456");
            assertThat(MDC.get("custom.session.id")).isEqualTo("session-789");

        } catch (NoSuchMethodException e) {
            fail("ProcessInstanceContext lacks extension API. Cannot verify extension context preservation. " +
                    "This proves extensions cannot participate in async context propagation generically.");
        } catch (Exception e) {
            fail("Extension context preservation failed: " + e.getMessage());
        }
    }

    @Test
    void extensionCanDefineCustomMdcPrefixes() {
        MockContextExtension customExtension = new MockContextExtension("custom");

        try {
            Method registerMethod = ProcessInstanceContext.class.getDeclaredMethod("registerContextExtension", String.class, ContextExtension.class);
            registerMethod.invoke(null, "custom", customExtension);

            MDC.put("custom.app.version", "1.0.0");
            MDC.put("custom.tenant.id", "tenant-123");
            MDC.put("other.unrelated", "should-not-preserve");

            ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);
            Map<String, String> contextMap = ProcessInstanceContext.copyContextForAsync();

            MDC.clear();
            ProcessInstanceContext.setContextFromAsync(contextMap);

            assertThat(MDC.get("custom.app.version"))
                    .as("Extension-defined prefix 'custom.' should be preserved")
                    .isEqualTo("1.0.0");
            assertThat(MDC.get("custom.tenant.id"))
                    .as("Extension-defined prefix 'custom.' should be preserved")
                    .isEqualTo("tenant-123");
            assertThat(MDC.get("other.unrelated"))
                    .as("Non-registered prefix should NOT be preserved")
                    .isNull();

        } catch (NoSuchMethodException e) {
            fail("ProcessInstanceContext lacks extension API. Cannot verify custom MDC prefix handling. " +
                    "This proves only 'otel.' prefix is hardcoded.");
        } catch (Exception e) {
            fail("Custom MDC prefix handling failed: " + e.getMessage());
        }
    }

    @Test
    void existingOtelFunctionalityIsPreserved() {
        MockContextExtension otelExtension = new MockContextExtension("otel");
        ProcessInstanceContext.registerContextExtension(otelExtension);

        MDC.put("otel.transaction.id", "tx-original");
        MDC.put("otel.tracker.id", "track-original");
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);

        Map<String, String> contextMap = ProcessInstanceContext.copyContextForAsync();

        ProcessInstanceContext.clear();
        MDC.clear();

        ProcessInstanceContext.setContextFromAsync(contextMap);

        assertThat(ProcessInstanceContext.getProcessInstanceId())
                .as("Process instance ID should be restored")
                .isEqualTo(TEST_PROCESS_ID);

        assertThat(MDC.get("otel.transaction.id"))
                .as("OTel transaction ID should be preserved when extension is registered")
                .isEqualTo("tx-original");

        assertThat(MDC.get("otel.tracker.id"))
                .as("OTel tracker ID should be preserved when extension is registered")
                .isEqualTo("track-original");
    }

    @Test
    void otelKeysPreservedWhenRestoringNullContext() {
        MockContextExtension otelExtension = new MockContextExtension("otel");
        ProcessInstanceContext.registerContextExtension(otelExtension);

        MDC.put("otel.transaction.id", "tx-123");
        MDC.put("otel.tracker.id", "track-456");

        ProcessInstanceContext.setContextFromAsync(null);

        assertThat(ProcessInstanceContext.getProcessInstanceId())
                .as("Process instance should be cleared to general context")
                .isEqualTo(ProcessInstanceContext.GENERAL_CONTEXT);

        assertThat(MDC.get("otel.transaction.id"))
                .as("OTel keys should be preserved when extension is registered, even with null context")
                .isEqualTo("tx-123");

        assertThat(MDC.get("otel.tracker.id"))
                .as("OTel keys should be preserved when extension is registered, even with null context")
                .isEqualTo("track-456");
    }

    @Test
    void otelKeysPreservedAcrossMultipleContextSwitches() {
        MockContextExtension otelExtension = new MockContextExtension("otel");
        ProcessInstanceContext.registerContextExtension(otelExtension);

        MDC.put("otel.transaction.id", "tx-persistent");

        ProcessInstanceContext.setProcessInstanceId("process-1");
        Map<String, String> context1 = ProcessInstanceContext.copyContextForAsync();

        ProcessInstanceContext.setProcessInstanceId("process-2");
        Map<String, String> context2 = ProcessInstanceContext.copyContextForAsync();

        ProcessInstanceContext.clear();
        MDC.clear();

        ProcessInstanceContext.setContextFromAsync(context1);
        assertThat(MDC.get("otel.transaction.id")).isEqualTo("tx-persistent");
        assertThat(ProcessInstanceContext.getProcessInstanceId()).isEqualTo("process-1");

        ProcessInstanceContext.setContextFromAsync(context2);
        assertThat(MDC.get("otel.transaction.id")).isEqualTo("tx-persistent");
        assertThat(ProcessInstanceContext.getProcessInstanceId()).isEqualTo("process-2");
    }

    @Test
    void allMdcKeysPreservedDuringCopy() {
        MDC.put("otel.transaction.id", "tx-123");
        MDC.put("app.custom.value", "custom-value");
        ProcessInstanceContext.setProcessInstanceId(TEST_PROCESS_ID);

        Map<String, String> contextMap = ProcessInstanceContext.copyContextForAsync();

        MDC.clear();
        ProcessInstanceContext.setContextFromAsync(contextMap);

        assertThat(MDC.get("otel.transaction.id"))
                .as("OTel keys should be preserved")
                .isEqualTo("tx-123");

        assertThat(MDC.get("app.custom.value"))
                .as("All MDC keys are preserved in copyContextForAsync (current behavior)")
                .isEqualTo("custom-value");
    }

    @Test
    void extensionRegistrationIsIdempotent() {
        MockContextExtension extension1 = new MockContextExtension("test");
        MockContextExtension extension2 = new MockContextExtension("test");

        try {
            Method registerMethod = ProcessInstanceContext.class.getDeclaredMethod("registerContextExtension", String.class, ContextExtension.class);
            registerMethod.invoke(null, "test", extension1);
            registerMethod.invoke(null, "test", extension2);

            Method getExtensionMethod = ProcessInstanceContext.class.getDeclaredMethod("getExtension", String.class);
            Object retrieved = getExtensionMethod.invoke(null, "test");

            assertThat(retrieved)
                    .as("Last registered extension should win (idempotent registration)")
                    .isSameAs(extension2);

        } catch (NoSuchMethodException e) {
            fail("ProcessInstanceContext lacks extension registration API");
        } catch (Exception e) {
            fail("Extension registration idempotency test failed: " + e.getMessage());
        }
    }

    @Test
    void unregisteredExtensionReturnsNull() {
        try {
            Method getExtensionMethod = ProcessInstanceContext.class.getDeclaredMethod("getExtension", String.class);
            Object retrieved = getExtensionMethod.invoke(null, "nonexistent");

            assertThat(retrieved)
                    .as("Getting non-existent extension should return null")
                    .isNull();

        } catch (NoSuchMethodException e) {
            fail("ProcessInstanceContext lacks getExtension API");
        } catch (Exception e) {
            fail("Getting unregistered extension failed: " + e.getMessage());
        }
    }

    static class MockContextExtension implements ContextExtension {
        private final String extensionId;
        private final Map<String, String> context = new HashMap<>();
        private Map<String, String> preservedKeys = new HashMap<>();

        MockContextExtension(String extensionId) {
            this.extensionId = extensionId;
        }

        @Override
        public String getExtensionId() {
            return extensionId;
        }

        @Override
        public String getMdcKeyPrefix() {
            return extensionId + ".";
        }

        @Override
        public void beforeContextRestore(Map<String, String> incomingContext) {
            preservedKeys.clear();
            if (incomingContext != null) {
                String prefix = getMdcKeyPrefix();
                incomingContext.entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith(prefix))
                        .forEach(entry -> preservedKeys.put(entry.getKey(), entry.getValue()));
            }
        }

        @Override
        public void afterContextRestore() {
            if (!preservedKeys.isEmpty()) {
                preservedKeys.forEach(MDC::put);
            }
        }

        public void putContext(String key, String value) {
            context.put(key, value);
        }

        public String getContext(String key) {
            return context.get(key);
        }

        public void clearContext() {
            context.clear();
        }

        public Map<String, String> copyContext() {
            return new HashMap<>(context);
        }

        public void restoreContext(Map<String, String> savedContext) {
            if (savedContext != null) {
                context.putAll(savedContext);
            }
        }
    }
}
