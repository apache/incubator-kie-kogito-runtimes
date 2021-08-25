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

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InternalFieldResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalFieldResolver.class);

    private final Class<?> type;
    private final Object self;

    InternalFieldResolver(Object self) {
        this.self = self;
        this.type = self.getClass();
    }

    Object resolve(String name) {
        try {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(self);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.trace("Could not find or access field named '" + name + "', continue", e);
        }

        throw new NoSuchElementException("Cannot find " + name);
    }

}
