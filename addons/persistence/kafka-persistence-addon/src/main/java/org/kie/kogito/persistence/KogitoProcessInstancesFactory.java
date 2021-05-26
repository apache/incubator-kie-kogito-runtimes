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
package org.kie.kogito.persistence;

import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.kie.kogito.persistence.kafka.KafkaProcessInstances;
import org.kie.kogito.persistence.kafka.KafkaStreamsStateListener;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstancesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class must always have exact FQCN as <code>org.kie.kogito.persistence.KogitoProcessInstancesFactory</code>
 */
public abstract class KogitoProcessInstancesFactory implements ProcessInstancesFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(KogitoProcessInstancesFactory.class);

    KafkaStreamsStateListener stateListener;
    KafkaProducer<String, byte[]> producer;

    @Inject
    public void setStateListener(KafkaStreamsStateListener stateListener) {
        this.stateListener = stateListener;
    }

    @Inject
    @Named("default-kafka-broker")
    public void setKafkaConfig(Map<String, Object> kafkaConfig) {
        producer = new KafkaProducer<>(kafkaConfig, new StringSerializer(), new ByteArraySerializer());
    }

    @PreDestroy
    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }

    public KafkaProcessInstances createProcessInstances(Process process) {
        try {
            LOGGER.info("Creating KafkaProcessInstances for process: {}", process.id());
            KafkaProcessInstances pi = new KafkaProcessInstances(process, producer);
            stateListener.addProcessInstances(pi);
            return pi;
        } catch (Exception ex) {
            LOGGER.error("Error creating KafkaProcessInstances for process: {}", process.id(), ex);
            throw new RuntimeException("Error creating KafkaProcessInstances for process: " + process.id(), ex);
        }
    }

}
