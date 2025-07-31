/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.serverless.workflow.token.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.kie.kogito.serverless.workflow.openapi.persistence.TokenCacheRepository;
import org.kie.kogito.serverless.workflow.openapi.persistence.model.TokenCacheRecord;
import org.kie.kogito.serverless.workflow.openapi.utils.CacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

/**
 * JDBC-based repository for token cache operations.
 * Follows the same pattern as other JDBC repositories in the codebase.
 */
@ApplicationScoped
@Alternative
@Priority(200)
public class JdbcTokenCacheRepository implements TokenCacheRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTokenCacheRepository.class);

    // SQL queries following the same pattern as other JDBC repositories
    static final String INSERT =
            "INSERT INTO kogito_oauth2_token_cache (process_instance_id, auth_name, access_token, refresh_token, expiration_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    static final String UPDATE = "UPDATE kogito_oauth2_token_cache SET access_token = ?, refresh_token = ?, expiration_time = ?, updated_at = ? WHERE process_instance_id = ? AND auth_name = ?";
    static final String FIND_BY_KEY =
            "SELECT process_instance_id, auth_name, access_token, refresh_token, expiration_time, created_at, updated_at FROM kogito_oauth2_token_cache WHERE process_instance_id = ? AND auth_name = ?";
    static final String DELETE_BY_KEY = "DELETE FROM kogito_oauth2_token_cache WHERE process_instance_id = ? AND auth_name = ?";
    static final String DELETE_EXPIRED = "DELETE FROM kogito_oauth2_token_cache WHERE expiration_time < ?";
    static final String FIND_EXPIRING_SOON =
            "SELECT process_instance_id, auth_name, access_token, refresh_token, expiration_time, created_at, updated_at FROM kogito_oauth2_token_cache WHERE expiration_time < ?";
    static final String FIND_ALL = "SELECT process_instance_id, auth_name, access_token, refresh_token, expiration_time, created_at, updated_at FROM kogito_oauth2_token_cache";

    private final DataSource dataSource;

    public JdbcTokenCacheRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TokenCacheRecord save(TokenCacheRecord record) {
        // Check if record exists first - use direct method since we have the components
        Optional<TokenCacheRecord> existing = findByKey(record.getProcessInstanceId(), record.getAuthName());

        if (existing.isPresent()) {
            return update(record);
        } else {
            return insert(record);
        }
    }

    private TokenCacheRecord insert(TokenCacheRecord record) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT)) {

            statement.setString(1, record.getProcessInstanceId());
            statement.setString(2, record.getAuthName());
            statement.setString(3, record.getAccessToken());
            statement.setString(4, record.getRefreshToken());
            statement.setLong(5, record.getExpirationTime());
            statement.setTimestamp(6, Timestamp.from(record.getCreatedAt()));
            statement.setTimestamp(7, Timestamp.from(record.getUpdatedAt()));

            int executed = statement.executeUpdate();
            if (executed > 0) {
                LOGGER.debug("Inserted token cache record for processInstanceId: {}, authName: {}",
                        record.getProcessInstanceId(), record.getAuthName());
                return record;
            } else {
                throw new RuntimeException("Failed to insert token cache record for processInstanceId: " +
                        record.getProcessInstanceId() + ", authName: " + record.getAuthName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error inserting token cache record for processInstanceId: " +
                    record.getProcessInstanceId() + ", authName: " + record.getAuthName(), e);
        }
    }

    private TokenCacheRecord update(TokenCacheRecord record) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            record.updateTimestamp(); // Update the timestamp

            statement.setString(1, record.getAccessToken());
            statement.setString(2, record.getRefreshToken());
            statement.setLong(3, record.getExpirationTime());
            statement.setTimestamp(4, Timestamp.from(record.getUpdatedAt()));
            statement.setString(5, record.getProcessInstanceId());
            statement.setString(6, record.getAuthName());

            int executed = statement.executeUpdate();
            if (executed > 0) {
                LOGGER.debug("Updated token cache record for processInstanceId: {}, authName: {}",
                        record.getProcessInstanceId(), record.getAuthName());
                return record;
            } else {
                throw new RuntimeException("Failed to update token cache record for processInstanceId: " +
                        record.getProcessInstanceId() + ", authName: " + record.getAuthName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating token cache record for processInstanceId: " +
                    record.getProcessInstanceId() + ", authName: " + record.getAuthName(), e);
        }
    }

    @Override
    public Optional<TokenCacheRecord> findByKey(String processInstanceId, String authName) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_KEY)) {

            statement.setString(1, processInstanceId);
            statement.setString(2, authName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToRecord(resultSet));
                }
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding token cache record by processInstanceId: " +
                    processInstanceId + ", authName: " + authName, e);
        }
    }

    @Override
    public Optional<TokenCacheRecord> findByCacheKey(String cacheKey) {
        // Extract components from cache key and delegate to the main method
        String processInstanceId = CacheUtils.extractProcessInstanceIdFromCacheKey(cacheKey);
        String authName = CacheUtils.extractAuthNameFromCacheKey(cacheKey);
        return findByKey(processInstanceId, authName);
    }

    @Override
    public void deleteByKey(String processInstanceId, String authName) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(DELETE_BY_KEY)) {

            statement.setString(1, processInstanceId);
            statement.setString(2, authName);
            int executed = statement.executeUpdate();

            if (executed > 0) {
                LOGGER.debug("Deleted token cache record for processInstanceId: {}, authName: {}",
                        processInstanceId, authName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error deleting token cache record for processInstanceId: " +
                    processInstanceId + ", authName: " + authName, e);
        }
    }

    @Override
    public void deleteByCacheKey(String cacheKey) {
        // Extract components from cache key and delegate to the main method
        String processInstanceId = CacheUtils.extractProcessInstanceIdFromCacheKey(cacheKey);
        String authName = CacheUtils.extractAuthNameFromCacheKey(cacheKey);
        deleteByKey(processInstanceId, authName);
    }

    @Override
    public List<TokenCacheRecord> findAll() {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {

            try (ResultSet resultSet = statement.executeQuery()) {
                List<TokenCacheRecord> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(mapResultSetToRecord(resultSet));
                }
                return results;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding all token cache records", e);
        }
    }

    private TokenCacheRecord mapResultSetToRecord(ResultSet resultSet) throws Exception {
        TokenCacheRecord record = new TokenCacheRecord();
        record.setProcessInstanceId(resultSet.getString("process_instance_id"));
        record.setAuthName(resultSet.getString("auth_name"));
        record.setAccessToken(resultSet.getString("access_token"));
        record.setRefreshToken(resultSet.getString("refresh_token"));
        record.setExpirationTime(resultSet.getLong("expiration_time"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            record.setCreatedAt(createdAt.toInstant());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            record.setUpdatedAt(updatedAt.toInstant());
        }

        return record;
    }
}
