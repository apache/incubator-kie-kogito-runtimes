/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.process.impl.marshalling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.serialization.process.MarshallerContext;
import org.kie.kogito.serialization.process.MarshallerContextName;
import org.kie.kogito.serialization.process.MarshallerReaderContext;
import org.kie.kogito.serialization.process.MarshallerWriterContext;
import org.kie.kogito.serialization.process.ObjectMarshallerStrategy;
import org.kie.kogito.serialization.process.ProcessMarshallerFactory;

public class ProcessInstanceMarshaller {

    private List<ObjectMarshallerStrategy> strats;

    private Map<String, Object> contextEntries;

    public ProcessInstanceMarshaller() {
        this(new HashMap<>());
    }

    public ProcessInstanceMarshaller(Map<String, Object> contextEntries) {
        this(contextEntries, new ObjectMarshallerStrategy[0]);
    }

    public ProcessInstanceMarshaller(Map<String, Object> contextEntries, ObjectMarshallerStrategy... strategies) {
        this.contextEntries = new HashMap<>();
        this.contextEntries.putAll(contextEntries);
        this.strats = new ArrayList<>();
        ServiceLoader<ObjectMarshallerStrategy> loader = ServiceLoader.load(ObjectMarshallerStrategy.class);

        for (ObjectMarshallerStrategy strategy : loader) {
            this.strats.add(strategy);
        }

        for (ObjectMarshallerStrategy strategy : strategies) {
            this.strats.add(strategy);
        }
        Collections.sort(this.strats);
    }

    protected void setupEnvironment(MarshallerContext env) {
        env.set(MarshallerContextName.OBJECT_MARSHALLING_STRATEGIES, strats.toArray(new ObjectMarshallerStrategy[strats.size()]));

        for (Map.Entry<String, Object> entry : contextEntries.entrySet()) {
            env.set(entry.getKey(), entry.getValue());
        }
    }

    public byte[] marshallProcessInstance(ProcessInstance<?> processInstance) {

        WorkflowProcessInstance pi = ((AbstractProcessInstance<?>) processInstance).internalGetProcessInstance();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MarshallerWriterContext context = ProcessMarshallerFactory.newWriterContext(baos);
            context.set(MarshallerContextName.MARSHALLER_PROCESS, processInstance.process());
            setupEnvironment(context);
            org.kie.kogito.serialization.process.ProcessInstanceMarshaller marshaller = ProcessMarshallerFactory.newKogitoProcessInstanceMarshaller();
            marshaller.writeProcessInstance(context, pi);
            pi.disconnect();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error while marshalling process instance", e);
        }
    }

    public WorkflowProcessInstance unmarshallWorkflowProcessInstance(byte[] data, Process<?> process) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            MarshallerReaderContext context = ProcessMarshallerFactory.newReaderContext(bais);
            context.set(MarshallerContextName.MARSHALLER_PROCESS, process);
            setupEnvironment(context);
            org.kie.kogito.serialization.process.ProcessInstanceMarshaller marshaller = ProcessMarshallerFactory.newKogitoProcessInstanceMarshaller();
            return (WorkflowProcessInstance) marshaller.readProcessInstance(context);
        } catch (Exception e) {
            throw new RuntimeException("Error while unmarshalling process instance", e);
        }
    }

    public ProcessInstance unmarshallProcessInstance(byte[] data, Process process) {
        return ((AbstractProcess) process).createInstance(unmarshallWorkflowProcessInstance(data, process));
    }

    public ProcessInstance unmarshallReadOnlyProcessInstance(byte[] data, Process process) {
        WorkflowProcessInstance wpi = unmarshallWorkflowProcessInstance(data, process);
        return ((AbstractProcess) process).createReadOnlyInstance(wpi);
    }

}
