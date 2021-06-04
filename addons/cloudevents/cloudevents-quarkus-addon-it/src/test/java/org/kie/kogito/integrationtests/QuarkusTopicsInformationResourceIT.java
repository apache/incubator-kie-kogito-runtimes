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

package org.kie.kogito.integrationtests;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.kogito.event.ChannelType;
import org.kie.kogito.event.Topic;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class QuarkusTopicsInformationResourceIT {

    @Test
    public void test() {
        List<Topic> topicList = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/messaging/topics")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath().getList(".", Topic.class);

        assertThat(topicList).hasSize(2).containsExactly(
                new Topic("cloudevents-addon-it-requests", ChannelType.INCOMING),
                new Topic("cloudevents-addon-it-responses", ChannelType.OUTGOING));
    }
}
