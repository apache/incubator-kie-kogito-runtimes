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

import javax.annotation.PostConstruct;

import org.kie.kogito.mongodb.transaction.MongoDBTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoClient;

@Component
public class SpringbootMongoDBEventPublisher extends MongoDBEventPublisher {

    @Autowired
    MongoClient mongoClient;

    @Autowired
    MongoDBTransactionManager transactionManager;

    @Value("${kogito.events.processinstances.enabled:true}")
    boolean processInstancesEvents;

    @Value("${kogito.events.usertasks.enabled:true}")
    boolean userTasksEvents;

    @Value("${kogito.events.variables.enabled:true}")
    boolean variablesEvents;

    @Value("${kogito.events.database:kogito-events}")
    String eventsDatabaseName;

    @Value("${kogito.events.processinstances.collection:kogitoprocessinstancesevents}")
    String processInstancesEventsCollection;

    @Value("${kogito.events.usertasks.collection:kogitousertaskinstancesevents}")
    String userTasksEventsCollection;

    @Value("${kogito.events.variables.collection:kogitovariablesevents}")
    String variablesEventsCollection;

    @PostConstruct
    public void setup() {
        super.configure();
    }

    @Override
    protected MongoClient mongoClient() {
        return this.mongoClient;
    }

    @Override
    protected MongoDBTransactionManager transactionManager() {
        return this.transactionManager;
    }

    @Override
    protected boolean processInstancesEvents() {
        return this.processInstancesEvents;
    }

    @Override
    protected boolean userTasksEvents() {
        return this.userTasksEvents;
    }

    @Override
    protected boolean variablesEvents() {
        return this.variablesEvents;
    }

    @Override
    protected String eventsDatabaseName() {
        return this.eventsDatabaseName;
    }

    @Override
    protected String processInstancesEventsCollection() {
        return this.processInstancesEventsCollection;
    }

    @Override
    protected String userTasksEventsCollection() {
        return this.userTasksEventsCollection;
    }

    @Override
    protected String variablesEventsCollection() {
        return this.variablesEventsCollection;
    }
}
