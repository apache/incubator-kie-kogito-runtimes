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

package org.kie.flyway.quarkus;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.kie.flyway.KieFlywayException;
import org.kie.flyway.KieFlywayInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class KieFlywayRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieFlywayRecorder.class);

    public void run(KieFlywayQuarkusConfig config, String defaultDSName, String dbKind) {

        assertValue(config, "Cannot run Kie Flyway migration: configuration is null.");
        assertValue(dbKind, "Cannot run Kie Flyway migration: `quarkus.datasource.dbKind` is null.");

        DataSources agroalDatasourceS = Arc.container().select(DataSources.class).get();
        DataSource dataSource = agroalDatasourceS.getDataSource(defaultDSName);

        assertValue(dataSource, "Cannot run Kie Flyway migration: default datasource not found.");

        Collection<String> excludedModules = config.modules()
                .entrySet()
                .stream().filter(entry -> !entry.getValue().enabled())
                .map(Map.Entry::getKey)
                .toList();

        KieFlywayInitializer.Builder.get()
                .withDatasource(dataSource)
                .withDbType(dbKind)
                .withClassLoader(Thread.currentThread().getContextClassLoader())
                .withModuleExclusions(excludedModules)
                .build().migrate();
    }

    private void assertValue(Object value, String message) {
        if (Objects.isNull(value)) {
            LOGGER.warn(message);
            throw new KieFlywayException(message);
        }
    }
}
