/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.services.event;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.event.AbstractDataEvent;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataEventAttrBuilderTest {

    @Test
    void verifyEventSourceBeingGenerated() {
        final String processId = "the_cool_project";
        final String instanceId = UUID.randomUUID().toString();
        ProcessInstance pi = mock(ProcessInstance.class);
        when(pi.getProcessId()).thenReturn(processId);
        when(pi.getId()).thenReturn(instanceId);
        final String source = DataEventAttrBuilder.toSource(pi);
        assertThat(source).isNotBlank().contains(processId).contains(instanceId);
    }

    @Test
    void verifyEventTypeBeingGenerated() {
        final String channelName = "github";
        final String processId = "the_cool_project";
        final String type = DataEventAttrBuilder.toType(channelName, processId);
        assertThat(type).isNotBlank().contains(processId).contains(channelName).startsWith(AbstractDataEvent.TYPE_PREFIX);
    }

    @Test
    void verifyEventTypeBeingGeneratedWithProcessInstance() {
        final String channelName = "github";
        final String processId = "COOL_PROJECT";
        ProcessInstance pi = mock(ProcessInstance.class);
        when(pi.getProcessId()).thenReturn(processId);
        final String type = DataEventAttrBuilder.toType(channelName, pi);
        assertThat(type)
                .isNotBlank()
                .doesNotContain(processId)
                .contains(processId.toLowerCase())
                .contains(channelName)
                .startsWith(AbstractDataEvent.TYPE_PREFIX);
    }
}