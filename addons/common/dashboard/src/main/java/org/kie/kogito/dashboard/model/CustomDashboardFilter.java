/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.dashboard.model;

import java.util.ArrayList;
import java.util.List;

public class CustomDashboardFilter {

    private final List<String> names;

    public CustomDashboardFilter() {
        this.names = new ArrayList<>();
    }

    public CustomDashboardFilter(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names.addAll(names);
    }

    @Override
    public String toString() {
        return "CustomDashboardFilter{" +
                "names=" + names +
                '}';
    }
}
