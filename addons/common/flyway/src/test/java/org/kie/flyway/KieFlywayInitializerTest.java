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
import java.util.*;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kie.flyway.test.dataSources.H2TestDataSource;
import org.kie.flyway.test.dataSources.PostgreSQLTestDataSource;
import org.kie.flyway.test.dataSources.TestDataSource;
import org.kie.flyway.test.models.Customer;
import org.kie.flyway.test.models.Guitar;
import org.kie.flyway.test.models.KieFlywayMigration;
import org.kie.flyway.test.utils.TestClassLoader;
import org.kie.kogito.testcontainers.KogitoPostgreSqlContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class KieFlywayInitializerTest {

    static final String MODULE_MIGRATIONS_QUERY_TEMPLATE = "select \"version\", \"description\", \"success\" from \"kie_flyway_history_%s\" where \"version\" = ?";
    static final String QUERY_CUSTOMERS_DATA = "select id, name, last_name, email from customers order by id";
    static final String QUERY_GUITARS_DATA = "select id, brand, model, rating from guitars order by id";
    static final String QUERY_QUERY_TABLE_EXISTS = "select count(*) as count from information_schema.tables where table_name = ?";

    private static final Collection<KieFlywayMigration> EXPECTED_CUSTOMERS_MIGRATIONS;
    private static final Collection<Customer> EXPECTED_CUSTOMERS;
    private static final Collection<KieFlywayMigration> EXPECTED_GUITARS_MIGRATIONS;
    private static final Collection<Guitar> EXPECTED_GUITARS;

    @Container
    private static final KogitoPostgreSqlContainer PG_CONTAINER = new KogitoPostgreSqlContainer();

    private static Collection<TestDataSource> TEST_DATASOURCES;

    private TestClassLoader classLoader;

    static {
        EXPECTED_CUSTOMERS_MIGRATIONS = Arrays.asList(
                new KieFlywayMigration("1.0.0", "Create table %s"),
                new KieFlywayMigration("1.0.1", "Alter table %s"),
                new KieFlywayMigration("1.0.2", "Insert book characters %s"),
                new KieFlywayMigration("1.0.5", "Insert game characters %s"));

        EXPECTED_CUSTOMERS = Arrays.asList(new Customer(1, "Ned", "Stark", "n.stark@winterfell.book"),
                new Customer(2, "Ender", "Wiggin", "ender@endersgame.book"),
                new Customer(3, "Guybrush", "Threepwood", "guybrush@monkeyisland.game"),
                new Customer(4, "Herman", "Toothrot", "toothrot@monkeyisland.game"));

        EXPECTED_GUITARS_MIGRATIONS = Arrays.asList(
                new KieFlywayMigration("1.0.0", "Create guitars table %s"),
                new KieFlywayMigration("1.0.1", "Alter guitars table %s"),
                new KieFlywayMigration("1.0.2", "Insert fender guitars %s"),
                new KieFlywayMigration("1.0.5", "Insert gibson guitars %s"));

        EXPECTED_GUITARS = Arrays.asList(new Guitar(1, "Fender", "Telecaster", 10),
                new Guitar(2, "Fender", "Stratocaster", 9),
                new Guitar(3, "Fender", "Jazzmaster", 7),
                new Guitar(4, "Gibson", "SG", 9),
                new Guitar(5, "Gibson", "Les Paul", 9),
                new Guitar(6, "Gibson", "ES-330", 10));
    }

    @BeforeAll
    public static void start() {
        PG_CONTAINER.start();
        TEST_DATASOURCES = List.of(new PostgreSQLTestDataSource(PG_CONTAINER), new H2TestDataSource("h2"), new H2TestDataSource("ansi"));
    }

    public static Stream<Arguments> getDataSources() {
        return TEST_DATASOURCES.stream()
                .map(Arguments::of);
    }

    @BeforeEach
    public void init() {
        classLoader = new TestClassLoader(this.getClass().getClassLoader());
    }

    @ParameterizedTest
    @MethodSource("getDataSources")
    public void testTestKieFlywayInitializerBuilderValidations(TestDataSource dataSource) {
        Assertions.assertThatThrownBy(() -> KieFlywayInitializer.builder()
                .withDbType(dataSource.getDbType())
                .build()).isInstanceOf(KieFlywayException.class)
                .hasMessage("Cannot create KieFlywayInitializer migration, dataSource is null.");

        Assertions.assertThatThrownBy(() -> KieFlywayInitializer.builder()
                .withDatasource(dataSource.getDataSource())
                .build()).isInstanceOf(KieFlywayException.class)
                .hasMessage("Cannot create KieFlywayInitializer migration, database type is null.");
    }

    @ParameterizedTest
    @MethodSource("getDataSources")
    public void testKieFlywayInitializerValidations(TestDataSource dataSource) {
        classLoader.addKieFlywayModule(classLoader.getResource("initializers/kie-flyway.duplicated1.properties"));
        classLoader.addKieFlywayModule(classLoader.getResource("initializers/kie-flyway.duplicated1.properties"));
        classLoader.addKieFlywayModule(classLoader.getResource("initializers/kie-flyway.duplicated2.properties"));
        classLoader.addKieFlywayModule(classLoader.getResource("initializers/kie-flyway.duplicated2.properties"));

        Assertions.assertThatThrownBy(() -> {
            KieFlywayInitializer.builder()
                    .withDbType(dataSource.getDbType())
                    .withDatasource(dataSource.getDataSource())
                    .withClassLoader(classLoader)
                    .build()
                    .migrate();
        }).isInstanceOf(KieFlywayException.class)
                .hasMessage("Cannot run Kie Flyway migration: Duplicated Modules found test-duplicated-1, test-duplicated-2");

    }

    @Order(1)
    @ParameterizedTest
    @MethodSource("getDataSources")
    public void testFlywayMigrations(TestDataSource dataSource) {

        classLoader.addKieFlywayModule(classLoader.getResource("initializers/kie-flyway.customers.properties"));
        classLoader.addKieFlywayModule(classLoader.getResource("initializers/kie-flyway.guitars.properties"));

        KieFlywayInitializer.builder()
                .withDbType(dataSource.getDbType())
                .withDatasource(dataSource.getDataSource())
                .withClassLoader(classLoader)
                .withModuleExclusions(Arrays.asList("guitars"))
                .build()
                .migrate();

        validateKieFlywayIndex("customers", EXPECTED_CUSTOMERS_MIGRATIONS.stream().limit(3).toList(), dataSource);
        validateCustomersData(EXPECTED_CUSTOMERS.stream().limit(2).toList(), dataSource);

        // Guitars module has been excluded, so it shouldn't be installed. Verifying that tables don't exist
        verifyTableDoesntExist("guitars", dataSource);
        verifyTableDoesntExist("kie_flyway_history_guitars", dataSource);
    }

    public void verifyTableDoesntExist(String tableName, TestDataSource dataSource) {
        try (Connection con = dataSource.getDataSource().getConnection();
                PreparedStatement stmt = con.prepareStatement(QUERY_QUERY_TABLE_EXISTS);) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                Assertions.assertThat(rs.next()).isTrue();
                Assertions.assertThat(rs.getInt("count")).isEqualTo(0);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("getDataSources")
    public void testFlywayMigrationsUpgrade(TestDataSource dataSource) {

        classLoader.addKieFlywayModule(classLoader.getResource("initializers/kie-flyway.customers2.properties"));
        classLoader.addKieFlywayModule(classLoader.getResource("initializers/kie-flyway.guitars2.properties"));

        KieFlywayInitializer.builder()
                .withDbType(dataSource.getDbType())
                .withDatasource(dataSource.getDataSource())
                .withClassLoader(classLoader)
                .withModuleExclusions(Arrays.asList("test-3"))
                .build()
                .migrate();

        validateKieFlywayIndex("customers", EXPECTED_CUSTOMERS_MIGRATIONS, dataSource);
        validateCustomersData(EXPECTED_CUSTOMERS, dataSource);

        validateKieFlywayIndex("guitars", EXPECTED_GUITARS_MIGRATIONS, dataSource);
        validateGuitarsData(dataSource);
    }

    public void validateKieFlywayIndex(String moduleName, Collection<KieFlywayMigration> expectedMigrations, TestDataSource dataSource) {
        expectedMigrations.forEach(kieFlywayMigration -> validateFlywayMigration(moduleName, kieFlywayMigration, dataSource));
    }

    private void validateFlywayMigration(final String moduleName, final KieFlywayMigration migration, final TestDataSource dataSource) {
        try (Connection con = dataSource.getDataSource().getConnection(); PreparedStatement stmt = con.prepareStatement(MODULE_MIGRATIONS_QUERY_TEMPLATE.formatted(moduleName));) {
            stmt.setString(1, migration.version());
            try (ResultSet rs = stmt.executeQuery()) {
                Assertions.assertThat(rs.next())
                        .isTrue();
                Assertions.assertThat(rs.getString("version"))
                        .isEqualTo(migration.version());
                Assertions.assertThat(rs.getString("description"))
                        .isEqualTo(migration.description().formatted(dataSource.getDbType()));
                Assertions.assertThat(rs.getBoolean("success"))
                        .isEqualTo(true);
                Assertions.assertThat(rs.next())
                        .isFalse();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void validateCustomersData(Collection<Customer> expectedCustomers, TestDataSource dataSource) {
        try (Connection con = dataSource.getDataSource().getConnection();
                PreparedStatement stmt = con.prepareStatement(QUERY_CUSTOMERS_DATA);
                ResultSet rs = stmt.executeQuery()) {

            for (Customer customer : expectedCustomers) {
                Assertions.assertThat(rs.next())
                        .isTrue();
                Assertions.assertThat(rs.getInt("id"))
                        .isEqualTo(customer.id());
                Assertions.assertThat(rs.getString("name"))
                        .isEqualTo(customer.name());
                Assertions.assertThat(rs.getString("last_name"))
                        .isEqualTo(customer.lastName());
                Assertions.assertThat(rs.getString("email"))
                        .isEqualTo(customer.email());
            }
            Assertions.assertThat(rs.next())
                    .isFalse();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void validateGuitarsData(TestDataSource dataSource) {
        try (Connection con = dataSource.getDataSource().getConnection();
                PreparedStatement stmt = con.prepareStatement(QUERY_GUITARS_DATA);
                ResultSet rs = stmt.executeQuery()) {

            for (Guitar guitar : EXPECTED_GUITARS) {
                Assertions.assertThat(rs.next())
                        .isTrue();
                Assertions.assertThat(rs.getInt("id"))
                        .isEqualTo(guitar.id());
                Assertions.assertThat(rs.getString("brand"))
                        .isEqualTo(guitar.brand());
                Assertions.assertThat(rs.getString("model"))
                        .isEqualTo(guitar.model());
                Assertions.assertThat(rs.getInt("rating"))
                        .isEqualTo(guitar.rating());
            }

            Assertions.assertThat(rs.next())
                    .isFalse();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @AfterAll
    public static void shutdown() {
        TEST_DATASOURCES.forEach(TestDataSource::shutDown);
    }

}
