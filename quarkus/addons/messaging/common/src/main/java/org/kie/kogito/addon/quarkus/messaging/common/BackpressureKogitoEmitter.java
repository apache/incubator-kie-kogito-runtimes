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
    public boolean resume(String channelName) {
        boolean result = statuses.remove(channelName);
        if (result) {
            Runnable handler = handlers.get(channelName);
            if (handler != null) {
                handler.run();
            }
        }
        return result;
    }

    @Override
    public boolean stop(String channelName) {
        return statuses.add(channelName);
    }

    @Override
    public boolean isEnabled(String channelName) {
        return !statuses.contains(channelName);
    }

    public void registerHandler(String channelName, Runnable runnable) {
        handlers.put(channelName, runnable);
    }
}
