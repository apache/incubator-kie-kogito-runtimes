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
package org.kie.kogito.serverless.workflow.functions;

import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.process.workitems.impl.WorkItemHandlerParamResolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public abstract class AbstractJsonPathResolver implements WorkItemHandlerParamResolver {

    private static final Configuration jsonPathConfig = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

    private String jsonPathExpr;
    private String paramName;

    protected AbstractJsonPathResolver(String jsonPathExpr, String paramName) {
        this.jsonPathExpr = jsonPathExpr;
        this.paramName = paramName;
    }

    @Override
    public Object apply(KogitoWorkItem workItem) {
        JsonNode node = JsonPath
                .using(jsonPathConfig)
                .parse(workItem.getParameter(paramName))
                .read(jsonPathExpr, JsonNode.class);
        return readValue(node);
    }

    protected abstract Object readValue(JsonNode node);
}
