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
package org.kie.kogito.infinispan;

import java.util.ArrayList;
import java.util.List;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCacheManagerAdmin;
import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.EnumMarshaller;
import org.jbpm.workflow.core.impl.WorkflowProcessImpl;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.Model;
import org.kie.kogito.persistence.KogitoProcessInstancesFactory;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KogitoProcessInstancesFactoryTest {

    @Test
    void testCreate() {
        RemoteCacheManager cacheManager = mock(RemoteCacheManager.class);
        when(cacheManager.administration()).thenReturn(mock(RemoteCacheManagerAdmin.class));
        List<BaseMarshaller<?>> myMarshallers = new ArrayList<>();
        KogitoProcessInstancesFactory factory = new KogitoProcessInstancesFactory(cacheManager) {

        };
        Process myProcess = new MyProcessImpl();
        CacheProcessInstances instances = factory.createProcessInstances(myProcess);
        assertNotNull(instances);
    }

    private static class MyProcessImpl extends AbstractProcess {

        @Override
        public ProcessInstance createInstance(WorkflowProcessInstance wpi) {
            return null;
        }

        @Override
        public ProcessInstance createInstance(String businessKey, Model workingMemory) {
            return null;
        }

        @Override
        public ProcessInstance createReadOnlyInstance(WorkflowProcessInstance wpi) {
            return null;
        }

        @Override
        public org.kie.api.definition.process.Process process() {
            return new WorkflowProcessImpl();
        }

        @Override
        public ProcessInstance createInstance(Model workingMemory) {
            return null;
        }

    }

    List<BaseMarshaller<?>> getMarshallers() {
        List<BaseMarshaller<?>> marshallers = new ArrayList<>();
        marshallers.add(new TheEnumMarshaller());
        return marshallers;
    }

    private enum TheEnum {
        YES,
        NO
    }

    private static class TheEnumMarshaller implements EnumMarshaller<TheEnum> {

        @Override
        public TheEnum decode(int enumValue) {
            return null;
        }

        @Override
        public int encode(TheEnum theEnum) throws IllegalArgumentException {
            return 0;
        }

        @Override
        public Class<? extends TheEnum> getJavaClass() {
            return null;
        }

        @Override
        public String getTypeName() {
            return null;
        }
    }
}
