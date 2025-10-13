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
package org.kie.kogito.process.migration;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jbpm.ruleflow.core.Metadata;
import org.jbpm.ruleflow.core.WorkflowElementIdentifierFactory;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.WorkflowModelValidator;
import org.jbpm.workflow.core.WorkflowProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.Application;
import org.kie.kogito.process.ProcessError;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstances;
import org.kie.kogito.process.Processes;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.ruleflow.core.Metadata.UNIQUE_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BaseProcessInstanceManagementResourceTest {

    public static final String PROCESS_ID = "processId";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String NODE_ID_ERROR = "processInstanceIdError";
    public static final String NODE_ID = "nodeId";
    public static final String NODE_UNIQUE_ID = "nodeUniqueId";
    private BaseProcessInstanceMigrationResource tested;

    @Mock
    private Processes processes;

    @Mock
    private Application application;

    @Mock
    private ProcessInstance processInstance;

    @Mock
    private ProcessError error;

    @Mock
    private ProcessInstances instances;

    @Mock
    private AbstractProcess process;

    @Mock
    private WorkflowProcess workflowProcess;

    @Mock
    private WorkflowModelValidator workflowValidator;

    @Mock
    private Node node;

    @Mock
    private ProcessMigrationSpec processMigrationSpec;

    @Mock
    private Object variables;

    @BeforeEach
    void setUp() {
        lenient().when(node.getId()).thenReturn(WorkflowElementIdentifierFactory.fromExternalFormat("one"));
        lenient().when(node.getName()).thenReturn("node");
        lenient().when(node.getUniqueId()).thenReturn(NODE_ID);
        lenient().when(node.getMetaData()).thenReturn(singletonMap(UNIQUE_ID, NODE_UNIQUE_ID));
        lenient().when(workflowProcess.getNodesRecursively()).thenReturn(singletonList(node));
        lenient().when(process.get()).thenReturn(workflowProcess);
        lenient().when(process.id()).thenReturn(PROCESS_ID);
        lenient().when(processes.processIds()).thenReturn(Arrays.asList(PROCESS_ID));
        lenient().when(processes.processById(anyString())).thenReturn(process);
        lenient().when(process.instances()).thenReturn(instances);
        lenient().when(process.name()).thenReturn("Javierito");
        lenient().when(process.version()).thenReturn("1_0");
        lenient().when(process.type()).thenReturn("BPMN");

        lenient().when(workflowProcess.getMetaData()).thenReturn(Map.of(Metadata.DESCRIPTION, "cool", Metadata.ANNOTATIONS, Arrays.asList("good")));
        lenient().when(workflowValidator.schema(JsonNode.class)).thenReturn(Optional.of(NullNode.instance));
        lenient().when(workflowProcess.getInputValidator()).thenReturn(Optional.of(workflowValidator));
        lenient().when(workflowProcess.getOutputValidator()).thenReturn(Optional.empty());
        lenient().when(instances.findById(anyString())).thenReturn(Optional.of(processInstance));
        lenient().when(processInstance.error()).thenReturn(Optional.of(error));
        lenient().when(processInstance.variables()).thenReturn(variables);
        lenient().when(processInstance.id()).thenReturn(PROCESS_INSTANCE_ID);
        lenient().when(processInstance.status()).thenReturn(ProcessInstance.STATE_ERROR);
        lenient().when(processInstance.timers()).thenReturn(List.of());
        lenient().when(error.failedNodeId()).thenReturn(NODE_ID_ERROR);
        lenient().when(error.errorMessage()).thenReturn("Test error message");
        lenient().when(application.unitOfWorkManager()).thenReturn(new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory()));

        tested = spy(new BaseProcessInstanceMigrationResource(processes, application) {

            @Override
            public Object migrateAllInstances(String processId, ProcessMigrationSpec migrationSpec) {
                return null;
            }

            @Override
            public Object migrateInstance(String processId, String processInstanceId, ProcessMigrationSpec migrationSpec) {
                return null;
            }

            @Override
            protected Object buildOkResponse(Object body) {
                return body;
            }

            @Override
            protected Object badRequestResponse(String message) {
                return message;
            }

            @Override
            protected Object notFoundResponse(String message) {
                return message;
            }
        });
    }

    @Test
    void testDoMigrateInstance() {
        Object response = tested.doMigrateInstance(PROCESS_ID, processMigrationSpec, PROCESS_INSTANCE_ID);
        verify(processes).processById(PROCESS_ID);
        verify(tested).buildOkResponse(any());
        assertThat(response).isInstanceOf(Map.class);
        Map responseMap = (Map) response;
        assertThat(responseMap.get("message")).isEqualTo("processInstanceId instance migrated");
        assertThat(responseMap.get("processInstanceId")).isEqualTo(PROCESS_INSTANCE_ID);
    }

    @Test
    void testDoMigrateAllInstances() {
        Object response = tested.doMigrateAllInstances(PROCESS_ID, processMigrationSpec);
        verify(processes).processById(PROCESS_ID);
        verify(tested).buildOkResponse(any());
        assertThat(response).isInstanceOf(Map.class);
        Map responseMap = (Map) response;
        assertThat(responseMap.get("message")).isEqualTo("All instances migrated");
        assertThat(responseMap.get("numberOfProcessInstanceMigrated")).isEqualTo(0L);
    }
}
