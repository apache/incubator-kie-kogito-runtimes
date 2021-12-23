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
package org.kie.kogito.test.quarkus.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;

import static java.util.Collections.singleton;

/**
 * Kafka client for Kogito Example tests.
 */
public class KafkaTestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTestClient.class);
    private static final int TIMEOUT = 10;

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private Vertx vertx = Vertx.vertx();
    private String hosts;

    public KafkaTestClient(String hosts) {
        this.hosts = hosts;
    }

    public KafkaTestClient(KafkaProducer<String, String> producer, KafkaConsumer<String, String> consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    private KafkaConsumer<String, String> createDefaultConsumer(String hosts) {
        Map<String, String> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaTestClient.class.getName() + "Consumer");
        return KafkaConsumer.create(vertx, consumerConfig);
    }

    private KafkaProducer<String, String> createDefaultProducer(String hosts) {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "1");
        producerConfig.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaTestClient.class.getName() + "Producer");
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        return KafkaProducer.create(vertx, producerConfig);
    }

    public void consume(Set<String> topics, Consumer<String> callback) {
        if (consumer == null) {
            consumer = createDefaultConsumer(hosts);
        } else {
            waitForCompletion(consumer.unsubscribe());
        }
        consumer.handler(record -> callback.accept(record.value()));
        waitForCompletion(consumer.subscribe(topics)
                .onSuccess(v -> LOGGER.debug("Kafka consumer subscribed to topic(s): {}", topics)));
    }

    public void consume(String topic, Consumer<String> callback) {
        consume(singleton(topic), callback);
    }

    public void produce(String data, String topic) {
        if (producer == null) {
            producer = createDefaultProducer(hosts);
        }
        LOGGER.info("Publishing event with data {} for topic {}", data, topic);
        producer.send(KafkaProducerRecord.create(topic, data), this::produceCallback);
        producer.flush();
    }

    public void produceCallback(AsyncResult<RecordMetadata> result) {
        if (result.failed()) {
            LOGGER.error("Event publishing failed", result.cause());
        } else {
            LOGGER.info("Event published {}", result.result());
        }
    }

    public void shutdown() {
        if (producer != null) {
            waitForCompletion(producer.close());
        }
        if (consumer != null) {
            waitForCompletion(consumer.close());
        }
    }

    public void waitForCompletion(Future future) {
        try {
            future.toCompletionStage().toCompletableFuture().get(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                throw new KafkaException(e.getCause());
            }
        }
    }

}