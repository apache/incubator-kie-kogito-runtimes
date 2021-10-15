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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.jackson.ObjectMapperCustomizer;

@Singleton
public class QuarkusObjectMapperCustomizer implements ObjectMapperCustomizer {
    private ObjectMapper objectMapper;

    @Override
    public void customize(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T convertValue(Object self, Class<T> type) {
        if (MapLikeDataContext.class == type || MapDataContext.class == type) {
            return (T) MapDataContext.of(objectMapper.convertValue(self, Map.class));
        }
        return objectMapper.convertValue(self, type);
    }

}
