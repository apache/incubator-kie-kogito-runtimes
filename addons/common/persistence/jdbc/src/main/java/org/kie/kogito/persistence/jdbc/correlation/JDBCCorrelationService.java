/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.persistence.jdbc.correlation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.kie.kogito.correlation.Correlation;
import org.kie.kogito.correlation.CorrelationEncoder;
import org.kie.kogito.correlation.CorrelationInstance;
import org.kie.kogito.correlation.CorrelationService;
import org.kie.kogito.event.correlation.MD5CorrelationEncoder;
import org.kie.kogito.persistence.jdbc.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCCorrelationService implements CorrelationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCCorrelationService.class);

    private PostgreSQLCorrelationRepository repository;
    private CorrelationEncoder correlationEncoder;

    public JDBCCorrelationService(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            if (!DatabaseType.POSTGRES.equals(DatabaseType.getDataBaseType(connection))) {
                throw new IllegalArgumentException("Only PostgreSQL is supported for correlation");
            }
        } catch (SQLException e) {
            LOGGER.error("Error getting connection for {}", dataSource);
        }
        this.repository = new PostgreSQLCorrelationRepository(dataSource);
        this.correlationEncoder = new MD5CorrelationEncoder();
    }

    @Override
    public CorrelationInstance create(Correlation correlation, String correlatedId) {
        String encodedCorrelationId = correlationEncoder.encode(correlation);
        return repository.insert(encodedCorrelationId, correlatedId, correlation);
    }

    @Override
    public Optional<CorrelationInstance> find(Correlation correlation) {
        String encoded = correlationEncoder.encode(correlation);
        return Optional.ofNullable(repository.findByEncodedCorrelationId(encoded));
    }

    @Override
    public Optional<CorrelationInstance> findByCorrelatedId(String correlatedId) {
        return Optional.ofNullable(repository.findByCorrelatedId(correlatedId));
    }

    @Override
    public void delete(Correlation correlation) {
        String encoded = correlationEncoder.encode(correlation);
        repository.delete(encoded);
    }
}
