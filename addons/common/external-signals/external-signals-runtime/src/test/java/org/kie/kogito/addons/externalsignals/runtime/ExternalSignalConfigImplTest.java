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
package org.kie.kogito.addons.externalsignals.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.kie.kogito.addons.externalsignals.ExternalSignalConfig;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalSignalConfigImplTest {

    @Test
    public void testDefaultConfiguration() {
        Map<String, String> emptyProps = new HashMap<>();

        ExternalSignalConfig config = new ExternalSignalConfigImpl(emptyProps);

        assertThat(config.getSignalTriggerMapping()).isEmpty();
        assertThat(config.getDefaultTriggerPrefix()).isEqualTo(ExternalSignalConfig.DEFAULT_TRIGGER_PREFIX);
    }

    @Test
    public void testLoadSignalMappings() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.mapping.OrderCreated", "order-events");
        props.put("kogito.external-signals.mapping.PaymentProcessed", "payment-topic");
        props.put("other.property", "ignored");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.getSignalTriggerMapping())
                .hasSize(2)
                .containsEntry("OrderCreated", "order-events")
                .containsEntry("PaymentProcessed", "payment-topic");
    }

    @Test
    public void testLoadCustomDefaultPrefix() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.default-prefix", "my-app-signal");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.getDefaultTriggerPrefix()).isEqualTo("my-app-signal");
    }

    @Test
    public void testTrimsWhitespaceFromValues() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.mapping.TestSignal", "  test-topic  ");
        props.put("kogito.external-signals.default-prefix", "  my-prefix  ");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.getSignalTriggerMapping().get("TestSignal")).isEqualTo("test-topic");
        assertThat(config.getDefaultTriggerPrefix()).isEqualTo("my-prefix");
    }

    @Test
    public void testIgnoresEmptySignalName() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.mapping.", "should-be-ignored");
        props.put("kogito.external-signals.mapping.ValidSignal", "valid-topic");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.getSignalTriggerMapping())
                .hasSize(1)
                .containsEntry("ValidSignal", "valid-topic");
    }

    @Test
    public void testIgnoresEmptyTriggerValue() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.mapping.EmptyTrigger", "");
        props.put("kogito.external-signals.mapping.WhitespaceTrigger", "   ");
        props.put("kogito.external-signals.mapping.ValidSignal", "valid-topic");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.getSignalTriggerMapping())
                .hasSize(1)
                .containsEntry("ValidSignal", "valid-topic");
    }

    @Test
    public void testGetMappedTrigger() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.mapping.OrderCreated", "order-events");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.getMappedTrigger("OrderCreated")).hasValue("order-events");
        assertThat(config.getMappedTrigger("UnmappedSignal")).isEmpty();
    }

    @Test
    public void testResolveTriggerWithMapping() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.mapping.OrderCreated", "order-events");
        props.put("kogito.external-signals.default-prefix", "my-app");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.resolveTrigger("OrderCreated")).isEqualTo("order-events");
    }

    @Test
    public void testResolveTriggerWithoutMapping() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.default-prefix", "my-app");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.resolveTrigger("UnmappedSignal")).isEqualTo("my-app-UnmappedSignal");
    }

    @Test
    public void testResolveTriggerWithDefaultPrefix() {
        Map<String, String> emptyProps = new HashMap<>();
        ExternalSignalConfig config = new ExternalSignalConfigImpl(emptyProps);

        assertThat(config.resolveTrigger("TestSignal"))
                .isEqualTo(ExternalSignalConfig.DEFAULT_TRIGGER_PREFIX + "-TestSignal");
    }

    @Test
    public void testConstructorWithProperties() {
        Properties props = new Properties();
        props.setProperty("kogito.external-signals.mapping.TestSignal", "test-topic");
        props.setProperty("kogito.external-signals.default-prefix", "test-prefix");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.getSignalTriggerMapping()).containsEntry("TestSignal", "test-topic");
        assertThat(config.getDefaultTriggerPrefix()).isEqualTo("test-prefix");
    }

    @Test
    public void testGetSignalTriggerMappingIsUnmodifiable() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.mapping.TestSignal", "test-topic");
        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        assertThat(config.getSignalTriggerMapping())
                .containsEntry("TestSignal", "test-topic");

        // Verify it's unmodifiable (would throw UnsupportedOperationException)
        assertThat(config.getSignalTriggerMapping()).isUnmodifiable();
    }

    @Test
    public void testToString() {
        Map<String, String> props = new HashMap<>();
        props.put("kogito.external-signals.mapping.Signal1", "topic1");
        props.put("kogito.external-signals.mapping.Signal2", "topic2");
        props.put("kogito.external-signals.default-prefix", "test-prefix");

        ExternalSignalConfig config = new ExternalSignalConfigImpl(props);

        String toString = config.toString();

        assertThat(toString)
                .contains("ExternalSignalConfigImpl")
                .contains("mappings=2")
                .contains("test-prefix");
    }
}
