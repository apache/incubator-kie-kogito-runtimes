/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.events.rm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventPublisher;
import org.kie.kogito.events.rm.document.ProcessInstanceDataEventDocument;
import org.kie.kogito.events.rm.document.UserTaskInstanceDataEventDocument;
import org.kie.kogito.events.rm.document.VariableInstanceDataEventDocument;
import org.kie.kogito.services.event.ProcessInstanceDataEvent;
import org.kie.kogito.services.event.UserTaskInstanceDataEvent;
import org.kie.kogito.services.event.VariableInstanceDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.TimeZone;

@Singleton
public class ReactiveMessagingEventPublisher implements EventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(ReactiveMessagingEventPublisher.class);
    private ObjectMapper json = new ObjectMapper();

    private MongoCollection<ProcessInstanceDataEventDocument> processInstanceDataEventMongoCollection;
    private MongoCollection<UserTaskInstanceDataEventDocument> userTaskInstanceDataEventMongoCollection;
    private MongoCollection<VariableInstanceDataEventDocument> variableInstanceDataEventMongoCollection;

    @Inject
    MongoClient mongoClient;

    @Inject
    @ConfigProperty(name = "kogito.events.processinstances.enabled")
    Optional<Boolean> processInstancesEvents;

    @Inject
    @ConfigProperty(name = "kogito.events.usertasks.enabled")
    Optional<Boolean> userTasksEvents;

    @Inject
    @ConfigProperty(name = "kogito.events.variables.enabled")
    Optional<Boolean> variablesEvents;

    @Inject
    @ConfigProperty(name = "kogito.events.database", defaultValue = "kogito-events")
    String eventsDatabaseName;

    @Inject
    @ConfigProperty(name = "kogito.events.processinstances.collection", defaultValue = "kogitoprocessinstancesevents")
    String processInstancesEventsCollection;

    @Inject
    @ConfigProperty(name = "kogito.events.usertasks.collection", defaultValue = "kogitousertaskinstancesevents")
    String userTasksEventsCollection;

    @Inject
    @ConfigProperty(name = "kogito.events.variables.collection", defaultValue = "kogitovariablesevents")
    String variablesEventsCollection;

    @PostConstruct
    public void configure() {
        json.setDateFormat(new StdDateFormat().withColonInTimeZone(true).withTimeZone(TimeZone.getDefault()));

        MongoDatabase mongoDatabase = mongoClient.getDatabase(eventsDatabaseName);
        processInstanceDataEventMongoCollection = mongoDatabase.getCollection(processInstancesEventsCollection, ProcessInstanceDataEventDocument.class);
        userTaskInstanceDataEventMongoCollection = mongoDatabase.getCollection(userTasksEventsCollection, UserTaskInstanceDataEventDocument.class);
        variableInstanceDataEventMongoCollection = mongoDatabase.getCollection(variablesEventsCollection, VariableInstanceDataEventDocument.class);
    }

    @Override
    public void publish(DataEvent<?> event, Object... options) {
        ClientSession clientSession;
        if (options.length > 0 && options[0] instanceof ClientSession) {
            clientSession = (ClientSession) options[0];
        } else {
            clientSession = null;
        }

        if (event.getType().equals("ProcessInstanceEvent") && processInstancesEvents.orElse(true)) {
            if (clientSession != null) {
                processInstanceDataEventMongoCollection.insertOne(clientSession, new ProcessInstanceDataEventDocument((ProcessInstanceDataEvent) event));
                processInstanceDataEventMongoCollection.deleteOne(clientSession, Filters.eq("_id", event.getId()));
            } else {
                processInstanceDataEventMongoCollection.insertOne(new ProcessInstanceDataEventDocument((ProcessInstanceDataEvent) event));
                processInstanceDataEventMongoCollection.deleteOne(Filters.eq("_id", event.getId()));
            }
        } else if (event.getType().equals("UserTaskInstanceEvent") && userTasksEvents.orElse(true)) {
            if (clientSession != null) {
                userTaskInstanceDataEventMongoCollection.insertOne(clientSession, new UserTaskInstanceDataEventDocument((UserTaskInstanceDataEvent) event));
                userTaskInstanceDataEventMongoCollection.deleteOne(clientSession, Filters.eq("_id", event.getId()));
            } else {
                userTaskInstanceDataEventMongoCollection.insertOne(new UserTaskInstanceDataEventDocument((UserTaskInstanceDataEvent) event));
                userTaskInstanceDataEventMongoCollection.deleteOne(Filters.eq("_id", event.getId()));
            }
        } else if (event.getType().equals("VariableInstanceEvent") && variablesEvents.orElse(true)) {
            if (clientSession != null) {
                variableInstanceDataEventMongoCollection.insertOne(clientSession, new VariableInstanceDataEventDocument((VariableInstanceDataEvent) event));
                variableInstanceDataEventMongoCollection.deleteOne(clientSession, Filters.eq("_id", event.getId()));
            } else {
                variableInstanceDataEventMongoCollection.insertOne(new VariableInstanceDataEventDocument((VariableInstanceDataEvent) event));
                variableInstanceDataEventMongoCollection.deleteOne(Filters.eq("_id", event.getId()));
            }
        } else {
            logger.warn("Unknown type of event '{}', ignoring", event.getType());
        }

    }

    @Override
    public void publish(Collection<DataEvent<?>> events, Object... options) {
        for (DataEvent<?> event : events) {
            publish(event, options);
        }
    }
}
