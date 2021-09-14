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
package org.kie.kogito.addon.quarkus.messaging.common;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.event.AbstractEventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQuarkusCloudEventReceiver extends AbstractEventReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractQuarkusCloudEventReceiver.class);

    public CompletionStage<?> produce(final String message) {
        return produce(message, null);
    }

    public CompletionStage<?> produce(final Message<String> message) {
        LOGGER.debug("Received message {}", message);
        return produce(message.getPayload(), (v, e) -> {
            LOGGER.debug("Acking message {}", message.getPayload());
            message.ack();
            if (e != null) {
                LOGGER.error("Error processing message {}", message.getPayload(), e);
            }
        });
    }

    private CompletionStage<?> produce(final String message, BiConsumer<Object, Throwable> callback) {
        CompletionStage<?> forwardResult = forwardToSubscribers(message);
        return callback == null ? forwardResult : forwardResult.whenComplete(callback);
    }
}
