/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kie.kogito.monitoring.elastic.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.micrometer.elastic.ElasticConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ElasticRegistryTest {

    @Test
    public void testElasticMicrometerIsUsingOurProperties() throws InterruptedException {
        ElasticRegistry elasticRegistry = new ElasticRegistry();

        KogitoElasticConfig kogitoElasticConfig = new KogitoElasticConfig();
        kogitoElasticConfig.setProperty(KogitoElasticConfig.HOST_KEY, "http://mylocalhost:8080");
        kogitoElasticConfig.setProperty(KogitoElasticConfig.INDEX_KEY, "myIndex");
        kogitoElasticConfig.setProperty(KogitoElasticConfig.STEP_KEY, "1s");
        kogitoElasticConfig.setProperty(KogitoElasticConfig.TIMESTAMP_FIELD_NAME_KEY, "myTimestampName");
        kogitoElasticConfig.setProperty(KogitoElasticConfig.USERNAME_KEY, "pippo");
        kogitoElasticConfig.setProperty(KogitoElasticConfig.PASSWORD_KEY, "pluto");
        kogitoElasticConfig.setProperty(KogitoElasticConfig.PIPELINE_KEY, "mypipe");
        kogitoElasticConfig.setProperty(KogitoElasticConfig.INDEX_DATE_SEPARATOR_KEY, "/");
        kogitoElasticConfig.setProperty(KogitoElasticConfig.DOCUMENT_TYPE_KEY, "doc");

        Map<String, String> configMap = kogitoElasticConfig.getConfigMap();
        Map<String, CountDownLatch> countDownLatchMap = new HashMap<>();
        configMap.keySet().forEach(x -> countDownLatchMap.put(x, new CountDownLatch(1)));

        ElasticConfig elasticConfig = s -> {
            System.out.println(s);
            countDownLatchMap.computeIfPresent(s, (k, v) -> {
                v.countDown();
                return v;
            });
            return configMap.getOrDefault(s, null);
        };

        elasticRegistry.start(elasticConfig);

        for (CountDownLatch value : countDownLatchMap.values()) {
            Assertions.assertTrue(value.await(20, TimeUnit.SECONDS));
        }
    }
}