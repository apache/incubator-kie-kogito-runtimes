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

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.kie.kogito.Model;
import org.kie.kogito.mongodb.marshalling.DocumentMarshallingStrategy;
import org.kie.kogito.mongodb.marshalling.DocumentProcessInstanceMarshaller;
import org.kie.kogito.mongodb.model.ProcessInstanceDocument;
import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceDuplicatedException;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.kie.kogito.mongodb.utils.DocumentConstants.DOCUMENT_ID;
import static org.kie.kogito.mongodb.utils.DocumentUtils.getCollection;
import static org.kie.kogito.process.ProcessInstanceReadMode.MUTABLE;

public class MongoDBProcessInstances<T extends Model> implements MutableProcessInstances<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBProcessInstances.class);
    private org.kie.kogito.process.Process<?> process;
    private DocumentProcessInstanceMarshaller marshaller;
    private final MongoCollection<ProcessInstanceDocument> collection;
    private final MongoClient mongoClient;

    public MongoDBProcessInstances(MongoClient mongoClient, org.kie.kogito.process.Process<?> process, String dbName) {
        this.process = process;
        this.mongoClient = mongoClient;
        collection = getCollection(mongoClient, process.id(), dbName);
        marshaller = new DocumentProcessInstanceMarshaller(new DocumentMarshallingStrategy());
    }

    @Override
    public Closeable startTransaction() {
        ClientSession clientSession = this.mongoClient.startSession();
        TransactionOptions txnOptions = TransactionOptions.builder()
                .readPreference(ReadPreference.primary())
                .readConcern(ReadConcern.MAJORITY)
                .writeConcern(WriteConcern.MAJORITY)
                .build();
        clientSession.startTransaction(txnOptions);
        return clientSession;
    }

    @Override
    public void commitTransaction(Closeable closeable) {
        ((ClientSession) closeable).commitTransaction();
    }

    @Override
    public Optional<ProcessInstance<T>> findById(String id, ProcessInstanceReadMode mode) {
        ProcessInstanceDocument piDoc = find(id, null);
        if (piDoc == null) {
            return Optional.empty();
        }
        return Optional.of(mode == MUTABLE ? marshaller.unmarshallProcessInstance(piDoc, process) : marshaller.unmarshallReadOnlyProcessInstance(piDoc, process));
    }

    @Override
    public Collection<ProcessInstance<T>> values(ProcessInstanceReadMode mode) {
        List<ProcessInstance<T>> list = new ArrayList<>();
        try (MongoCursor<ProcessInstanceDocument> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                list.add(mode == MUTABLE ? marshaller.unmarshallProcessInstance(cursor.next(), process) : marshaller.unmarshallReadOnlyProcessInstance(cursor.next(), process));
            }
        }
        return list;
    }

    @Override
    public void create(String id, ProcessInstance<T> instance, Object... options) {
        if (options.length > 0 && options[0] instanceof ClientSession) {
            updateStorage(id, instance, true, (ClientSession) options[0]);
        } else {
            updateStorage(id, instance, true, null);
        }
    }

    @Override
    public void update(String id, ProcessInstance<T> instance, Object... options) {
        if (options.length > 0 && options[0] instanceof ClientSession) {
            updateStorage(id, instance, false, (ClientSession) options[0]);
        } else {
            updateStorage(id, instance, false, null);
        }
    }

    protected void updateStorage(String id, ProcessInstance<T> instance, boolean checkDuplicates, ClientSession clientSession) {
        if (isActive(instance)) {
            ProcessInstanceDocument doc = marshaller.marshalProcessInstance(instance);
            if (checkDuplicates) {
                if (exists(id)) {
                    throw new ProcessInstanceDuplicatedException(id);
                } else {
                    if (clientSession != null) {
                        collection.insertOne(clientSession, doc);
                    } else {
                        collection.insertOne(doc);
                    }
                }
            } else {
                if (clientSession != null) {
                    collection.replaceOne(clientSession, Filters.eq(DOCUMENT_ID, id), doc);
                } else {
                    collection.replaceOne(Filters.eq(DOCUMENT_ID, id), doc);
                }
            }
        }
        reloadProcessInstance(instance, id, clientSession);
    }

    private ProcessInstanceDocument find(String id, ClientSession clientSession) {
        if (clientSession != null) {
            return collection.find(clientSession, Filters.eq(DOCUMENT_ID, id)).first();
        } else {
            return collection.find(Filters.eq(DOCUMENT_ID, id)).first();
        }
    }

    @Override
    public boolean exists(String id, Object... options) {
        if (options.length > 0 && options[0] instanceof ClientSession) {
            return find(id, (ClientSession) options[0]) != null;
        } else {
            return find(id, null) != null;
        }
    }

    @Override
    public void remove(String id, Object... options) {
        if (options.length > 0 && options[0] instanceof ClientSession) {
            collection.deleteOne((ClientSession) options[0], Filters.eq(DOCUMENT_ID, id));
        } else {
            collection.deleteOne(Filters.eq(DOCUMENT_ID, id));
        }
    }

    private void reloadProcessInstance(ProcessInstance<T> instance, String id, ClientSession clientSession) {
        ((AbstractProcessInstance<?>) instance).internalRemoveProcessInstance(() -> {
            try {
                ProcessInstanceDocument reloaded = find(id, clientSession);
                if (reloaded != null) {
                    return marshaller.unmarshallWorkflowProcessInstance(reloaded, process);
                }
            } catch (RuntimeException e) {
                LOGGER.error("Unexpected exception thrown when reloading process instance {}", instance.id(), e);
            }
            return null;
        });
    }

    @Override
    public Integer size() {
        return (int) collection.countDocuments();
    }
}
