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

package org.kie.flyway;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractH2KieFlywayKieFlywayMigrationInitializerTest extends AbstractKieFlywayInitializerTest {
    private static JdbcDataSource dataSource;

    @BeforeAll
    public static void start() {
        try {
            dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=true");
            dataSource.setUser("sa");
            dataSource.setPassword("sa");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void stop() {
        try (Connection con = dataSource.getConnection(); Statement stmt = con.createStatement()) {
            stmt.execute("SHUTDOWN");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean skipFirstMigrationValidation() {
        return true;
    }

    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
