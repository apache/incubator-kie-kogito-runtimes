/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.integrationtests;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class HelloService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloService.class);

    public String hello(User user) {
        logMethodCall("hello", user);
        return String.format("Hello %s %s !", user.getFirstName(), user.getLastName());
    }

    private static void logMethodCall(String method, Object... arguments) {
        LOGGER.info("HelloService.{} invoked with params: {}", method, arguments);
    }
}
