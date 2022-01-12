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
package org.kie.kogito.quarkus.drools;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.kogito.incubation.common.*;
import org.kie.kogito.incubation.rules.services.adapters.RuleUnitInstance;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class StatefulRuleUnitAltAPITest {
    @Inject
    RuleUnitInstance<AnotherService> instance;

    @Test
    void testCreate() {
        assertEquals("/rule-units/org.kie.kogito.quarkus.drools.AnotherService",
                instance.id().ruleUnitId().asLocalUri().path());
    }

    @Test
    void testQuery() {
        AnotherService ctx = instance.context();

        ctx.getStrings().add(new StringHolder("hello folks"));
        ctx.getStrings().add(new StringHolder("hello people"));
        ctx.getStrings().add(new StringHolder("hello Mario"));
        ctx.getStrings().add(new StringHolder("helicopter"));

        Stream<ExtendedDataContext> result = instance.query("Strings", ExtendedReferenceContext.ofData(EmptyDataContext.Instance));
        List<String> strings = result
                .map(e -> e.data().as(MapDataContext.class).get("results", StringHolder.class).getValue())
                .collect(Collectors.toList());

        strings.removeAll(List.of("hello folks", "hello people", "hello Mario"));
        assertTrue(strings.isEmpty());

    }

}
