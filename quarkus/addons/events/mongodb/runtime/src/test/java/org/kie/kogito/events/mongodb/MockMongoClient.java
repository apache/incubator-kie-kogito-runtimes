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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.connection.ClusterDescription;

import io.quarkus.test.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Mock
@ApplicationScoped
public class MockMongoClient implements MongoClient {

    private MongoDatabase mongoDatabase = mock(MongoDatabase.class);

    @Override
    public MongoDatabase getDatabase(String databaseName) {
        MongoCollection mongoCollection = mock(MongoCollection.class);
        when(mongoDatabase.withCodecRegistry(any())).thenReturn(mongoDatabase);
        when(mongoDatabase.getCollection(any(), any())).thenReturn(mongoCollection);
        when(mongoCollection.withCodecRegistry(any())).thenReturn(mongoCollection);
        return mongoDatabase;
    }

    @Override
    public ClientSession startSession() {
        return null;
    }

    @Override
    public ClientSession startSession(ClientSessionOptions options) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public MongoIterable<String> listDatabaseNames() {
        return null;
    }

    @Override
    public MongoIterable<String> listDatabaseNames(ClientSession clientSession) {
        return null;
    }

    @Override
    public ListDatabasesIterable<Document> listDatabases() {
        return null;
    }

    @Override
    public ListDatabasesIterable<Document> listDatabases(ClientSession clientSession) {
        return null;
    }

    @Override
    public <TResult> ListDatabasesIterable<TResult> listDatabases(Class<TResult> tResultClass) {
        return null;
    }

    @Override
    public <TResult> ListDatabasesIterable<TResult> listDatabases(ClientSession clientSession, Class<TResult> tResultClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch() {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> tResultClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline, Class<TResult> tResultClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, Class<TResult> tResultClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession, List<? extends Bson> pipeline) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, List<? extends Bson> pipeline, Class<TResult> tResultClass) {
        return null;
    }

    @Override
    public ClusterDescription getClusterDescription() {
        return null;
    }
}
