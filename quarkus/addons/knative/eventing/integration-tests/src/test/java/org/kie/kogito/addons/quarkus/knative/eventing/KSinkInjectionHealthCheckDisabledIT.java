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
package org.kie.kogito.addons.quarkus.knative.eventing;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.kie.kogito.addons.quarkus.knative.eventing.KSinkInjectionHealthCheck.CONFIG_ALIAS;

@QuarkusIntegrationTest
@TestProfile(KSinkInjectionHealthCheckDisabledProfile.class)
class KSinkInjectionHealthCheckDisabledIT extends AbstractKSinkInjectionHealthCheckIT {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void kSinkInjectionHealthDisabled() {
        assertThat(config.getOptionalValue(CONFIG_ALIAS, Boolean.class)).hasValue(false);
        assertHealthChecks(not(hasItems(hasEntry("name", KSinkInjectionHealthCheck.NAME))));
    }
}
