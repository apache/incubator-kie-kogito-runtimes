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
package org.kie.kogito.addon.cloudevents;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.kie.kogito.event.SubscriptionInfo;

public class Subscription<T> {
    private final Function<T, CompletionStage<?>> consumer;
    private final SubscriptionInfo<String, T> info;

    public Subscription(Function<T, CompletionStage<?>> consumer, SubscriptionInfo<String, T> info) {
        this.consumer = consumer;
        this.info = info;
    }

    public Function<T, CompletionStage<?>> getConsumer() {
        return consumer;
    }

    public SubscriptionInfo<String, T> getInfo() {
        return info;
    }
}
