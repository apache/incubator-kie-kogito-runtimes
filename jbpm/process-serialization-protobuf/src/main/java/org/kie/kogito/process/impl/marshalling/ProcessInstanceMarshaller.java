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

import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.serialization.process.MarshallerReaderContext;
import org.kie.kogito.serialization.process.MarshallerWriterContext;
import org.kie.kogito.serialization.process.ProcessMarshallerFactory;
import org.kie.kogito.serialization.protobuf.process.util.ClassObjectMarshallingStrategyAcceptor;
import org.kie.kogito.serialization.protobuf.process.util.KogitoSerializablePlaceholderResolverStrategy;
import org.kie.kogito.serialization.protobuf.process.util.SerializablePlaceholderResolverStrategy;

public class ProcessInstanceMarshaller {

    private Environment env = new org.kie.kogito.serialization.protobuf.process.util.EnvironmentImpl();

    public ProcessInstanceMarshaller(ObjectMarshallingStrategy... strategies) {
        ObjectMarshallingStrategy[] strats = null;
        if (strategies == null) {
            strats = new ObjectMarshallingStrategy[] { new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT) };
        } else {
            strats = new ObjectMarshallingStrategy[strategies.length + 1];
            int i = 0;
            for (ObjectMarshallingStrategy strategy : strategies) {
                strats[i] = strategy;
                i++;
            }
            strats[i] = new KogitoSerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT);
        }

        env.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, strats);
    }

    public byte[] marshallProcessInstance(ProcessInstance<?> processInstance) {

        WorkflowProcessInstance pi = ((AbstractProcessInstance<?>) processInstance).internalGetProcessInstance();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MarshallerWriterContext context = ProcessMarshallerFactory.newWriterContext(baos, this.env);
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
            MarshallerReaderContext context = ProcessMarshallerFactory.newReaderContext(bais, this.env);
            org.kie.kogito.serialization.process.ProcessInstanceMarshaller marshaller = ProcessMarshallerFactory.newKogitoProcessInstanceMarshaller();
            WorkflowProcessInstance pi = (WorkflowProcessInstance) marshaller.readProcessInstance(context);
            return pi;
        } catch (Exception e) {
            throw new RuntimeException("Error while unmarshalling process instance", e);
        }
    }

    public ProcessInstance unmarshallProcessInstance(byte[] data, Process process) {
        return ((AbstractProcess) process).createInstance(unmarshallWorkflowProcessInstance(data, process));
    }

    public ProcessInstance unmarshallReadOnlyProcessInstance(byte[] data, Process process) {
        return ((AbstractProcess) process).createReadOnlyInstance(unmarshallWorkflowProcessInstance(data, process));
    }
}
