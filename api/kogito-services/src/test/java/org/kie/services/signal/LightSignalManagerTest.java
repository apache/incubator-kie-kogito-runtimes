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

package org.kie.services.signal;

import org.junit.jupiter.api.Test;
import org.kie.api.runtime.process.EventListener;
import org.kie.kogito.signal.SignalManagerHub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LightSignalManagerTest {

    @Test
    public void testAddNewListener() {
        LightSignalManager sm = new LightSignalManager(mock(EventListenerResolver.class), mock(SignalManagerHub.class));
        EventListener listen = mock(EventListener.class);
        String type = "completion";

        sm.addEventListener(type, listen);
        assertThat(sm.getListeners()).hasEntrySatisfying(type, s -> assertThat(s).hasSize(1));

        sm.addEventListener(type, listen);
        assertThat(sm.getListeners()).hasEntrySatisfying(type, s -> assertThat(s).hasSize(1));
    }
}
