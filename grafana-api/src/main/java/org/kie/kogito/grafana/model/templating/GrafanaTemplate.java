/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.grafana.model.templating;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrafanaTemplate {

    @JsonProperty("allFormat")
    public String allFormat;

    @JsonProperty("current")
    public GrafanaTemplateCurrent current;

    @JsonProperty("datasource")
    public String datasource;

    @JsonProperty("includeAll")
    public boolean includeAll;

    @JsonProperty("name")
    public String name;

    @JsonProperty("options")
    public List<GrafanaTemplateOption> options;

    @JsonProperty("query")
    public String query;

    @JsonProperty("refresh")
    public String refresh;

    @JsonProperty("type")
    public String type;
}