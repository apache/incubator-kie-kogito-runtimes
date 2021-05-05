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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Models {
    private Models() {
    }

    public static Map<String, Object> toMap(Object m) {
        try {
            Map<String, Object> map = new HashMap<>();
            BeanInfo beanInfo = Introspector.getBeanInfo(m.getClass());
            Map<String, PropertyDescriptor> descriptors = descriptorMap(beanInfo);

            for (Map.Entry<String, PropertyDescriptor> e : descriptors.entrySet()) {
                String k = e.getKey();
                if (k.equals("class") || k.equals("id")) {
                    continue;
                }
                if (e.getKey().startsWith("v$")) {
                    k = k.substring(2);
                }
                map.put(k, e.getValue().getReadMethod().invoke(m));
            }
            return map;
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T fromMap(T m, Map<String, Object> map) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(m.getClass());
            Map<String, PropertyDescriptor> descriptors = descriptorMap(beanInfo);

            for (Map.Entry<String, PropertyDescriptor> e : descriptors.entrySet()) {
                String k = e.getKey();
                if (e.getKey().startsWith("v$")) {
                    k = k.substring(2);
                }
                if (map.containsKey(k)) {
                    e.getValue().getWriteMethod().invoke(m, map.get(k));
                }
            }
            return m;
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T fromMap(Class<T> cls, Map<String, Object> map) {
        try {
            Constructor<T> constructor = cls.getConstructor();
            T t = constructor.newInstance();
            fromMap(t, map);
            return t;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void setId(Object m, String id) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(m.getClass());
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (pd.getName().equals("id")) {
                    pd.getWriteMethod().invoke(m, id);
                }
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public static <I, O> O convert(I in, O out) {
        fromMap(out, toMap(in));
        return out;
    }

    private static Map<String, PropertyDescriptor> descriptorMap(BeanInfo beanInfo) {
        return Arrays.stream(beanInfo.getPropertyDescriptors())
                .filter(pd -> !pd.getName().equals("class"))
                .collect(Collectors.toMap(
                        PropertyDescriptor::getName,
                        Function.identity()));
    }
}
