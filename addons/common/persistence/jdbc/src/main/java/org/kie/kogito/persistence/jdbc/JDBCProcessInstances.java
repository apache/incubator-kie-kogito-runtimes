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
package org.kie.kogito.persistence.jdbc;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.serialization.process.ProcessInstanceMarshallerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.kogito.process.ProcessInstanceReadMode.MUTABLE;

public class JDBCProcessInstances implements MutableProcessInstances {

    static final String PAYLOAD = "payload";
    static final String VERSION = "version";

    enum DatabaseType {
        POSTGRES("PostgreSQL", "process_instances"),
        ORACLE("Oracle", "PROCESS_INSTANCES");

        private final String dbIdentifier;
        private final String tableNamePattern;

        DatabaseType(final String dbIdentifier, final String tableNamePattern) {
            this.dbIdentifier = dbIdentifier;
            this.tableNamePattern = tableNamePattern;
        }

        public static DatabaseType create(final String dbIdentifier) {
            if ("Oracle".equals(dbIdentifier)) {
                return ORACLE;
            } else if ("PostgreSQL".equals(dbIdentifier)) {
                return POSTGRES;
            }
            return null;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCProcessInstances.class);

    private final Process<?> process;
    private final ProcessInstanceMarshallerService marshaller;
    private final boolean autoDDL;
    private final DataSource dataSource;
    private final boolean lock;

    private Repository repository;

    public JDBCProcessInstances(Process<?> process, DataSource dataSource, boolean autoDDL, boolean lock) {
        this.dataSource = dataSource;
        this.process = process;
        this.autoDDL = autoDDL;
        this.lock = lock;
        this.marshaller = ProcessInstanceMarshallerService.newBuilder().withDefaultObjectMarshallerStrategies().build();
        init();
    }

    private void init() {
        if (!autoDDL) {
            LOGGER.debug("Auto DDL is disabled, do not running initializer scripts");
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            final DatabaseMetaData metaData = connection.getMetaData();
            final String dbProductName = metaData.getDatabaseProductName();
            DatabaseType databaseType = DatabaseType.create(dbProductName);
            if (databaseType == null) {
                throw new Exception("Database (" + dbProductName + ") not suported");
            }
            switch (databaseType) {
                case ORACLE:
                    repository = new OracleRepository();
                    break;
                case POSTGRES:
                    repository = new PostgresRepository();
                    break;
            }

            final String[] types = { "TABLE" };
            ResultSet tables = metaData.getTables(null, null, databaseType.tableNamePattern, types);
            boolean exist = false;
            while (tables.next()) {
                LOGGER.debug("Found process_instance table");
                exist = true;
            }

            if (!exist) {
                LOGGER.info("dynamically creating process_instances table");
                createTable(connection, databaseType);
            }

        } catch (Exception e) {
            //not break the execution flow in case of any missing permission for db application user, for instance.
            LOGGER.error("Error creating process_instances table, the database should be configured properly before " +
                    "starting the application", e);
        }
    }

    private void createTable(final Connection connection, final DatabaseType dbType) {
        try {
            final List<String> statements = getQueryFromFile(dbType.dbIdentifier, "create_tables");
            for (String s : statements) {
                PreparedStatement prepareStatement = connection.prepareStatement(s.trim());
                prepareStatement.execute();
            }
            LOGGER.info("DDL successfully done for ProcessInstance");
        } catch (SQLException e1) {
            LOGGER.error("Error creating process_instances table", e1);
        }
    }

    private List<String> getQueryFromFile(final String dbType, final String scriptName) {
        final String fileName = String.format("sql/%s_%s.sql", scriptName, dbType);
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                throw new Exception();
            }
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            String[] statments = new String(buffer).split(";");
            return List.of(statments);
        } catch (Exception e) {
            throw uncheckedException(e, "Error reading query script file %s", fileName);
        }
    }

    @Override
    public boolean exists(String id) {
        return findById(id).isPresent();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void create(String id, ProcessInstance instance) {
        if (isActive(instance)) {
            repository.insertInternal(dataSource, process.id(), UUID.fromString(id), marshaller.marshallProcessInstance(instance));
        }
        disconnect(instance);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(String id, ProcessInstance instance) {
        if (isActive(instance)) {
            if (lock) {
                boolean isUpdated = repository.updateWithLock(dataSource, UUID.fromString(id), marshaller.marshallProcessInstance(instance), instance.version());
                if (!isUpdated) {
                    throw uncheckedException(null, "The document with ID: %s was updated or deleted by other request.", id);
                }
            } else {
                repository.updateInternal(dataSource, UUID.fromString(id), marshaller.marshallProcessInstance(instance));
            }
        }
        disconnect(instance);
    }

    @Override
    public void remove(String id) {
        boolean isDeleted = repository.deleteInternal(dataSource, UUID.fromString(id));
        if (lock && !isDeleted) {
            throw uncheckedException(null, "The document with ID: %s was deleted by other request.", id);
        }
    }

    @Override
    public Optional<ProcessInstance> findById(String id, ProcessInstanceReadMode mode) {
        ProcessInstance<?> instance = null;
        Map<String, Object> map = repository.findByIdInternal(dataSource, UUID.fromString(id));
        if (map.containsKey(PAYLOAD)) {
            byte[] b = (byte[]) map.get(PAYLOAD);
            instance = mode == MUTABLE ? marshaller.unmarshallProcessInstance(b, process)
                    : marshaller.unmarshallReadOnlyProcessInstance(b, process);
            ((AbstractProcessInstance<?>) instance).setVersion((Long) map.get(VERSION));
            return Optional.of(instance);
        }
        return Optional.empty();
    }

    @Override
    public Collection<ProcessInstance> values(ProcessInstanceReadMode mode) {
        return repository.findAllInternal(dataSource, process.id()).stream()
                .map(b -> mode == MUTABLE ? marshaller.unmarshallProcessInstance(b, process) : marshaller.unmarshallReadOnlyProcessInstance(b, process))
                .collect(Collectors.toList());
    }

    @Override
    public Integer size() {
        return repository.countInternal(dataSource, process.id()).intValue();
    }

    @Override
    public boolean lock() {
        return this.lock;
    }

    private void disconnect(ProcessInstance instance) {
        Supplier<byte[]> supplier = () -> {
            Map<String, Object> map = repository.findByIdInternal(dataSource, UUID.fromString(instance.id()));
            ((AbstractProcessInstance<?>) instance).setVersion((Long) map.get(VERSION));
            return (byte[]) map.get(PAYLOAD);
        };
        ((AbstractProcessInstance<?>) instance).internalRemoveProcessInstance(marshaller.createdReloadFunction(supplier));
    }

    private RuntimeException uncheckedException(Exception ex, String message, Object... param) {
        return new RuntimeException(String.format(message, param), ex);
    }
}
