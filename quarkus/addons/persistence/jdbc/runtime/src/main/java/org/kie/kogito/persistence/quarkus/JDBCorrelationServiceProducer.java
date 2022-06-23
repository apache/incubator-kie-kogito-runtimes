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
package org.kie.kogito.persistence.quarkus;

import java.sql.Connection;
import java.sql.SQLException;

import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

import org.kie.kogito.correlation.CorrelationService;
import org.kie.kogito.persistence.jdbc.DatabaseType;
import org.kie.kogito.persistence.jdbc.correlation.JDBCCorrelationService;
import org.kie.kogito.services.event.correlation.DefaultCorrelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCorrelationServiceProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCorrelationServiceProducer.class);

    @Produces
    public CorrelationService jdbcCorrelationService(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            if (!DatabaseType.POSTGRES.equals(DatabaseType.getDataBaseType(connection))) {
                return new DefaultCorrelationService();
            }
        } catch (SQLException e) {
            LOGGER.error("Error getting connection for {}", dataSource);
        }
        return new JDBCCorrelationService(dataSource);
    }
}
