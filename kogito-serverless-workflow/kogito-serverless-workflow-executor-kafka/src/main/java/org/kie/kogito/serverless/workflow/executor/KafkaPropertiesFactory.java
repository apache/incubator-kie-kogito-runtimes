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

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.kie.kogito.serverless.workflow.utils.ConfigResolverHolder;

import io.cloudevents.kafka.CloudEventSerializer;

public class KafkaPropertiesFactory {

    private static KafkaPropertiesFactory INSTANCE = new KafkaPropertiesFactory();

    public static KafkaPropertiesFactory get() {
        return INSTANCE;
    }

    private void initCommonProperties(Map<String, Object> map) {
        map.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    }

    public Map<String, Object> getKafkaPublishConfig() {
        Map<String, Object> map = new HashMap<>(ConfigResolverHolder.getConfigResolver().asMap());
        initCommonProperties(map);
        map.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CloudEventSerializer.class);
        map.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return map;
    }

    private KafkaPropertiesFactory() {
    }
}
