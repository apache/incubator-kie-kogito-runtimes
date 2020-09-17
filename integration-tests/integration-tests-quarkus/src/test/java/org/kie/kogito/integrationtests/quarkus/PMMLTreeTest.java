/**
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.integrationtests.quarkus;

import java.util.Map;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.http.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PMMLTreeTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create( JavaArchive.class)
                    .addAsResource("PMMLTree.pmml.test", "src/main/resources/PMMLTree.pmml"));

    @Test
    @SuppressWarnings("unchecked")
    void testWholeModel() {
        String inputData = "{\"temperature\":30.0, \"humidity\":10.0}";
        Object resultVariables =  given()
                .contentType(ContentType.JSON)
                .body(inputData)
                .when()
                .post("/SampleMine")
                .then()
                .statusCode(200)
                .body("correlationId", nullValue())
                .body("segmentationId", nullValue())
                .body("segmentId", nullValue())
                .body("segmentIndex", is(0)) // as JSON is not schema aware, here we assert the RAW string
                .body("resultCode", is("OK"))
                .body("resultObjectName", is("decision"))
                .extract()
                .path("resultVariables");
        assertNotNull(resultVariables);
        assertTrue(resultVariables instanceof Map);
        Map<String, Object> mappedResultVariables = (Map) resultVariables;
        assertTrue(mappedResultVariables.containsKey("decision"));
        assertEquals("sunglasses", mappedResultVariables.get("decision"));
        assertTrue(mappedResultVariables.containsKey("weatherdecision"));
        assertEquals("sunglasses", mappedResultVariables.get("weatherdecision"));
    }
}
