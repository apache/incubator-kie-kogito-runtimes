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
package org.kie.kogito.mongodb.utils;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.kie.kogito.mongodb.DocumentMarshallingException;
import org.kie.kogito.mongodb.DocumentUnmarshallingException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DocumentUtils {

    private DocumentUtils() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {

        return MAPPER;
    }

    public static MongoCollection<Document> getCollection(MongoClient mongoClient, String processId, String dbName) {
        CodecRegistry registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry());
        MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName).withCodecRegistry(registry);
        return mongoDatabase.getCollection(processId, Document.class).withCodecRegistry(registry);
    }

    public static byte[] toByteArray(Object object) {
        String json = null;
        try {
            MAPPER.activateDefaultTyping(LaissezFaireSubTypeValidator.instance);
            json = MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new DocumentMarshallingException(e);
        }
        return json.getBytes();
    }

    public static Object fromByteArray(String dataType, byte[] object) {
        try {
            Class<?> loadClass = Thread.currentThread().getContextClassLoader().loadClass(dataType);
            MAPPER.activateDefaultTyping(LaissezFaireSubTypeValidator.instance);
            return MAPPER.readValue(new String(object), loadClass);
        } catch (ClassNotFoundException | JsonProcessingException e) {
            throw new DocumentUnmarshallingException(e);
        }
    }
}
