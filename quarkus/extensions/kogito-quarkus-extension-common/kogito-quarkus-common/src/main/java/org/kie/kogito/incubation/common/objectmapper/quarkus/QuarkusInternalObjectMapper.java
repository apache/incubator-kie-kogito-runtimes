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

package org.kie.kogito.incubation.common.objectmapper.quarkus;

import java.util.Map;

import javax.inject.Singleton;

import org.kie.kogito.incubation.common.MapDataContext;
import org.kie.kogito.incubation.common.MapLikeDataContext;
import org.kie.kogito.incubation.common.objectmapper.InternalObjectMapper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class acts both as a CDI singleton and a Service provider
 * It is stateless so it is ok if it is implemented more than once.
 * It bridges CDI to non-CDI classes by using <code>CDI.current().select()</code>
 */
@Singleton
public class QuarkusInternalObjectMapper implements InternalObjectMapper {

    private final ObjectMapper mapper;

    public QuarkusInternalObjectMapper() {
        this.mapper = new ObjectMapper();
        this.mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        this.mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    @Override
    public <T> T convertValue(Object self, Class<T> type) {
        if (type.isInstance(self)) {
            return type.cast(self);
        }
        if (MapLikeDataContext.class == type || MapDataContext.class == type) {
            return (T) MapDataContext.of(mapper.convertValue(self, Map.class));
        }
        return mapper.convertValue(self, type);
    }
}
