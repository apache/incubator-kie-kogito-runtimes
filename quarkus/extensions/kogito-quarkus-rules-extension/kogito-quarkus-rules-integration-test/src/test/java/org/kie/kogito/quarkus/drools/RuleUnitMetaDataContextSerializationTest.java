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

import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.kie.kogito.incubation.common.EmptyDataContext;
import org.kie.kogito.incubation.common.ExtendedDataContext;
import org.kie.kogito.incubation.rules.RuleUnitId;
import org.kie.kogito.incubation.rules.RuleUnitIds;
import org.kie.kogito.incubation.rules.services.contexts.RuleUnitMetaDataContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class RuleUnitMetaDataContextSerializationTest {
    @Inject
    ObjectMapper mapper;

    @Inject
    RuleUnitIds ruleUnitRoot;

    @Test
    void ensureRuleUnitMetaDataSerializable() throws JsonProcessingException {
        RuleUnitId id = ruleUnitRoot.get(AnotherService.class);
        String path = id.asLocalUri().path();
        RuleUnitMetaDataContext mdc = RuleUnitMetaDataContext.of(id);
        String out = mapper.writeValueAsString(mdc);
        assertEquals("{\"id\":\"/rule-units/org.kie.kogito.quarkus.drools.AnotherService\"}", out);
        Map m = mapper.convertValue(mdc, Map.class);
        assertEquals(Map.of("id", path), m);
    }

    @Test
    void ensureExtendedMetaDataSerializable() throws JsonProcessingException {
        RuleUnitId id = ruleUnitRoot.get(AnotherService.class);
        RuleUnitMetaDataContext mdc = RuleUnitMetaDataContext.of(id);
        ExtendedDataContext edc = ExtendedDataContext.of(mdc, EmptyDataContext.Instance);
        String out = mapper.writeValueAsString(edc);
        assertEquals("{\"meta\":{\"id\":\"/rule-units/org.kie.kogito.quarkus.drools.AnotherService\"},\"data\":{}}", out);
    }
}
