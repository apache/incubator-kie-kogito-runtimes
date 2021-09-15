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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

    private static final String VERSION = "version";

    private static final String PAYLOAD = "payload";

    private static final List<String> SUPPORTED_DBS = List.of("PostgreSQL", "Oracle");

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCProcessInstances.class);

    private final Process<?> process;
    private final ProcessInstanceMarshallerService marshaller;
    private final boolean autoDDL;
    private final DataSource dataSource;
    private final boolean lock;

    private static final String FIND_ALL = "SELECT payload FROM process_instances WHERE process_id = ?";
    private static final String FIND_BY_ID = "SELECT payload, version FROM process_instances WHERE id = ?";
    private static final String INSERT = "INSERT INTO process_instances (id, payload, process_id, version) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE process_instances SET payload = ? WHERE id = ?";
    private static final String UPDATE_WITH_LOCK = "UPDATE process_instances SET payload = ?, version = ? WHERE id = ? and version = ?";
    private static final String DELETE = "DELETE FROM process_instances WHERE id = ?";
    private static final String COUNT = "SELECT COUNT(id) FROM process_instances WHERE process_id = ?";

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
            final String dbType = metaData.getDatabaseProductName();
            if (!SUPPORTED_DBS.contains(dbType)) {
                throw new Exception("Database (" + dbType + ")" + "not suported");
            }
            final String[] types = { "TABLE" };
            ResultSet tables = metaData.getTables(null, null, "%process_instances%", types);
            boolean exist = false;
            while (tables.next()) {
                LOGGER.debug("Found process_instance table");
                exist = true;
            }

            if (!exist) {
                LOGGER.info("dynamically creating process_instances table");
                createTable(connection, dbType);
            }

        } catch (Exception e) {
            //not break the execution flow in case of any missing permission for db application user, for instance.
            LOGGER.error("Error creating process_instances table, the database should be configured properly before " +
                    "starting the application", e);
        }
    }

    private void createTable(final Connection connection, final String dbType) {
        try {
            final String query = getQueryFromFile(dbType, "create_tables");
            LOGGER.error(query);
            PreparedStatement prepareStatement = connection.prepareStatement(query);
            prepareStatement.execute();
            LOGGER.info("DDL successfully done for ProcessInstance");
        } catch (SQLException e1) {
            LOGGER.error("Error creating process_instances table", e1);
        }
    }

    private String getQueryFromFile(final String dbType, final String scriptName) {
        final String fileName = String.format("sql/%s_%s.sql", scriptName, dbType);
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                throw new Exception();
            }
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            return new String(buffer);
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
            insertInternal(UUID.fromString(id), marshaller.marshallProcessInstance(instance));
        }
        disconnect(instance);
    }

    private void insertInternal(UUID id, byte[] payload) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setObject(1, id);
            statement.setBytes(2, payload);
            statement.setString(3, process.id());
            statement.setLong(4, 1L);
            statement.executeUpdate();
        } catch (Exception e) {
            throw uncheckedException(e, "Error inserting process instance %s", id);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(String id, ProcessInstance instance) {
        if (isActive(instance)) {
            if (lock) {
                boolean isUpdated = updateWithLock(UUID.fromString(id), marshaller.marshallProcessInstance(instance), instance.version());
                if (!isUpdated) {
                    throw uncheckedException(null, "The document with ID: %s was updated or deleted by other request.", id);
                }
            } else {
                updateInternal(UUID.fromString(id), marshaller.marshallProcessInstance(instance));
            }
        }
        disconnect(instance);
    }

    private void updateInternal(UUID id, byte[] payload) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setBytes(1, payload);
            statement.setObject(2, id);
            statement.executeUpdate();
        } catch (Exception e) {
            throw uncheckedException(e, "Error updating process instance %s", id);
        }
    }

    private boolean updateWithLock(UUID id, byte[] payload, long version) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_WITH_LOCK)) {
            statement.setBytes(1, payload);
            statement.setLong(2, version + 1);
            statement.setObject(3, id);
            statement.setLong(4, version);
            int count = statement.executeUpdate();
            return count == 1;
        } catch (Exception e) {
            throw uncheckedException(e, "Error updating process instance %s", id);
        }
    }

    @Override
    public void remove(String id) {
        boolean isDeleted = deleteInternal(UUID.fromString(id));
        if (lock && !isDeleted) {
            throw uncheckedException(null, "The document with ID: %s was deleted by other request.", id);
        }
    }

    private boolean deleteInternal(UUID id) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setObject(1, id);
            int count = statement.executeUpdate();
            return count == 1;
        } catch (Exception e) {
            throw uncheckedException(e, "Error deleting process instance %s", id);
        }

    }

    @Override
    public Optional<ProcessInstance> findById(String id, ProcessInstanceReadMode mode) {
        ProcessInstance<?> instance = null;
        Map<String, Object> map = findByIdInternal(UUID.fromString(id));
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
        return findAllInternal().stream().map(b -> mode == MUTABLE ? marshaller.unmarshallProcessInstance(b, process) : marshaller.unmarshallReadOnlyProcessInstance(b, process))
                .collect(Collectors.toList());
    }

    private Map<String, Object> findByIdInternal(UUID id) {
        Map<String, Object> result = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Optional<byte[]> b = Optional.ofNullable(resultSet.getBytes(PAYLOAD));
                    if (b.isPresent()) {
                        result.put(PAYLOAD, b.get());
                    }
                    result.put(VERSION, resultSet.getLong(VERSION));
                    return result;
                }
            }
        } catch (Exception e) {
            throw uncheckedException(e, "Error finding process instance %s", id);
        }
        return result;
    }

    private List<byte[]> findAllInternal() {
        List<byte[]> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            statement.setString(1, process.id());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getBytes(PAYLOAD));
                }
            }
            return result;
        } catch (Exception e) {
            throw uncheckedException(e, "Error finding all process instances, for processId %s", process.id());
        }
    }

    @Override
    public Integer size() {
        return countInternal().intValue();
    }

    @Override
    public boolean lock() {
        return this.lock;
    }

    private Long countInternal() {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(COUNT)) {
            statement.setString(1, process.id());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("count");
                }
            }
        } catch (Exception e) {
            throw uncheckedException(e, "Error counting process instances, for processId %s", process.id());
        }
        return 0l;
    }

    private void disconnect(ProcessInstance instance) {
        Supplier<byte[]> supplier = () -> {
            Map<String, Object> map = findByIdInternal(UUID.fromString(instance.id()));
            ((AbstractProcessInstance<?>) instance).setVersion((Long) map.get(VERSION));
            return (byte[]) map.get(PAYLOAD);
        };
        ((AbstractProcessInstance<?>) instance).internalRemoveProcessInstance(marshaller.createdReloadFunction(supplier));
    }

    private RuntimeException uncheckedException(Exception ex, String message, Object... param) {
        return new RuntimeException(String.format(message, param), ex);
    }
}