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
package org.kie.kogito.serialization.protobuf.process.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.marshalling.ObjectMarshallingStrategyAcceptor;

public class ClassObjectMarshallingStrategyAcceptor implements ObjectMarshallingStrategyAcceptor {
    private static final String STAR = "*";

    public static final ClassObjectMarshallingStrategyAcceptor DEFAULT = new ClassObjectMarshallingStrategyAcceptor(new String[] { "*.*" });

    private final Map<String, Object> patterns;

    public ClassObjectMarshallingStrategyAcceptor(String[] patterns) {
        this.patterns = new HashMap<String, Object>();
        for (String pattern : patterns) {
            addPattern(pattern);
        }
    }

    public ClassObjectMarshallingStrategyAcceptor() {
        this.patterns = new HashMap<String, Object>();
    }

    private void addPattern(String pattern) {
        addImportStylePatterns(this.patterns, pattern);
    }

    public boolean accept(Object object) {
        return isMatched(this.patterns, object.getClass().getName());
    }

    @Override
    public String toString() {
        return "ClassObjectMarshallingStrategyAcceptor for " + patterns.keySet();
    }

    /**
     * Populates the import style pattern map from give comma delimited string
     */
    public void addImportStylePatterns(Map<String, Object> patterns, String str) {
        if (str == null || "".equals(str.trim())) {
            return;
        }

        String[] items = str.split(" ");
        for (String item : items) {
            String qualifiedNamespace = item.substring(0,
                    item.lastIndexOf('.')).trim();
            String name = item.substring(item.lastIndexOf('.') + 1).trim();
            Object object = patterns.get(qualifiedNamespace);
            if (object == null) {
                if (STAR.equals(name)) {
                    patterns.put(qualifiedNamespace, STAR);
                } else {
                    // create a new list and add it
                    List<String> list = new ArrayList<>();
                    list.add(name);
                    patterns.put(qualifiedNamespace, list);
                }
            } else if (name.equals(STAR)) {
                // if its a STAR now add it anyway, we don't care if it was a STAR or a List before
                patterns.put(qualifiedNamespace, STAR);
            } else {
                // its a list so add it if it doesn't already exist
                List list = (List) object;
                if (!list.contains(name)) {
                    list.add(name);
                }
            }
        }
    }

    /**
     * Determines if a given full qualified class name matches any import style patterns.
     */
    public boolean isMatched(Map<String, Object> patterns,
            String className) {
        // Array [] object class names are "[x", where x is the first letter of the array type
        // -> NO '.' in class name, thus!
        // see http://download.oracle.com/javase/6/docs/api/java/lang/Class.html#getName%28%29
        String qualifiedNamespace = className;
        String name = className;
        if (className.indexOf('.') > 0) {
            qualifiedNamespace = className.substring(0, className.lastIndexOf('.')).trim();
            name = className.substring(className.lastIndexOf('.') + 1).trim();
        } else if (className.indexOf('[') == 0) {
            qualifiedNamespace = className.substring(0, className.lastIndexOf('['));
        }
        Object object = patterns.get(qualifiedNamespace);
        if (object == null) {
            return true;
        } else if (STAR.equals(object)) {
            return false;
        } else if (patterns.containsKey("*")) {
            // for now we assume if the name space is * then we have a catchall *.* pattern
            return true;
        } else {
            List list = (List) object;
            return !list.contains(name);
        }
    }

}
