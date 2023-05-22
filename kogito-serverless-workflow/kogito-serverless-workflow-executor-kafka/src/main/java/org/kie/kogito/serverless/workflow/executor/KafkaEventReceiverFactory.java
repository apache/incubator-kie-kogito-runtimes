/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow.executor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.EventReceiverFactory;
import org.kie.kogito.serverless.workflow.utils.ConfigResolverHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.CloudEvent;

public class KafkaEventReceiverFactory implements EventReceiverFactory {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventReceiverFactory.class);
    private Map<String, String> trigger2Topic = KafkaPropertiesFactory.get().triggerToTopicMap("kogito.addon.messaging.incoming.trigger.");
    private Map<String, KafkaEventReceiver> receivers = new HashMap<>();
    private Consumer<byte[], CloudEvent> consumer;
    private Lock consumerLock = new ReentrantLock();
    private Thread consumerThread;

    @Override
    public EventReceiver apply(String trigger) {
        Set<String> topics;
        EventReceiver receiver;
        synchronized (receivers) {
            receiver = receivers.computeIfAbsent(trigger2Topic.getOrDefault(trigger, trigger), k -> new KafkaEventReceiver());
            topics = receivers.keySet();
        }
        try {
            consumerLock.lock();
            boolean createConsumer = consumer == null;
            if (createConsumer) {
                consumer = createKafkaConsumer();
                consumerThread = new Thread(this::eventLoop);
            }
            consumer.subscribe(topics);
            if (createConsumer) {
                consumerThread.start();
            }
        } finally {
            consumerLock.unlock();
        }
        return receiver;
    }

    private void eventLoop() {
        while (true) {
            Iterable<ConsumerRecord<byte[], CloudEvent>> records;
            int pollTimeout = ConfigResolverHolder.getConfigResolver().getConfigProperty("kogito.sw.executor.event.pollInterval", int.class).orElse(10);
            try {
                consumerLock.lock();
                if (consumer == null) {
                    return;
                }
                records = consumer.poll(Duration.ofSeconds(pollTimeout));
            } finally {
                consumerLock.unlock();
            }
            for (ConsumerRecord<byte[], CloudEvent> record : records) {
                KafkaEventReceiver receiver;
                String topic = record.topic();
                synchronized (receivers) {
                    receiver = receivers.get(topic);
                }
                if (receiver == null) {
                    logger.info("No subscription for topic {}", topic);
                } else {
                    receiver.onEvent(record.value());
                }
            }
            try {
                consumerLock.lock();
                if (consumer == null) {
                    return;
                }
                consumer.commitAsync();
            } finally {
                consumerLock.unlock();
            }
        }
    }

    @Override
    public void close() throws InterruptedException {
        synchronized (receivers) {
            receivers.clear();
        }
        try {
            consumerLock.lock();
            if (consumer != null) {
                consumer.close();
                consumer = null;
                consumerThread.join();
                consumerThread = null;
            }
        } finally {
            consumerLock.unlock();
        }
    }

    protected Consumer<byte[], CloudEvent> createKafkaConsumer() {
        return new KafkaConsumer<>(KafkaPropertiesFactory.get().getKafkaConsumerConfig());
    }
}
