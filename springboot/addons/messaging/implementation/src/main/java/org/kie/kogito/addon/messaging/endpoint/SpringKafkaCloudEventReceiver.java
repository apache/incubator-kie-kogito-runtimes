/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.addon.messaging.endpoint;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.kie.kogito.config.ConfigBean;
import org.kie.kogito.event.CloudEventUnmarshallerFactory;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.EventUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class SpringKafkaCloudEventReceiver implements EventReceiver {

    private static final Logger log = LoggerFactory.getLogger(SpringKafkaCloudEventReceiver.class);

    @Autowired
    EventUnmarshaller<Object> eventDataUnmarshaller;

    @Autowired
    CloudEventUnmarshallerFactory<Object> cloudEventUnmarshaller;

    @Autowired
    ConfigBean configBean;

    @KafkaListener(topics = { "#{springTopics.getIncomingTopics}" })
    public void receive(ConsumerRecord<String, String> message, Acknowledgment ack) throws InterruptedException {
        log.debug("Receive message with key {} for topic {}", message.key(), message.topic());
        CompletionStage<?> future = CompletableFuture.completedFuture(null);

        future.whenComplete((v, e) -> acknowledge(e, ack));
    }

    private void acknowledge(Throwable ex, Acknowledgment ack) {
        if (ex != null) {
            log.error("Event publishing failed", ex);
        } else {
            log.debug("Acknoledge message");
            ack.acknowledge();
        }
    }
}
