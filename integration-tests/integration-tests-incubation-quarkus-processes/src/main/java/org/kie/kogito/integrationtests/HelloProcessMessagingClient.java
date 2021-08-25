/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.integrationtests;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.kie.kogito.incubation.common.MapDataContext;

@ApplicationScoped
public class HelloProcessMessagingClient {

    @Channel("hello-publisher")
    Emitter<User> emitter;

    private final Map<Object, CompletableFuture<MapDataContext>> evaluationCallbacks = new ConcurrentHashMap<>();

    @Incoming("hello-response")
    public void onResponse(Map<String, Object> response) {
        Optional.ofNullable(response)
                .map(i -> MapDataContext.from(i))
                .map(i -> i.get("user", User.class))
                .map(evaluationCallbacks::remove)
                .ifPresent(callback -> callback.complete(MapDataContext.from(response)));
    }

    public CompletableFuture<MapDataContext> evaluate(User user) {
        emitter.send(user);
        CompletableFuture<MapDataContext> response = new CompletableFuture<>();
        evaluationCallbacks.put(user, response);
        return response;
    }
}