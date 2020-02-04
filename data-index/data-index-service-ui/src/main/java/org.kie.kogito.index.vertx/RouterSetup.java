/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RouterSetup {

    @Inject
    @ConfigProperty(name = "kogito.data-index-service.url", defaultValue = "false")
    String graphiqlURL;

    @Inject
    Vertx vertx;

    private WebClient client;

    @PostConstruct
    public void setup() {
        client = WebClient.create(vertx);
    }

    void setupRouter(@Observes Router router) {
        router.route("/logout").handler(ctx -> {
            ctx.clearUser();
            ctx.response().putHeader("location", "/graphiql/").setStatusCode(302).end();
        });
        router.route("/graphql").handler(ctx -> {
            ctx.request().bodyHandler(bodyBuffer -> {
                getRemoteRequest(ctx).sendBuffer(bodyBuffer,
                                                 result -> {
                                                     if (result.succeeded() && (result.result().bodyAsBuffer() != null)) {
                                                         ctx.response().end(result.result().bodyAsBuffer());
                                                     } else {
                                                         ctx.response().putHeader("location", "/logout/").setStatusCode(302).end();
                                                     }
                                                 });
            });
        });
        router.route("/*").handler(ctx -> {
            HttpRequest<Buffer> remoteRequest = getRemoteRequest(ctx);
            if (remoteRequest != null) {
                getRemoteRequest(ctx).send(result -> {
                    if (result.succeeded() && (result.result().bodyAsBuffer() != null)) {
                        ctx.response().end(result.result().bodyAsBuffer());
                    } else {
                        ctx.response().putHeader("location", "/logout/").setStatusCode(302).end();
                    }
                });
            } else {
                ctx.response().end();
            }
        });
    }

    private HttpRequest<Buffer> getRemoteRequest(RoutingContext ctx) {
        String bearerToken = "";
        if (ctx.user() != null) {
            bearerToken = "Bearer " + ((QuarkusHttpUser) ctx.user()).getSecurityIdentity().getCredential(AccessTokenCredential.class).getToken();
        } else {
            bearerToken = ctx.request().headers().get("Bearer-token");
        }
        if (bearerToken != null) {

            HttpRequest<Buffer> remoteRequest;
            HttpMethod method = ctx.request().method();
            String remoteURL = graphiqlURL + ctx.request().path();
            if (method == HttpMethod.POST) {
                remoteRequest = client.postAbs(remoteURL);
            } else if (method == HttpMethod.DELETE) {
                remoteRequest = client.deleteAbs(remoteURL);
            } else if (method == HttpMethod.GET) {
                remoteRequest = client.getAbs(remoteURL);
            } else {
                remoteRequest = client.putAbs(remoteURL);
            }

            //headers
            remoteRequest.putHeader(HttpHeaders.AUTHORIZATION.toString(), bearerToken)
                    .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                    .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                    .putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS.toString(), "Authorization, Content-Type, Accept")
                    .putHeader("Bearer-token", bearerToken)
                    .putHeader(HttpHeaders.LOCATION.toString(), graphiqlURL)
                    .putHeaders(ctx.request().headers());
            remoteRequest.putHeader(HttpHeaders.ORIGIN.toString(), graphiqlURL);

            //queryparams
            MultiMap multimap = ctx.queryParams();
            multimap.names().stream().forEach(paramName -> remoteRequest.addQueryParam(paramName, multimap.get(paramName)));

            return remoteRequest;
        }
        return null;
    }
}