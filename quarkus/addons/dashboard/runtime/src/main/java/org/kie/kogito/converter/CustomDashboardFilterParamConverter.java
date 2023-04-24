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

package org.kie.kogito.converter;

import java.util.Collections;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;

import org.kie.kogito.dashboard.model.CustomDashboardFilter;

@Provider
public class CustomDashboardFilterParamConverter implements ParamConverter<CustomDashboardFilter> {
    public CustomDashboardFilter fromString(String names) {
        StringTokenizer stringTokenizer = new StringTokenizer(names, ";");
        return new CustomDashboardFilter(Collections.list(stringTokenizer).stream().map(s -> (String) s).collect(Collectors.toList()));
    }

    public String toString(CustomDashboardFilter names) {
        return names.toString();
    }
}
