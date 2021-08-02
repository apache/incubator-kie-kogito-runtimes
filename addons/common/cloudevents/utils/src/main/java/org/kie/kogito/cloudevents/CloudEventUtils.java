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
package org.kie.kogito.cloudevents;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.Extension;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;

public class CloudEventUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CloudEventUtils.class);

    public static final String UNKNOWN_SOURCE_URI_STRING = urlEncodedStringFrom("__UNKNOWN_SOURCE__")
            .orElseThrow(IllegalStateException::new);

    public static <E> Optional<CloudEvent> build(String id, URI source, E data, Class<E> dataType) {
        return build(id, source, dataType.getName(), null, data);
    }

    public static Optional<CloudEvent> build(String id, URI source, String type, String subject, Object data, Extension... extensions) {
        try {
            byte[] bytes = Mapper.mapper().writeValueAsBytes(data);

            CloudEventBuilder builder = CloudEventBuilder.v1()
                    .withId(id)
                    .withSource(source)
                    .withType(type)
                    .withData(bytes);

            if (subject != null) {
                builder.withSubject(subject);
            }

            if (extensions != null) {
                for (Extension extension : extensions) {
                    builder.withExtension(extension);
                }
            }

            return Optional.of(builder.build());
        } catch (JsonProcessingException e) {
            LOG.error("Unable to serialize CloudEvent data", e);
            return Optional.empty();
        }
    }

    public static Optional<String> encode(CloudEvent event) {
        try {
            return Optional.of(Mapper.mapper().writeValueAsString(event));
        } catch (JsonProcessingException e) {
            LOG.error("Unable to encode CloudEvent", e);
            return Optional.empty();
        }
    }

    public static Optional<Map<String, Object>> toDataEvent(CloudEvent event) {
        ObjectMapper mapper = Mapper.mapper();
        try {
            return Optional.of(mapper.readValue(mapper.writeValueAsBytes(event), Map.class));
        } catch (IOException e) {
            LOG.error("Unable to encode CloudEvent", e);
            return Optional.empty();
        }
    }

    public static Optional<CloudEvent> decode(String json) {
        try {
            return Optional.of(Mapper.mapper().readValue(json, CloudEvent.class));
        } catch (JsonProcessingException e) {
            LOG.error("Unable to decode CloudEvent", e);
            return Optional.empty();
        }
    }

    public static <T> Optional<T> decodeData(CloudEvent event, Class<T> dataClass) {
        try {
            return Optional.ofNullable(Mapper.mapper().readValue(event.getData(), dataClass));
        } catch (IOException e) {
            LOG.error("Unable to decode CloudEvent data to " + dataClass.getName(), e);
            return Optional.empty();
        }
    }

    public static <K, V> Optional<Map<K, V>> decodeMapData(CloudEvent event, Class<K> keyClass, Class<V> valueClass) {
        try {
            JavaType mapType = Mapper.mapper().getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
            return Optional.ofNullable(Mapper.mapper().readValue(event.getData(), mapType));
        } catch (IOException e) {
            LOG.error("Unable to decode CloudEvent data to Map<" + keyClass.getName() + "," + valueClass.getName() + ">", e);
            return Optional.empty();
        }
    }

    public static Optional<String> urlEncodedStringFrom(String input) {
        return Optional.ofNullable(input)
                .map(i -> {
                    try {
                        return URLEncoder.encode(i, StandardCharsets.UTF_8.toString());
                    } catch (UnsupportedEncodingException e) {
                        LOG.error("Unable to URL-encode string \"" + i + "\"", e);
                        return null;
                    }
                });
    }

    public static Optional<URI> urlEncodedURIFrom(String input) {
        return urlEncodedStringFrom(input)
                .map(encodedInput -> {
                    try {
                        return URI.create(encodedInput);
                    } catch (IllegalArgumentException e) {
                        LOG.error("Unable to create URI from string \"" + encodedInput + "\"", e);
                        return null;
                    }
                });
    }

    public static URI buildDecisionSource(String serviceUrl) {
        return buildDecisionSource(serviceUrl, null, null);
    }

    public static URI buildDecisionSource(String serviceUrl, String decisionModelName) {
        return buildDecisionSource(serviceUrl, decisionModelName, null);
    }

    public static URI buildDecisionSource(String serviceUrl, String decisionModelName, String decisionServiceName) {
        String modelChunk = Optional.ofNullable(decisionModelName)
                .filter(s -> !s.isEmpty())
                .flatMap(CloudEventUtils::urlEncodedStringFrom)
                .orElse(null);

        String decisionChunk = Optional.ofNullable(decisionServiceName)
                .filter(s -> !s.isEmpty())
                .flatMap(CloudEventUtils::urlEncodedStringFrom)
                .orElse(null);

        String fullUrl = Stream.of(serviceUrl, modelChunk, decisionChunk)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining("/"));

        return URI.create(Optional.of(fullUrl)
                .filter(s -> !s.isEmpty())
                .orElse(UNKNOWN_SOURCE_URI_STRING));
    }

    // This trick allows to inject a mocked ObjectMapper in the unit tests via Mockito#mockStatic
    public static class Mapper {

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(JsonFormat.getCloudEventJacksonModule());

        public static ObjectMapper mapper() {
            return OBJECT_MAPPER;
        }

        private Mapper() {
            throw new IllegalStateException("Instantiation of utility class CloudEventUtils.Mapper is forbidden");
        }
    }

    private CloudEventUtils() {
        throw new IllegalStateException("Instantiation of utility class CloudEventUtils is forbidden");
    }
}
