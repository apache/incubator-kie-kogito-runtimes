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
import javax.inject.Inject;

import org.kie.kogito.persistence.rocksdb.RocksDBProcessInstancesFactory;
import org.kie.kogito.process.ProcessInstancesFactory;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RocksDbProcessInstancesFactoryProducer {

    private static final Logger logger = LoggerFactory.getLogger(RocksDbProcessInstancesFactoryProducer.class);
    private Options options;
    private RocksDBProcessInstancesFactory processInstancesFactory;
    @Inject
    RocksDbConfig config;

    @PostConstruct
    void init() throws RocksDBException {
        options = new Options();
        options.setCreateIfMissing(true);
        String dataDir = config.dataDir();
        logger.info("Opening rocksdb in directory {}", dataDir);
        processInstancesFactory = new RocksDBProcessInstancesFactory(options, dataDir);
    }

    @Produces
    ProcessInstancesFactory factory() {
        return processInstancesFactory;
    }

    @PreDestroy
    void cleanup() throws RocksDBException {
        processInstancesFactory.close();
        if (config.destroyDB()) {
            String dataDir = config.dataDir();
            logger.info("Cleaning rocksdb in directory {}", dataDir);
            RocksDB.destroyDB(dataDir, options);
        }
        options.close();
    }
}
