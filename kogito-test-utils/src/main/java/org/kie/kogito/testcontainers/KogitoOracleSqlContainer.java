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
package org.kie.kogito.testcontainers;

import java.util.function.Consumer;

import org.kie.kogito.test.resources.TestResource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.utility.DockerImageName;

/**
 * Placeholder for OracleXE Container (cleaned for Legal restrictions related to oracle license).
 */
public class KogitoOracleSqlContainer extends JdbcDatabaseContainer<KogitoOracleSqlContainer> implements TestResource {

    public KogitoOracleSqlContainer() {
        this(null);
    }

    public KogitoOracleSqlContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    private Consumer<OutputFrame> getLogger() {
        return f -> System.out.print(f.getUtf8String());
    }

    @Override
    public String getResourceName() {
        return null;
    }

    @Override
    public int getMappedPort() {
        return 0;
    }

    @Override
    public String getDriverClassName() {
        return null;
    }

    @Override
    public String getJdbcUrl() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    protected String getTestQueryString() {
        return null;
    }
}
