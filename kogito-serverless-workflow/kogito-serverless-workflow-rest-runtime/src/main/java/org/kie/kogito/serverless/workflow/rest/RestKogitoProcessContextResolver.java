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
package org.kie.kogito.serverless.workflow.rest;

import java.util.Map;
import java.util.function.Function;

import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.kie.kogito.serverless.workflow.utils.KogitoProcessContextResolverExtension;

import static org.kie.kogito.serverless.workflow.rest.JsonNodeResultHandler.FAIL_ON_STATUS_ERROR;
import static org.kie.kogito.serverless.workflow.rest.JsonNodeResultHandler.RESPONSE_HEADERS;
import static org.kie.kogito.serverless.workflow.rest.JsonNodeResultHandler.RETURN_HEADERS;
import static org.kie.kogito.serverless.workflow.rest.JsonNodeResultHandler.RETURN_STATUS_CODE;
import static org.kie.kogito.serverless.workflow.rest.JsonNodeResultHandler.RETURN_STATUS_MESSAGE;
import static org.kie.kogito.serverless.workflow.rest.JsonNodeResultHandler.STATUS_CODE;
import static org.kie.kogito.serverless.workflow.rest.JsonNodeResultHandler.STATUS_MESSAGE;

public class RestKogitoProcessContextResolver implements KogitoProcessContextResolverExtension {

    @Override
    public Map<String, Function<KogitoProcessContext, Object>> getKogitoProcessContextResolver() {
        return Map.of(
                JsonNodeResultHandler.RETURN_HEADERS, k -> k.getVariable(RETURN_HEADERS),
                JsonNodeResultHandler.RETURN_STATUS_CODE, k -> k.getVariable(RETURN_STATUS_CODE),
                JsonNodeResultHandler.RETURN_STATUS_MESSAGE, k -> k.getVariable(RETURN_STATUS_MESSAGE),
                JsonNodeResultHandler.FAIL_ON_STATUS_ERROR, k -> k.getVariable(FAIL_ON_STATUS_ERROR),
                JsonNodeResultHandler.STATUS_CODE, k -> k.getVariable(STATUS_CODE),
                JsonNodeResultHandler.STATUS_MESSAGE, k -> k.getVariable(STATUS_MESSAGE),
                JsonNodeResultHandler.RESPONSE_HEADERS, k -> k.getVariable(RESPONSE_HEADERS));
    }
}
