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
package org.kie.kogito.process.handler;

import java.util.Optional;

import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.kogito.Model;
import org.kie.kogito.handler.ExceptionHandler;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.services.uow.UnitOfWorkExecutor;
import org.kie.kogito.uow.UnitOfWorkManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringbootExceptionHandler implements ExceptionHandler {

    @Autowired
    UnitOfWorkManager unitOfWorkManager;

    @Autowired
    Processes processes;

    @Override
    public void handle(Exception th) {
        if (th instanceof ProcessInstanceExecutionException processInstanceExecutionException) {
            UnitOfWorkExecutor.executeInUnitOfWork(unitOfWorkManager, () -> {
                String processInstanceId = processInstanceExecutionException.getProcessInstanceId();
                String nodeInstanceId = processInstanceExecutionException.getFailedNodeId();
                Optional<Process<? extends Model>> processDefinition = processes.processByProcessInstanceId(processInstanceId);
                if (processDefinition.isEmpty()) {
                    return null;
                }

                var instance = processDefinition.get().instances().findById(processInstanceId);
                if (instance.isEmpty()) {
                    return null;
                }

                AbstractProcessInstance<? extends Model> processInstance = ((AbstractProcessInstance<? extends Model>) instance.get());
                KogitoNodeInstance nodeIntsance = processInstance.internalGetProcessInstance().getNodeInstance(nodeInstanceId);
                ((WorkflowProcessInstanceImpl) processInstance.internalGetProcessInstance()).setErrorState((org.jbpm.workflow.instance.NodeInstance) nodeIntsance, th);

                ((MutableProcessInstances) processDefinition.get().instances()).update(processInstanceId, processInstance);
                return null;
            });
        }
    }

}
