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
package org.kogito.workitem.rest.decorators;

import java.util.Map;

import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kogito.workitem.rest.auth.AuthDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.mutiny.ext.web.client.HttpRequest;

import static org.kie.kogito.internal.utils.ConversionUtils.isEmpty;
import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.getParam;

public class TokenPropagationDecorator implements AuthDecorator {

    private static final Logger logger = LoggerFactory.getLogger(TokenPropagationDecorator.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String PROPAGATE_TOKEN_PARAM = "propagateToken";

    @Override
    public void decorate(KogitoWorkItem item, Map<String, Object> parameters, HttpRequest<?> request) {

        String token = getParam(parameters, PROPAGATE_TOKEN_PARAM, String.class, null);

        if (isEmpty(token)) {
            logger.debug("No token found in '{}' parameter, request will be sent without authentication", PROPAGATE_TOKEN_PARAM);
            return;
        }

        if (!token.startsWith(BEARER_PREFIX)) {
            token = BEARER_PREFIX + token;
        }

        request.putHeader(AUTHORIZATION_HEADER, token);
        logger.debug("Token added to request Authorization header from '{}' parameter", PROPAGATE_TOKEN_PARAM);
    }
}
