/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.internal.utils;

import java.util.TreeSet;

public class CaseInsensitiveSet extends TreeSet<String> {

    private static final long serialVersionUID = 1L;

    public CaseInsensitiveSet(String... initialContent) {
        super((o1, o2) -> {
            if (o1 == null) {
                if (o2 == null) {
                    return 0;
                } else {
                    return -1;
                }
            } else if (o2 == null) {
                return 1;
            }
            int diff = o1.length() - o2.length();
            for (int i = 0; diff == 0 && i < o1.length(); i++) {
                diff = Character.toLowerCase(o1.charAt(i)) - Character.toLowerCase(o2.charAt(i));
            }
            return diff;

        });
        for (String item : initialContent) {
            add(item);
        }
    }
}
