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

    private final String name;
    private final String type;

    public AdHocFragment(Node node) {
        this.name = node.getName();
        this.type = node.getClass().getSimpleName();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AdHocFragment{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
