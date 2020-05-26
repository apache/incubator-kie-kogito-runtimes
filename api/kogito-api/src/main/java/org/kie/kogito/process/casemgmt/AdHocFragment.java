/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.process.casemgmt;

import java.io.Serializable;

import org.kie.api.definition.process.Node;

public class AdHocFragment implements Serializable {

    private final String type;
    private final String name;

    public AdHocFragment(Node node) {
        this(node.getClass().getSimpleName(), node.getName());
    }

    public AdHocFragment(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AdHocFragment{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
