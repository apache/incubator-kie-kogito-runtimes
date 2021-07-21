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
package org.kie.kogito.addon.cloudevents.quarkus;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.addon.cloudevents.Subscription;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.KogitoEventStreams;
import org.kie.kogito.event.SubscriptionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;

@Startup
@ApplicationScoped
public class QuarkusCloudEventReceiver implements EventReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusCloudEventReceiver.class);

    private Collection<Subscription<Object>> consumers;

    @PostConstruct
    private void init() {
        consumers = new CopyOnWriteArrayList<>();
    }

    /**
     * Listens to a message published in the {@link KogitoEventStreams#INCOMING} channel
     *
     * @param message the given message in string format
     */
    @Incoming(KogitoEventStreams.INCOMING)
    public CompletionStage<?> onEvent(Message<String> message) {
        LOGGER.debug("Received message from channel {}: {}", KogitoEventStreams.INCOMING, message);
        return produce(message.getPayload(), (v, e) -> {
            LOGGER.debug("Acking message {}", message.getPayload());
            message.ack();
            if (e != null) {
                LOGGER.error("Error processing message {}", message.getPayload(), e);
            }
        });
    }

    public CompletionStage<?> produce(final String message) {
        return produce(message, null);
    }

    public CompletionStage<?> produce(final String message, BiConsumer<Object, Throwable> callback) {
        CompletionStage<?> result = CompletableFuture.completedFuture(null);
        CompletionStage<?> future = result;
        for (Subscription<Object> subscription : consumers) {
            Object object;
            try {
                object = subscription.getInfo().getConverter().apply(message, subscription.getInfo().getOutputClass());
                future = future.thenCompose(f -> subscription.getConsumer().apply(object));
            } catch (IOException e) {
                LOGGER.info("Cannot convert to {} from {}, ignoring type {}, exception message is {}", subscription.getInfo().getOutputClass(), message,
                        subscription.getInfo().getType(), e.getMessage());
            }
        }
        if (callback != null) {
            future.whenComplete(callback);
        }
        return result;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> void subscribe(Function<T, CompletionStage<?>> consumer, SubscriptionInfo<String, T> info) {
        consumers.add(new Subscription(consumer, info));
    }
}
