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

import java.util.concurrent.CompletionStage;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventEmitter;

public class KafkaEventEmitter implements EventEmitter {

    private final KafkaProducer kafkaProducer;
    private final String topic;

    public KafkaEventEmitter(KafkaProducer kafkaProducer, String topic) {
        this.kafkaProducer = kafkaProducer;
        this.topic = topic;
    }

    @Override
    public CompletionStage<Void> emit(DataEvent<?> dataEvent) {
        // rely on kafka serializer/deserializer 
        return ((KafkaFuture) kafkaProducer.send(new ProducerRecord(topic, dataEvent))).toCompletionStage();
    }

}
