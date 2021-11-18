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

package org.kie.kogito.incubation.common;

import java.util.Map;

import org.kie.kogito.incubation.common.objectmapper.InternalObjectMapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyObjectMapper implements InternalObjectMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public <T> T convertValue(Object self, Class<T> type) {
        if (type.isInstance(self)) {
            return type.cast(self);
        }
        if (MapLikeDataContext.class == type || MapDataContext.class == type) {
            return (T) MapDataContext.of(objectMapper.convertValue(self, Map.class));
        }
        if (ExtendedDataContext.class == type) {
            if (self instanceof DataContext) {
                return (T) ExtendedDataContext.ofData((DataContext) self);
            } else {
                return (T) ExtendedDataContext.ofData(convertValue(self, MapDataContext.class));
            }
        }
        return objectMapper.convertValue(self, type);
    }
}
