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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.kogito.persistence.jdbc.JDBCProcessInstances.PAYLOAD;
import static org.kie.kogito.persistence.jdbc.JDBCProcessInstances.VERSION;

public class GenericRepository extends Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericRepository.class);

    private enum DatabaseType {
        ANSI("ansi", "process_instances"),
        ORACLE("Oracle", "PROCESS_INSTANCES"),
        POSTGRES("PostgreSQL", "process_instances");

        private final String dbIdentifier;
        private final String tableNamePattern;

        DatabaseType(final String dbIdentifier, final String tableNamePattern) {
            this.dbIdentifier = dbIdentifier;
            this.tableNamePattern = tableNamePattern;
        }

        String getDbIdentifier() {
            return this.dbIdentifier;
        }

        public static DatabaseType create(final String dbIdentifier) {
            if (ORACLE.getDbIdentifier().equals(dbIdentifier)) {
                return ORACLE;
            } else if (POSTGRES.getDbIdentifier().equals(dbIdentifier)) {
                return POSTGRES;
            } else {
                var msg = String.format("Unrecognized DB (%s), defaulting to ansi", dbIdentifier);
                LOGGER.warn(msg);
                return ANSI;
            }
        }
    }

    private DatabaseType getDataBaseType(Connection connection) throws SQLException {
        final DatabaseMetaData metaData = connection.getMetaData();
        final String dbProductName = metaData.getDatabaseProductName();
        return DatabaseType.create(dbProductName);
    }

    @Override
    boolean tableExists(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseType databaseType = getDataBaseType(connection);
            final DatabaseMetaData metaData = connection.getMetaData();
            final String[] types = { "TABLE" };
            ResultSet tables = metaData.getTables(null, null, databaseType.tableNamePattern, types);
            while (tables.next()) {
                LOGGER.debug("Found process_instance table");
                return true;
            }
            return false;
        } catch (SQLException e) {
            var msg = "Failed to read table metadata";
            throw new RuntimeException(msg);
        }
    }

    @Override
    void createTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseType databaseType = getDataBaseType(connection);
            final List<String> statements = FileLoader.getQueryFromFile(databaseType.dbIdentifier, "create_tables");
            for (String s : statements) {
                try (PreparedStatement prepareStatement = connection.prepareStatement(s.trim())) {
                    prepareStatement.execute();
                }
            }
            LOGGER.info("DDL successfully done for ProcessInstance");
        } catch (SQLException e) {
            var msg = "Error creating process_instances table, the database should be configured properly before starting the application";
            LOGGER.error(msg, e);
            throw new RuntimeException(msg);
        }
    }

    @Override
    void insertInternal(DataSource dataSource, String processId, UUID id, byte[] payload) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT)) {
            statement.setString(1, id.toString());
            statement.setBytes(2, payload);
            statement.setString(3, processId);
            statement.setLong(4, 0L);
            statement.executeUpdate();
        } catch (Exception e) {
            throw uncheckedException(e, "Error inserting process instance %s", id);
        }
    }

    @Override
    void updateInternal(DataSource dataSource, UUID id, byte[] payload) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setBytes(1, payload);
            statement.setString(2, id.toString());
            statement.executeUpdate();
        } catch (Exception e) {
            throw uncheckedException(e, "Error updating process instance %s", id);
        }
    }

    @Override
    boolean updateWithLock(DataSource dataSource, UUID id, byte[] payload, long version) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE_WITH_LOCK)) {
            statement.setBytes(1, payload);
            statement.setLong(2, version + 1);
            statement.setString(3, id.toString());
            statement.setLong(4, version);
            int count = statement.executeUpdate();
            return count == 1;
        } catch (Exception e) {
            throw uncheckedException(e, "Error updating with lock process instance %s", id);
        }
    }

    @Override
    boolean deleteInternal(DataSource dataSource, UUID id) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setString(1, id.toString());
            int count = statement.executeUpdate();
            return count == 1;
        } catch (Exception e) {
            throw uncheckedException(e, "Error deleting process instance %s", id);
        }
    }

    @Override
    Map<String, Object> findByIdInternal(DataSource dataSource, UUID id) {
        Map<String, Object> result = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setString(1, id.toString());
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

    @Override
    List<byte[]> findAllInternal(DataSource dataSource, String processId) {
        List<byte[]> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            statement.setString(1, processId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getBytes(PAYLOAD));
                }
            }
            return result;
        } catch (Exception e) {
            throw uncheckedException(e, "Error finding all process instances, for processId %s", processId);
        }
    }

    @Override
    Long countInternal(DataSource dataSource, String processId) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(COUNT)) {
            statement.setString(1, processId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("count");
                }
            }
        } catch (Exception e) {
            throw uncheckedException(e, "Error counting process instances, for processId %s", processId);
        }
        return 0l;
    }

}
