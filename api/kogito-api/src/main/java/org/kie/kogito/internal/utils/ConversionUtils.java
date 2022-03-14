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
package org.kie.kogito.internal.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

public class ConversionUtils {

    private ConversionUtils() {
    }

    public static <T> T convert(Object value, Class<T> clazz) {
        return convert(value, clazz, v -> v.toString());
    }

    public static <T> T convert(Object value, Class<T> clazz, Function<Object, String> stringConverter) {
        if (value == null || clazz.isAssignableFrom(value.getClass())) {
            return clazz.cast(value);
        } else {
            for (Method method : clazz.getMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isStatic(modifiers) && method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(String.class) && clazz.isAssignableFrom(method.getReturnType())) {
                    try {
                        return clazz.cast(method.invoke(null, stringConverter.apply(value)));
                    } catch (ReflectiveOperationException e1) {
                    }
                }
            }
            try {
                return clazz.getConstructor(String.class).newInstance(stringConverter.apply(value));
            } catch (ReflectiveOperationException e) {

            }
        }
        throw new IllegalArgumentException(value + " cannot be converted to " + clazz.getName());
    }

}
