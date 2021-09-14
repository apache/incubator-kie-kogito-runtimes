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
package org.kie.kogito.addon.cloudevents.spring;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.kie.kogito.addon.cloudevents.AbstractEventReceiver;
import org.kie.kogito.event.KogitoEventStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SpringKafkaCloudEventReceiver extends AbstractEventReceiver {

    private static final Logger log = LoggerFactory.getLogger(SpringKafkaCloudEventReceiver.class);

    @KafkaListener(topics = "${kogito.addon.cloudevents.kafka." + KogitoEventStreams.INCOMING + ":" + KogitoEventStreams.INCOMING + "}")
    public void receive(@Payload Collection<String> messages) throws InterruptedException {
        log.debug("Received {} events", messages.size());
        CompletableFuture.allOf(
                messages.stream()
                        .map(this::forwardToSubscribers)
                        .map(CompletionStage::toCompletableFuture)
                        .toArray(CompletableFuture[]::new))
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        if (ex instanceof IOException) {
                            log.info(ex.getMessage(), ex);
                        } else {
                            log.error(ex.getMessage(), ex);
                        }
                        log.debug("All operations in batch completed with errors");
                    } else {
                        log.debug("All operations in batch completed without errors");
                    }
                });
    }
}
