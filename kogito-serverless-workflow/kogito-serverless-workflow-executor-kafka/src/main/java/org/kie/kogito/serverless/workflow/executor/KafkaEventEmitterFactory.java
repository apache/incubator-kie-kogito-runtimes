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

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.kie.kogito.event.EventEmitter;
import org.kie.kogito.event.EventEmitterFactory;
import org.kie.kogito.serverless.workflow.utils.ConfigResolver;
import org.kie.kogito.serverless.workflow.utils.ConfigResolverHolder;

public class KafkaEventEmitterFactory implements EventEmitterFactory {

    private static final String MAPPING_PREFIX = "kogito.addon.messaging.outgoing.trigger.";
    private Map<String, String> trigger2Topic = new HashMap<>();
    private Map<String, EventEmitter> emitters = new HashMap<>();
    // TODO? support different per chanel serializer/deserializer
    private Producer producer;

    public KafkaEventEmitterFactory() {
        ConfigResolver config = ConfigResolverHolder.getConfigResolver();
        for (String name : config.getPropertyNames()) {
            if (name.startsWith(MAPPING_PREFIX)) {
                trigger2Topic.put(name.substring(MAPPING_PREFIX.length()), config.getConfigProperty(name, String.class).orElseThrow());
            }
        }
    }

    @Override
    public EventEmitter apply(String trigger) {
        return emitters.computeIfAbsent(trigger2Topic.getOrDefault(trigger, trigger), this::createEmitter);
    }

    private EventEmitter createEmitter(String trigger) {
        synchronized (this) {
            if (producer == null) {
                producer = createKafkaProducer();
            }
        }
        return new KafkaEventEmitter(producer, trigger);
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }

    protected Producer createKafkaProducer() {
        return new KafkaProducer(KafkaPropertiesFactory.get().getKafkaPublishConfig());
    }
}
