/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tck.junit.extension;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TestClassLoader extends URLClassLoader {

    private final Map<String, byte[]> extraClassDefs;

    public TestClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        this.extraClassDefs = new HashMap<>();


    }

    public void addExtraClasses(Map<String, byte[]> extraClassDefs) {
        for (Entry<String, byte[]> entry : extraClassDefs.entrySet()) {
            this.extraClassDefs.put(entry.getKey().replaceAll("/", ".").replaceFirst("\\.class", ""), entry.getValue());
        }
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] classBytes = this.extraClassDefs.remove(name);
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }

}