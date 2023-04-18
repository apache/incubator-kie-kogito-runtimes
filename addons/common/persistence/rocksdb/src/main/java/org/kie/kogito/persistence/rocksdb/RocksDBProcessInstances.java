/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

import java.io.Closeable;
import java.util.Optional;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.serialization.process.ProcessInstanceMarshallerService;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class RocksDBProcessInstances<T> implements MutableProcessInstances<T> {

    private final Process<T> process;
    private ProcessInstanceMarshallerService marshaller;
    private final Options options;
    private final String dbLocation;

    public RocksDBProcessInstances(Process<T> process, Options options, String dbLocation) {
        this.process = process;
        marshaller = ProcessInstanceMarshallerService.newBuilder().withDefaultObjectMarshallerStrategies().build();
        this.options = options;
        this.dbLocation = dbLocation;
    }

    @FunctionalInterface
    private static interface DBFunction<T> {
        T apply(RocksDB db) throws RocksDBException;
    }

    @FunctionalInterface
    private static interface DBConsumer {
        void accept(RocksDB db) throws RocksDBException;
    }

    private class RockSplitIterator extends AbstractSpliterator<ProcessInstance<T>> implements Closeable {

        private final RocksIterator iterator;

        protected RockSplitIterator(RocksIterator iterator) {
            super(Integer.MAX_VALUE, 0);
            this.iterator = iterator;
            iterator.seekToFirst();
        }

        @Override
        public boolean tryAdvance(Consumer<? super ProcessInstance<T>> action) {
            boolean hasNext = iterator.isValid();
            if (hasNext) {
                action.accept(unmarshall(iterator.value()));
                iterator.next();
                hasNext = iterator.isValid();
            }
            return hasNext;
        }

        @Override
        public void close() {
            iterator.close();
        }
    }

    @Override
    public Optional<ProcessInstance<T>> findById(String id, ProcessInstanceReadMode mode) {
        return executeDBFunction(db -> {
            byte[] data = db.get(id.getBytes());
            return data == null ? Optional.empty() : Optional.of(unmarshall(data));
        });
    }

    @Override
    public Stream<ProcessInstance<T>> stream(ProcessInstanceReadMode mode) {
        return executeDBFunction(db -> {
            RocksDBProcessInstances<T>.RockSplitIterator iterator = new RockSplitIterator(db.newIterator());
            return StreamSupport.stream(iterator, false).onClose(iterator::close);
        });
    }

    @Override
    public boolean exists(String id) {
        return executeDBFunction(db -> db.get(id.getBytes()) != null);
    }

    @Override
    public void create(String id, ProcessInstance<T> instance) {
        update(id, instance);
    }

    @Override
    public void update(String id, ProcessInstance<T> instance) {
        executeDBOperation(db -> db.put(id.getBytes(), marshaller.marshallProcessInstance(instance)));
    }

    @Override
    public void remove(String id) {
        executeDBOperation(db -> db.delete(id.getBytes()));
    }

    private <R> R executeDBFunction(DBFunction<R> function) {
        try (final RocksDB db = RocksDB.open(options, dbLocation)) {
            return function.apply(db);
        } catch (RocksDBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void executeDBOperation(DBConsumer function) {
        try (final RocksDB db = RocksDB.open(options, dbLocation)) {
            function.accept(db);
        } catch (RocksDBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private ProcessInstance<T> unmarshall(byte[] data) {
        return (ProcessInstance<T>) marshaller.unmarshallProcessInstance(data, process);
    }
}
