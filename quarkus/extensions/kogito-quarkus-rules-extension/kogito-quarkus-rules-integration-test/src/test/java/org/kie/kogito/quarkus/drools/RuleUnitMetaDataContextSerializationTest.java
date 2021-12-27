package org.kie.kogito.quarkus.drools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.kie.kogito.incubation.application.ReflectiveAppRoot;
import org.kie.kogito.incubation.common.EmptyDataContext;
import org.kie.kogito.incubation.common.ExtendedDataContext;
import org.kie.kogito.incubation.rules.RuleUnitId;
import org.kie.kogito.incubation.rules.RuleUnitIds;
import org.kie.kogito.incubation.rules.RuleUnitInstanceId;
import org.kie.kogito.incubation.rules.services.contexts.RuleUnitMetaDataContext;

import javax.inject.Inject;

import java.util.Map;

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
