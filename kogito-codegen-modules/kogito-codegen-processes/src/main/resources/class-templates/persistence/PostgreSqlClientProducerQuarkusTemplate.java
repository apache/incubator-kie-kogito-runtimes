/*
 *  Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.persistence;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.quarkus.arc.DefaultBean;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PostgreSqlClientProducerQuarkusTemplate {

    @Produces
    @DefaultBean
    private PgPool pgClient(@ConfigProperty(name = "postgresql.connection.uri") String connectionUri) {
        // Customization of Pool options
        return PgPool.pool(connectionUri);
    }
}
