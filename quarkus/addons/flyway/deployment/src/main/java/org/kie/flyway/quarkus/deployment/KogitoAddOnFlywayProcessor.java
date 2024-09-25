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
package org.kie.flyway.quarkus.deployment;

import java.util.List;
import java.util.Optional;

import org.kie.flyway.quarkus.KieFlywayQuarkusConfig;
import org.kie.flyway.quarkus.KieFlywayRecorder;

import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.deployment.annotations.*;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class KogitoAddOnFlywayProcessor {

    private static final String FEATURE = "kie-flyway";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Consume(BeanContainerBuildItem.class)
    @Consume(JdbcDataSourceBuildItem.class)
    @Produce(SyntheticBeansRuntimeInitBuildItem.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    void runMigration(
            KieFlywayRecorder recorder,
            KieFlywayQuarkusConfig config,
            List<JdbcDataSourceBuildItem> jdbcDataSourceBuildItems) {

        if (!config.enabled()) {
            return;
        }

        Optional<JdbcDataSourceBuildItem> jdbcDataSourceOptional = jdbcDataSourceBuildItems.stream()
                .filter(JdbcDataSourceBuildItem::isDefault)
                .findFirst();

        if (jdbcDataSourceOptional.isEmpty()) {
            return;
        }

        JdbcDataSourceBuildItem jdbcDataSource = jdbcDataSourceOptional.get();
        recorder.run(config, jdbcDataSource.getName(), jdbcDataSource.getDbKind());
    }
}
