/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.compiler.canonical;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionUtils {

    private ReflectionUtils() {
    }

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    private static Map<Class<?>, Class<?>> wrappers2Primitive = new ConcurrentHashMap<>();

    static {
        wrappers2Primitive.put(Boolean.class, boolean.class);
        wrappers2Primitive.put(Byte.class, byte.class);
        wrappers2Primitive.put(Character.class, char.class);
        wrappers2Primitive.put(Double.class, double.class);
        wrappers2Primitive.put(Float.class, float.class);
        wrappers2Primitive.put(Integer.class, int.class);
        wrappers2Primitive.put(Long.class, long.class);
        wrappers2Primitive.put(Short.class, short.class);
    }

    public static boolean isWrapper(Class<?> clazz) {
        return wrappers2Primitive.containsKey(clazz);
    }

    public static Class<?> getPrimitive(Class<?> clazz) {
        return wrappers2Primitive.get(clazz);
    }

    public static Method
            getMethod(ClassLoader cl,
                    Class<?> clazz,
                    String methodName,
                    Collection<String> parameterTypes) throws ReflectiveOperationException {
        Class<?>[] methodParameters = new Class[parameterTypes.size()];
        int i = 0;
        for (String parameterType : parameterTypes) {
            if (!parameterType.contains(".")) {
                parameterType = "java.lang." + parameterType;
            }
            methodParameters[i++] = cl.loadClass(parameterType);
        }

        List<Method> candidates = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName) && m.getParameterCount() == methodParameters.length) {
                Class<?>[] thisMethodParams = m.getParameterTypes();
                boolean valid = true;
                boolean potentiallyValid = true;
                for (i = 0; potentiallyValid && i < methodParameters.length; i++) {
                    valid = thisMethodParams[i].isAssignableFrom(methodParameters[i]) ||
                            thisMethodParams[i].isAssignableFrom(wrappers2Primitive.getOrDefault(methodParameters[i], methodParameters[i]));
                    potentiallyValid = valid || methodParameters[i].equals(java.lang.Object.class);
                }
                if (valid) {
                    return m;
                } else if (potentiallyValid) {
                    candidates.add(m);
                }
            }
        }
        if (candidates.size() != 1) {
            throw new NoSuchMethodException(methodName);
        } else {
            return candidates.get(0);
        }
    }

}
