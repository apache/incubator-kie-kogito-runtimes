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
package org.kie.kogito.quarkus.dmn;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.devtools.codestarts.quarkus.QuarkusCodestartData.QuarkusDataKey;
import io.quarkus.devtools.testing.codestarts.QuarkusCodestartTest;
import io.quarkus.maven.ArtifactCoords;

import static io.quarkus.devtools.codestarts.quarkus.QuarkusCodestartCatalog.Language.JAVA;

public class KogitoDMNCodeCodestartIT {

    public static String projectVersion() {
        try {
            final Properties properties = new Properties();
            properties.load(KogitoDMNCodeCodestartIT.class.getClassLoader().getResourceAsStream("project.properties"));
            return properties.getProperty("version");
        } catch (Exception e) {
            return "<project.properties version unknown>";
        }
    }

    @RegisterExtension
    public static QuarkusCodestartTest codestartTest = QuarkusCodestartTest.builder()
            .standaloneExtensionCatalog()
            .extension(ArtifactCoords.pom("io.quarkus", "quarkus-resteasy-jackson", null)) // account for KOGITO-5817
            .extension(ArtifactCoords.fromString("org.kie.kogito:kogito-quarkus-decisions:" + projectVersion()))
            .putData(QuarkusDataKey.APP_CONFIG, Map.of("quarkus.http.test-port", "0"))
            .languages(JAVA)
            .build();

    @Test
    void testContent() throws Throwable {
        codestartTest.checkGeneratedTestSource("org.acme.PricingTest");
    }

    @Test
    void testBuild() throws Throwable {
        codestartTest.buildAllProjects();
    }
}
