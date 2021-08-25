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
package org.kie.kogito.integrationtests.quarkus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.integrationtests.HelloProcessMessagingClient;
import org.kie.kogito.integrationtests.User;
import org.kie.kogito.testcontainers.quarkus.KafkaQuarkusTestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(KafkaQuarkusTestResource.class)
public class HelloProcessIT {

    @Inject
    HelloProcessMessagingClient processMessagingClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloProcessIT.class);

    @Test
    void testHelloProcessRest() {
    }

    @Test
    void testHelloProcessMessaging() throws Exception {
        CompletableFuture<MapDataContext> evaluate = processMessagingClient.evaluate(new User("Tiago", "Dolphine"));
        MapDataContext response = evaluate.get(50, TimeUnit.SECONDS);
        assertThat(response.get("greetings")).isEqualTo("Hello Tiago Dolphine !");
    }
}
