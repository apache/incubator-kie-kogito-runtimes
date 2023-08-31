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
package org.kie.kogito;

import java.lang.reflect.Field;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class Models {

    private static final ObjectMapper mapper = JsonMapper.builder()
            .disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS, MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_IS_GETTERS)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .build();

    private Models() {
    }

    public static Map<String, Object> toMap(Object m) {
        return mapper.convertValue(m, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <T> T fromMap(T m, String id, Map<String, Object> map) {
        setId(m, id);
        return fromMap(m, map);
    }

    public static <T> T fromMap(T m, Map<String, Object> map) {
        for (Field field : m.getClass().getDeclaredFields()) {
            JsonProperty jsonAnnotation = field.getAnnotation(JsonProperty.class);
            if (jsonAnnotation != null) {
                String name = jsonAnnotation.value();
                if (map.containsKey(name)) {
                    field.setAccessible(true);
                    try {
                        field.set(m, map.get(name));
                    } catch (ReflectiveOperationException e) {
                        throw new ReflectiveModelAccessException(e);
                    }
                }
            }
        }
        return m;
    }

    public static <T> T fromMap(Class<T> cls, Map<String, Object> map) {
        return mapper.convertValue(map, cls);
    }

    public static void setId(Object m, String id) {
        try {
            m.getClass().getMethod("setId", String.class).invoke(m, id);
        } catch (ReflectiveOperationException e) {
            throw new ReflectiveModelAccessException(e);
        }
    }
}
