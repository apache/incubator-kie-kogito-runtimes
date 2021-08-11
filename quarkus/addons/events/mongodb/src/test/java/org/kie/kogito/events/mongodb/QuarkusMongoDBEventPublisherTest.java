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

package org.kie.kogito.events.mongodb;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.kogito.mongodb.transaction.MongoDBTransactionManager;

import com.mongodb.client.MongoClient;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@QuarkusTest
class QuarkusMongoDBEventPublisherTest {

    @Inject
    QuarkusMongoDBEventPublisher publisher;

    @InjectSpy
    MongoClient mongoClient;

    @Inject
    MongoDBTransactionManager transactionManager;

    @Test
    void setup() {
        publisher.setup();
        verify(mongoClient, atLeastOnce()).getDatabase(eq("testDB"));
    }

    @Test
    void mongoClient() {
        assertEquals(mongoClient.getDatabase("testDB"), publisher.mongoClient().getDatabase("testDB"));
    }

    @Test
    void transactionManager() {
        assertEquals(transactionManager, publisher.transactionManager());
    }

    @Test
    void processInstancesEvents() {
        assertFalse(publisher.processInstancesEvents());
    }

    @Test
    void userTasksEvents() {
        assertFalse(publisher.userTasksEvents());
    }

    @Test
    void variablesEvents() {
        assertFalse(publisher.variablesEvents());
    }

    @Test
    void eventsDatabaseName() {
        assertEquals("testDB", publisher.eventsDatabaseName());
    }

    @Test
    void processInstancesEventsCollection() {
        assertEquals("testPICollection", publisher.processInstancesEventsCollection());
    }

    @Test
    void userTasksEventsCollection() {
        assertEquals("testUTCollection", publisher.userTasksEventsCollection());
    }

    @Test
    void variablesEventsCollection() {
        assertEquals("testVCollection", publisher.variablesEventsCollection());
    }
}
