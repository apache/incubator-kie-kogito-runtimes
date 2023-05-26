/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.integrationtests.quarkus.knative.eventing;

import javax.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.hamcrest.Matcher;

import static io.restassured.RestAssured.given;

abstract class AbstractKSinkInjectionHealthCheckTest {

    @Inject
    Config config;

    protected void assertHealthChecks(Matcher<?> matcher) {
        given()
                .when()
                .get("/q/health/live")
                .then()
                .body("checks", matcher);
    }
}
