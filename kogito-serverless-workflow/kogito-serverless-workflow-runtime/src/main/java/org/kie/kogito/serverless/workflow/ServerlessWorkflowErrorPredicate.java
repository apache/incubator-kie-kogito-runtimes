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
package org.kie.kogito.serverless.workflow;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerlessWorkflowErrorPredicate implements Predicate<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(ServerlessWorkflowErrorPredicate.class);

    private final String error;
    private final String code;

    public ServerlessWorkflowErrorPredicate(String error, String code) {
        this.error = error;
        this.code = code;
    }

    @Override
    public boolean test(Throwable t) {
        boolean result = "*".equals(error);
        if (!result && code != null) {
            try {
                result = t.getClass().isAssignableFrom(Class.forName(code));
            } catch (ClassNotFoundException e) {
                logger.warn("Code {} is not a valid class name", code, e);
                // TODO  try to search for similarity between code and exception class name
            }

        }

        // TODO if result is false try to deduce from error 
        return result;
    }

}
