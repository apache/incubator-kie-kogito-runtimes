/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.index.vertx;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import graphql.GraphQL;
import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class VertxRouterSetup {

    @Inject
    @ConfigProperty(name = "quarkus.oidc.enabled", defaultValue = "false")
    Boolean authEnabled;

    @Inject
    GraphQL graphQL;

    void setupRouter(@Observes Router router) {
        GraphiQLHandler graphiQLHandler = GraphiQLHandler.create(new GraphiQLHandlerOptions().setEnabled(true));
        if (Boolean.TRUE.equals(authEnabled)) {
            graphiQLHandler.graphiQLRequestHeaders(rc -> {

                MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
                String token = "";
                if (rc.user() != null) {
                    token = ((QuarkusHttpUser) rc.user()).getSecurityIdentity().getCredential(AccessTokenCredential.class).getToken();
                } else {
                    token =  rc.request().getHeader(HttpHeaders.AUTHORIZATION);
                }
                multiMap.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                multiMap.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                multiMap.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type, Accept");
                multiMap.add("Bearer-token",  "Bearer " + token);
                rc.request().headers().addAll(multiMap);
                return multiMap.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            });
        }
        router.route("/graphiql/*").handler(graphiQLHandler);
        router.route().handler(LoggerHandler.create());
        router.route().handler(StaticHandler.create());
        router.route().handler(FaviconHandler.create());
        router.route("/").handler(ctx -> ctx.response().putHeader("location", "/graphiql/").setStatusCode(302).end());
        router.route("/graphql").handler(ApolloWSHandler.create(graphQL));
        router.route("/graphql").handler(GraphQLHandler.create(graphQL, new GraphQLHandlerOptions()));
    }
}
