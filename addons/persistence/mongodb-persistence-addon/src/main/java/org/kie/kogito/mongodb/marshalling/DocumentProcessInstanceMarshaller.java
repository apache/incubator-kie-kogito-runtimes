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
package org.kie.kogito.mongodb.marshalling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.bson.Document;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.kogito.mongodb.model.ProcessInstanceDocument;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.serialization.process.MarshallerReaderContext;
import org.kie.kogito.serialization.process.MarshallerWriterContext;
import org.kie.kogito.serialization.process.ProcessInstanceMarshaller;
import org.kie.kogito.serialization.process.ProcessMarshallerFactory;
import org.kie.kogito.serialization.protobuf.process.util.ClassObjectMarshallingStrategyAcceptor;
import org.kie.kogito.serialization.protobuf.process.util.EnvironmentImpl;
import org.kie.kogito.serialization.protobuf.process.util.SerializablePlaceholderResolverStrategy;

import static org.kie.kogito.mongodb.utils.DocumentConstants.DOCUMENT_MARSHALLING_ERROR_MSG;
import static org.kie.kogito.mongodb.utils.DocumentConstants.DOCUMENT_UNMARSHALLING_ERROR_MSG;

public class DocumentProcessInstanceMarshaller {

    private Environment env = new EnvironmentImpl();

    public DocumentProcessInstanceMarshaller(ObjectMarshallingStrategy... strategies) {
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
            strats[i] = new SerializablePlaceholderResolverStrategy(ClassObjectMarshallingStrategyAcceptor.DEFAULT);
        }
        env.set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, strats);
    }

    public ProcessInstanceDocument marshalProcessInstance(ProcessInstance<?> processInstance) {
        try {
            WorkflowProcessInstance pi = ((AbstractProcessInstance<?>) processInstance).internalGetProcessInstance();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                env.set(ProcessMarshallerFactory.FORMAT, "json");
                MarshallerWriterContext context = ProcessMarshallerFactory.newWriterContext(baos, env);
                ProcessInstanceMarshaller marshaller = ProcessMarshallerFactory.newKogitoProcessInstanceMarshaller();
                marshaller.writeProcessInstance(context, pi);
                pi.disconnect();

                ProcessInstanceDocument document = new ProcessInstanceDocument();
                document.setId(processInstance.id());
                document.setProcessInstance(Document.parse(new String(baos.toByteArray())));
                return document;
            }
        } catch (Exception e) {
            throw new DocumentMarshallingException(processInstance.id(), e, DOCUMENT_MARSHALLING_ERROR_MSG);
        }
    }

    public WorkflowProcessInstance unmarshallWorkflowProcessInstance(ProcessInstanceDocument doc, Process<?> process) {

        try (ByteArrayInputStream bais = new ByteArrayInputStream(getDummyByteArray())) {
            MarshallerReaderContext context = ProcessMarshallerFactory.newReaderContext(bais, env);
            ProcessInstanceMarshaller marshaller = ProcessMarshallerFactory.newKogitoProcessInstanceMarshaller();
            return (WorkflowProcessInstance) marshaller.readProcessInstance(context);
        } catch (Exception e) {
            throw new DocumentUnmarshallingException(process.id(), e, DOCUMENT_UNMARSHALLING_ERROR_MSG);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ProcessInstance<T> unmarshallProcessInstance(ProcessInstanceDocument doc, Process<?> process) {
        return (ProcessInstance<T>) ((AbstractProcess<?>) process).createInstance(unmarshallWorkflowProcessInstance(doc, process));
    }

    @SuppressWarnings("unchecked")
    public <T> ProcessInstance<T> unmarshallReadOnlyProcessInstance(ProcessInstanceDocument doc, Process<?> process) {
        return (ProcessInstance<T>) ((AbstractProcess<?>) process).createReadOnlyInstance(unmarshallWorkflowProcessInstance(doc, process));
    }

    //This is to get dummy byte arrays to create context using existing marshaling framework
    private byte[] getDummyByteArray() {
        String dummy = "";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeUTF(dummy);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new DocumentMarshallingException(e);
        }
    }
}
