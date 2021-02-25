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
package org.kie.kogito.codegen.json;

import java.io.IOException;
import java.util.List;

import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.DataStream;
import org.kie.kogito.rules.SingletonStore;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;

public class KogitoModule extends SimpleModule {

    public KogitoModule() {
        addDefaultSerializers();
        addDefaultDeserializers();
    }

    private void addDefaultSerializers() {
    }

    private void addDefaultDeserializers() {
        addDeserializer(DataStream.class, new DataStreamDeserializer());
        addDeserializer(DataStore.class, new DataStoreDeserializer());
        addDeserializer(SingletonStore.class, new SingletonStoreDeserializer());
    }

    public static class DataStreamDeserializer extends JsonDeserializer<DataStream<?>> implements ContextualDeserializer {

        private CollectionType collectionType;

        @Override
        public DataStream deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            DataStream stream = org.kie.kogito.rules.DataSource.createStream();
            List list = ctxt.readValue(jp, collectionType);
            list.forEach(stream::append);
            return stream;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            CollectionType collectionType = ctxt.getTypeFactory().constructCollectionType(List.class, property.getType().containedType(0));
            DataStreamDeserializer deserializer = new DataStreamDeserializer();
            deserializer.collectionType = collectionType;
            return deserializer;
        }
    }

    public static class DataStoreDeserializer extends JsonDeserializer<DataStore<?>> implements ContextualDeserializer {

        private CollectionType collectionType;

        @Override
        public DataStore deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            DataStore store = org.kie.kogito.rules.DataSource.createStore();
            List list = ctxt.readValue(jp, collectionType);
            list.forEach(store::add);
            return store;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            CollectionType collectionType = ctxt.getTypeFactory().constructCollectionType(List.class, property.getType().containedType(0));
            DataStoreDeserializer deserializer = new DataStoreDeserializer();
            deserializer.collectionType = collectionType;
            return deserializer;
        }
    }

    public static class SingletonStoreDeserializer extends JsonDeserializer<SingletonStore<?>> implements ContextualDeserializer {

        private JavaType javaType;

        @Override
        public SingletonStore deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            SingletonStore store = org.kie.kogito.rules.DataSource.createSingleton();
            store.set(ctxt.readValue(jp, javaType));
            return store;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            JavaType javaType = property.getType().containedType(0);
            SingletonStoreDeserializer deserializer = new SingletonStoreDeserializer();
            deserializer.javaType = javaType;
            return deserializer;
        }
    }
}
