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
package org.kie.kogito.jackson.utils;

import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.cloudevents.jackson.JsonFormat;

public class ObjectMapperFactory {

    private static final Logger logger = Logger.getLogger(ObjectMapperFactory.class.getName());

    private ObjectMapperFactory() {
    }

    private static class DefaultObjectMapper {
        private static ObjectMapper instance;

        static {
            extractedDefaultObjectMethod();
        }

        private static void extractedDefaultObjectMethod() {
            logger.severe("hhhhh DefaultObjectMapper#static -> begin");

            JsonMapper.Builder builder = JsonMapper.builder();
            logger.severe("hhhhh DefaultObjectMapper#static -> 1");

            JsonMapper.Builder enable = builder.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            logger.severe("hhhhh DefaultObjectMapper#static -> 2");

            JsonMapper build = enable.build();
            logger.severe("hhhhh DefaultObjectMapper#static -> 3");

            ObjectMapper objectMapper2 = build.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            logger.severe("hhhhh DefaultObjectMapper#static -> 4");

            ObjectMapper objectMapper1 = objectMapper2.setTypeFactory(TypeFactory.defaultInstance().withClassLoader(Thread.currentThread().getContextClassLoader()));
            logger.severe("hhhhh DefaultObjectMapper#static -> 5");

            ObjectMapper objectMapper = objectMapper1.registerModule(JsonFormat.getCloudEventJacksonModule());
            logger.severe("hhhhh DefaultObjectMapper#static -> 6");

            instance = objectMapper.findAndRegisterModules();
            logger.severe("hhhhh DefaultObjectMapper#static -> 7");

            logger.severe("hhhhh DefaultObjectMapper#static -> end");
        }
    }

    private static class ListenerAwareMapper {
        private static ObjectMapper instance;

        static {
            extracted();
        }

        private static void extracted() {
            logger.severe("hhhhh ListenerAwareMapper#static -> begin");

            ObjectMapper instance1 = DefaultObjectMapper.instance;
            logger.severe("hhhhh ListenerAwareMapper#static -> 1");

            ObjectMapper copy = instance1.copy();
            logger.severe("hhhhh ListenerAwareMapper#static -> 2");

            JsonNodeFactoryListener f = new JsonNodeFactoryListener();
            logger.severe("hhhhh ListenerAwareMapper#static -> 3");

            instance = copy.setNodeFactory(f);
            logger.severe("hhhhh ListenerAwareMapper#static -> 4");

            logger.severe("hhhhh ListenerAwareMapper#static -> end");
        }

    }

    public static ObjectMapper get() {
        return DefaultObjectMapper.instance;
    }

    public static ObjectMapper listenerAware() {
        logger.severe("hhhhh listenerAware");
        return ListenerAwareMapper.instance;
    }
}
