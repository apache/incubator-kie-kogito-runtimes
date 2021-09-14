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
package org.kie.kogito.event;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public abstract class AbstractEventReceiver implements EventReceiver {

    @Override
    public <T> void subscribe(Function<T, CompletionStage<Void>> consumer, SubscriptionInfo<String, T> info) {
        subscriptions.add(new Subscription<>(consumer, info));
    }

    private final Collection<Subscription<?>> subscriptions = new CopyOnWriteArrayList<>();

    protected CompletionStage<Void> forwardToSubscribers(String message) {
        return CompletableFuture.allOf(
                subscriptions.stream()
                        .map(s -> s.forward(message))
                        .map(CompletionStage::toCompletableFuture)
                        .toArray(CompletableFuture[]::new));
    }

    protected static class Subscription<T> {
        private final Function<T, CompletionStage<Void>> consumer;
        private final SubscriptionInfo<String, T> info;

        public Subscription(Function<T, CompletionStage<Void>> consumer, SubscriptionInfo<String, T> info) {
            this.consumer = consumer;
            this.info = info;
        }

        public CompletionStage<Void> forward(String message) {
            try {
                return consumer.apply(info.convert(message));
            } catch (IOException ex) {
                return CompletableFuture.failedFuture(new IOException(
                        String.format("Cannot convert to %s from %s, ignoring type %s", info.getOutputClass(), message, info.getType()),
                        ex));
            }
        }
    }

}
