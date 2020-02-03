/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.codegen.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.process.ProcessData;
import org.kie.kogito.codegen.AbstractCodegenTest;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.impl.Sig;
import org.kie.kogito.uow.UnitOfWork;

public class SignalEventTest extends AbstractCodegenTest {
    
    @Test
    public void testIntermediateSignalEventWithData() throws Exception {
        
        Application app = generateCode(Collections.singletonList("signalevent/IntermediateCatchEventSignal.bpmn2"), Collections.singletonList("ruletask/BusinessRuleTask.drl"));        
        assertThat(app).isNotNull();
                
        Process<? extends ProcessData> p = app.processes().processById("IntermediateCatchEvent");
        
        ProcessData m = p.createModel();
        
        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
        
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        
        List<WorkItem> workItems = processInstance.workItems();
        assertThat(workItems).hasSize(1);
        
        processInstance.completeWorkItem(workItems.get(0).getId(), null);
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        
        processInstance.send(Sig.of("MyMessage", "test"));
        
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        
        ProcessData result = (ProcessData)processInstance.variables();
        assertThat(result.toMap()).hasSize(2).containsKey("x");
        assertThat(result.toMap().get("x")).isEqualTo("test");
                
        assertThat(p.instances().values()).hasSize(0);
    }
    
    @Test
    public void testBoundarySignalEventWithData() throws Exception {
        
        Application app = generateCode(Collections.singletonList("signalevent/BoundarySignalEventOnTask.bpmn2"), Collections.singletonList("ruletask/BusinessRuleTask.drl"));        
        assertThat(app).isNotNull();
                
        Process<? extends ProcessData> p = app.processes().processById("BoundarySignalOnTask");
        
        ProcessData m = p.createModel();
        
        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
   
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        
        processInstance.send(Sig.of("MySignal", "test"));
        
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        
        ProcessData result = (ProcessData)processInstance.variables();
        assertThat(result.toMap()).hasSize(1).containsKey("x");
        assertThat(result.toMap().get("x")).isEqualTo("test");
        
        assertThat(p.instances().values()).hasSize(0);
    }
    
    @Test
    public void testBoundaryInterruptingSignalEventWithData() throws Exception {
        
        Application app = generateCode(Collections.singletonList("signalevent/BoundaryInterruptingSignalEventOnTask.bpmn2"), Collections.singletonList("ruletask/BusinessRuleTask.drl"));        
        assertThat(app).isNotNull();
                
        Process<? extends ProcessData> p = app.processes().processById("BoundarySignalOnTask");
        
        ProcessData m = p.createModel();
        
        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
   
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        
        processInstance.send(Sig.of("MySignal", "test"));
        
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        
        ProcessData result = (ProcessData)processInstance.variables();
        assertThat(result.toMap()).hasSize(1).containsKey("x");
        assertThat(result.toMap().get("x")).isEqualTo("test");
        
        assertThat(p.instances().values()).hasSize(0);
    }
    
    @Test
    public void testIntermediateSignalEventWithDataControlledByUnitOfWork() throws Exception {
        
        Application app = generateCode(Collections.singletonList("signalevent/IntermediateCatchEventSignal.bpmn2"), Collections.singletonList("ruletask/BusinessRuleTask.drl"));        
        assertThat(app).isNotNull();
        // create first unit of work
        UnitOfWork uow = app.unitOfWorkManager().newUnitOfWork();                        
        uow.start();
                
        Process<? extends ProcessData> p = app.processes().processById("IntermediateCatchEvent");
        
        ProcessData m = p.createModel();
        
        ProcessInstance<?> processInstance = p.createInstance(m);
        processInstance.start();
        
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        
        // since unit of work is not ended yet there are no instance added
        assertThat(p.instances().values()).hasSize(0);
        uow.end();
        // after the unit of work is ended process instance shows up in the list
        assertThat(p.instances().values()).hasSize(1);
        
        uow = app.unitOfWorkManager().newUnitOfWork();                        
        uow.start();
        List<WorkItem> workItems = processInstance.workItems();
        assertThat(workItems).hasSize(1);
        
        processInstance.completeWorkItem(workItems.get(0).getId(), null);
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_ACTIVE);
        
        uow.end();
        
        uow = app.unitOfWorkManager().newUnitOfWork();                        
        uow.start();
        processInstance.send(Sig.of("MyMessage", "test"));
        
        assertThat(processInstance.status()).isEqualTo(ProcessInstance.STATE_COMPLETED);
        
        ProcessData result = (ProcessData)processInstance.variables();
        assertThat(result.toMap()).hasSize(2).containsKey("x");
        assertThat(result.toMap().get("x")).isEqualTo("test");
                
        // since the unit of work is not ended yet there is still instance visible
        assertThat(p.instances().values()).hasSize(1);
        uow.end();
        // after unit of work is ended instance is gone from the list
        assertThat(p.instances().values()).hasSize(0);
    }
}
