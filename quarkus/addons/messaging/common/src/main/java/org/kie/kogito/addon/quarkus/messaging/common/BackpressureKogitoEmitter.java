/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addon.quarkus.messaging.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import io.vertx.core.impl.ConcurrentHashSet;

@ApplicationScoped
public class BackpressureKogitoEmitter implements QuarkusEmitterController {

    private Set<String> statuses = new ConcurrentHashSet<>();
    private Map<String, Runnable> handlers = new HashMap<>();

    @Override
    public void resume(String channelName) {
        if (statuses.remove(channelName)) {
            handlers.get(channelName).run();
        }
    }

    @Override
    public void stop(String channelName) {
        statuses.add(channelName);
    }

    @Override
    public boolean isEnabled(String channelName) {
        return !statuses.contains(channelName);
    }

    public void registerHandler(String channelName, Runnable runnable) {
        handlers.put(channelName, runnable);
    }
}
