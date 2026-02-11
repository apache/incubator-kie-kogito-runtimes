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
package org.kogito.workitem.rest.auth;

import java.util.Map;

import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.mutiny.ext.web.client.HttpRequest;

import static org.kie.kogito.internal.utils.ConversionUtils.isEmpty;
import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.getParam;

public class BearerTokenAuthDecorator implements AuthDecorator {

    private static final Logger logger = LoggerFactory.getLogger(BearerTokenAuthDecorator.class);
    public static final String BEARER_TOKEN = "accessToken";

    @Override
    public void decorate(KogitoWorkItem item, Map<String, Object> parameters, HttpRequest<?> request) {
        String bearerToken = getParam(parameters, BEARER_TOKEN, String.class, null);

        if (!isEmpty(bearerToken)) {
            logger.debug("Adding bearer token authentication to REST request");
            request.bearerTokenAuthentication(bearerToken);
        } else {
            logger.debug("No bearer token provided for REST request - proceeding without authentication");
        }
    }
}
