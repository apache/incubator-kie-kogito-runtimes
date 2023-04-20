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
package org.kie.kogito.persistence.quarkus.rocksdb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.persistence.rocksdb.RocksDBProcessInstancesFactory;
import org.kie.kogito.process.ProcessInstancesFactory;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;

@ApplicationScoped
public class RocksDbProcessInstancesFactoryProducer {

    private Options options;
    private RocksDBProcessInstancesFactory processInstancesFactory;
    @ConfigProperty(name = "quarkus.kogito.persistence.rocksdb.data.dir", defaultValue = "rockdbtemp")
    String dbLocation;

    @PostConstruct
    void init() throws RocksDBException {
        options = new Options().setCreateIfMissing(true);
        processInstancesFactory = new RocksDBProcessInstancesFactory(options, dbLocation);
    }

    @Produces
    ProcessInstancesFactory factory() {
        return processInstancesFactory;
    }

    @PreDestroy
    void cleanup() {
        processInstancesFactory.close();
        options.close();
    }
}
