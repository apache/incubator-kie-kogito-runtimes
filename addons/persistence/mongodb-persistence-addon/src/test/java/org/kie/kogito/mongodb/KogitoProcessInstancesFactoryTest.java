/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.mongodb;

import org.jbpm.workflow.core.impl.WorkflowProcessImpl;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.Model;
import org.kie.kogito.persistence.KogitoProcessInstancesFactory;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class KogitoProcessInstancesFactoryTest extends TestHelper {

    @Test
    void test() {
        KogitoProcessInstancesFactory factory = new KogitoProcessInstancesFactory(getMongoClient()) {

            @Override
            public String dbName() {
                return DB_NAME;
            }
        };
        Process<?> process = new TestProcessImpl();
        MongoDBProcessInstances<?> instance = factory.createProcessInstances(process);
        assertNotNull(instance);
    }

    private static class TestProcessImpl extends AbstractProcess<Model> {

        @Override
        public String id() {
            return PROCESS_NAME;
        }

        @Override
        public String name() {
            return PROCESS_NAME;
        }

        @Override
        public ProcessInstance<Model> createInstance(WorkflowProcessInstance wpi) {
            return null;
        }

        @Override
        public ProcessInstance<Model> createReadOnlyInstance(WorkflowProcessInstance wpi) {
            return null;
        }

        @Override
        public org.kie.api.definition.process.Process process() {
            return new WorkflowProcessImpl();
        }

        @Override
        public ProcessInstance<Model> createInstance(Model workingMemory) {
            return null;
        }
    }
}
