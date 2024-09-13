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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.flyway.utils.TestClassLoader;

public abstract class AbstractKieFlywayInitializerTest {

    private static final Collection<KieFlywayMigration> EXPECTED_MIGRATIONS;

    static final String MODULE_MIGRATIONS_QUERY = "select \"version\", \"description\", \"success\" from \"kie_flyway_history_test\"";
    static final String TEST_TABLE_QUERY = "select id, message, dbtype from test_table";

    static {
        EXPECTED_MIGRATIONS = Arrays.asList(
                new KieFlywayMigration("1.0.0", "Create table"),
                new KieFlywayMigration("1.0.1", "Alter table"),
                new KieFlywayMigration("1.0.2", "Insert data"));
    }

    protected boolean skipFirstMigrationValidation() {
        return false;
    }

    protected abstract DataSource getDataSource();

    protected abstract String getDbType();

    @Test
    public void testTestKieFlywayInitializerBuilderValidations() {
        Assertions.assertThatThrownBy(() -> KieFlywayInitializer.Builder.get()
                .withDbType(getDbType())
                .build()).isInstanceOf(KieFlywayException.class)
                .hasMessage("Cannot create KieFlywayInitializer migration, dataSource is null.");

        Assertions.assertThatThrownBy(() -> KieFlywayInitializer.Builder.get()
                .withDatasource(getDataSource())
                .build()).isInstanceOf(KieFlywayException.class)
                .hasMessage("Cannot create KieFlywayInitializer migration, database type is null.");
    }

    @Test
    public void testKieFlywayInitializerValidations() {
        TestClassLoader classLoader = new TestClassLoader(this.getClass().getClassLoader());
        classLoader.addModuleConfig(classLoader.getResource("initializers/kie-flyway.duplicated1.properties"));
        classLoader.addModuleConfig(classLoader.getResource("initializers/kie-flyway.duplicated1.properties"));
        classLoader.addModuleConfig(classLoader.getResource("initializers/kie-flyway.duplicated2.properties"));
        classLoader.addModuleConfig(classLoader.getResource("initializers/kie-flyway.duplicated2.properties"));

        Assertions.assertThatThrownBy(() -> {
            KieFlywayInitializer.Builder.get()
                    .withDbType(getDbType())
                    .withDatasource(getDataSource())
                    .withClassLoader(classLoader)
                    .build()
                    .migrate();
        }).isInstanceOf(KieFlywayException.class)
                .hasMessage("Cannot run Kie Flyway migration: Duplicated Modules found test-duplicated-1, test-duplicated-2");

    }

    @Test
    public void testFlywayMigrations() {
        KieFlywayInitializer.Builder.get()
                .withDbType(getDbType())
                .withDatasource(getDataSource())
                .build()
                .migrate();

        validateDBData(MODULE_MIGRATIONS_QUERY, this::validateFlywayMigrationsTable);
        validateDBData(TEST_TABLE_QUERY, this::validateTestTable);
    }

    private void validateFlywayMigrationsTable(ResultSet rs) {
        try {
            if (skipFirstMigrationValidation()) {
                Assertions.assertThat(rs.next())
                        .isTrue();

            }
            for (KieFlywayMigration migration : EXPECTED_MIGRATIONS) {
                Assertions.assertThat(rs.next())
                        .isTrue();
                Assertions.assertThat(rs.getString("version"))
                        .isEqualTo(migration.version());
                Assertions.assertThat(rs.getString("description"))
                        .isEqualTo(migration.description());
                Assertions.assertThat(rs.getBoolean("success"))
                        .isEqualTo(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateTestTable(ResultSet rs) {
        try {
            Assertions.assertThat(rs.next())
                    .isTrue();

            Assertions.assertThat(rs.getInt("id"))
                    .isEqualTo(1);

            Assertions.assertThat(rs.getString("message"))
                    .isEqualTo("Hello from Kie Flyway");

            Assertions.assertThat(rs.getString("dbtype"))
                    .isEqualTo(getDbType());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void validateDBData(String query, Consumer<ResultSet> validator) {
        try (Connection con = getDataSource().getConnection();
                PreparedStatement stmt = con.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            validator.accept(rs);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private record KieFlywayMigration(String version, String description) {
    }
}
