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

package org.kie.flyway.springboot;

import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.kie.flyway.KieFlywayException;
import org.kie.flyway.KieFlywayInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.jdbc.support.JdbcUtils;

public class KieFlywaySpringbootInitializer implements InitializingBean, Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieFlywaySpringbootInitializer.class);

    private final KieFlywaySpringbootProperties properties;
    private final DataSource dataSource;

    public KieFlywaySpringbootInitializer(KieFlywaySpringbootProperties properties, DataSource dataSource) {
        this.properties = properties;
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() {
        assertValue(properties, "Cannot run Kie Flyway migration: configuration is null.");
        assertValue(dataSource, "Cannot run Kie Flyway migration: default datasource not found.");

        String dbType = getDataSourceType();

        assertValue(dbType, "Cannot run Kie Flyway migration: cannot determine database type.");

        Collection<String> excludedModules = properties.getModules()
                .entrySet()
                .stream().filter(entry -> !entry.getValue().isEnabled())
                .map(Map.Entry::getKey)
                .toList();

        KieFlywayInitializer.Builder.get()
                .withClassLoader(Thread.currentThread().getContextClassLoader())
                .withDatasource(dataSource)
                .withDbType(dbType)
                .withModuleExclusions(excludedModules)
                .build()
                .migrate();
    }

    private String getDataSourceType() {
        try {
            return JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName);
        } catch (Exception e) {
            LOGGER.error("Couldn't extract database product name from datasource ", e);
            throw new KieFlywayException("Couldn't extract database product name from datasource", e);
        }
    }

    private void assertValue(Object value, String message) {
        if (Objects.isNull(value)) {
            LOGGER.warn(message);
            throw new KieFlywayException(message);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
