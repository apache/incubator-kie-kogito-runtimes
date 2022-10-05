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
package org.kie.kogito.spring.drools;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.examples.Hello;
import org.kie.kogito.examples.KogitoSpringbootApplication;
import org.drools.ruleunits.api.RuleUnit;
import org.drools.ruleunits.api.RuleUnitInstance;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
public class HelloTest {

    @Autowired
    RuleUnit<Hello> ruleUnit;

    @Test
    public void testHelloEndpoint() {
        Hello data = new Hello();
        data.getStrings().add("hello");

        RuleUnitInstance<Hello> ruleUnitInstance = ruleUnit.createInstance(data);
        List<Map<String, Object>> results = ruleUnitInstance.executeQuery("hello").toList();

        List<String> stringResults = results.stream()
                .flatMap(entry -> entry.values().stream())
                .map(String.class::cast)
                .collect(Collectors.toList());

        assertThat(stringResults).contains("hello", "world");
    }
}
