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

package org.kie.kogito.integrationtests.springboot;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.web.server.LocalServerPort;

public abstract class BaseRestTest {

    @LocalServerPort
    int randomServerPort;

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    static String extractID(String location) {
        return location.substring(location.lastIndexOf("/") + 1);
    }

    @BeforeEach
    void setPort() {
        RestAssured.port = randomServerPort;
    }

}