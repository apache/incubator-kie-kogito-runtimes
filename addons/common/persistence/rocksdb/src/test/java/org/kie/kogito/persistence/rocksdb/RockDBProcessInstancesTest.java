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
package org.kie.kogito.persistence.rocksdb;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;

import org.drools.io.ClassPathResource;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.bpmn2.BpmnProcess;
import org.kie.kogito.process.bpmn2.BpmnVariables;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RockDBProcessInstancesTest {

    private static final String TEST_ID = "02ac3854-46ee-42b7-8b63-5186c9889d96";

    @TempDir
    Path tempDir;
    private RocksDBProcessInstancesFactory factory;
    private static Options options;

    @BeforeAll
    static void init() {
        options = new Options().setCreateIfMissing(true);
    }

    @BeforeEach
    void setup() throws RocksDBException {
        factory = new RocksDBProcessInstancesFactory(options, tempDir.toString());
    }

    @AfterEach
    void close() {
        factory.close();
    }

    @AfterAll
    static void cleanUp() {
        options.close();
    }

    private BpmnProcess createProcess(String fileName) {
        BpmnProcess process = BpmnProcess.from(new ClassPathResource(fileName)).get(0);
        process.setProcessInstancesFactory(factory);
        process.configure();
        return process;
    }

    @Test
    public void testBasic() {

        BpmnProcess process = createProcess("BPMN2-UserTask.bpmn2");

        RocksDBProcessInstances pi = factory.createProcessInstances(process);
        assertThat(pi).isNotNull();

        WorkflowProcessInstance createPi = ((AbstractProcessInstance<?>) process.createInstance(BpmnVariables.create(Collections.singletonMap("test", "test")))).internalGetProcessInstance();
        createPi.setStartDate(new Date());

        AbstractProcessInstance<?> mockCreatePi = mock(AbstractProcessInstance.class);
        mockCreatePi.setVersion(1L);
        when(mockCreatePi.status()).thenReturn(ProcessInstance.STATE_ACTIVE);
        when(mockCreatePi.internalGetProcessInstance()).thenReturn(createPi);
        when(mockCreatePi.id()).thenReturn(TEST_ID);
        pi.create(TEST_ID, mockCreatePi);

        assertThat(pi.exists(TEST_ID)).isTrue();
        try (Stream<ProcessInstance<?>> stream = pi.stream()) {
            assertThat(stream.count()).isOne();
        }
        assertThat(pi.findById(TEST_ID)).isNotEmpty();
        assertThat(pi.findById("non_existant")).isEmpty();

        WorkflowProcessInstance updatePi = ((AbstractProcessInstance<?>) process.createInstance(BpmnVariables.create(Collections.singletonMap("test", "test")))).internalGetProcessInstance();
        updatePi.setId(TEST_ID);
        updatePi.setStartDate(new Date());
        AbstractProcessInstance<?> mockUpdatePi = mock(AbstractProcessInstance.class);
        when(mockUpdatePi.status()).thenReturn(ProcessInstance.STATE_ACTIVE);
        when(mockUpdatePi.internalGetProcessInstance()).thenReturn(updatePi);
        when(mockUpdatePi.id()).thenReturn(TEST_ID);
        pi.remove(TEST_ID);
        assertThat(pi.exists(TEST_ID)).isFalse();
        try (Stream<ProcessInstance<?>> stream = pi.stream()) {
            assertThat(stream.count()).isZero();
        }
    }
}
