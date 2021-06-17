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
package org.kie.kogito.testcontainers.quarkus;

import java.util.Map;

import org.kie.kogito.resources.ConditionalQuarkusTestResource;
import org.kie.kogito.testcontainers.KogitoKafkaContainer;

import static java.util.Collections.singletonMap;

/**
 * Kafka quarkus resource that works within the test lifecycle.
 *
 */
public class KafkaQuarkusTestResource extends ConditionalQuarkusTestResource<KogitoKafkaContainer> {

    public static final String KOGITO_KAFKA_PROPERTY = "kafka.bootstrap.servers";

    public KafkaQuarkusTestResource() {
        super(new KogitoKafkaContainer());
    }

    @Override
    protected Map<String, String> getProperties() {
        return singletonMap(KOGITO_KAFKA_PROPERTY, "localhost:" + getTestResource().getMappedPort());
    }

    public static class Conditional extends KafkaQuarkusTestResource {

        public Conditional() {
            super();
            enableConditional();
        }
    }
}
