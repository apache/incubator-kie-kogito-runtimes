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
package org.kie.kogito.monitoring.core.common.process;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcessInstance;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsProcessEventListenerTest {

    MeterRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = new SimpleMeterRegistry();
    }

    @Test
    public void testGaugeRunningProcesses() {
        // Arrange
        MetricsProcessEventListener eventListener = new MetricsProcessEventListener("myId", KogitoGAV.EMPTY_GAV, registry);
        ProcessInstance processInstanceMock = mock(KogitoWorkflowProcessInstance.class);
        when(processInstanceMock.getProcessId()).thenReturn("myProcessId");

        ProcessStartedEvent processStartedEvent = mock(ProcessStartedEvent.class);
        when(processStartedEvent.getProcessInstance()).thenReturn(processInstanceMock);

        // Act
        eventListener.afterProcessStarted(processStartedEvent);
        eventListener.afterProcessStarted(processStartedEvent);

        // Assert
        assertThat(registry.find("kogito_process_instance_running_total")
                .gauge()
                .value()).isEqualTo(2);
    }
}
