package org.jbpm.bpmn2.rule;

import java.util.HashMap;
import java.util.Map;
import org.jbpm.process.core.transformation.JsonResolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dmn.feel.lang.FEELProperty;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionRuleTypeEngineImplTest {

    private static DecisionRuleTypeEngineImpl DECISION_RULE_TYPE_ENGINE;
    private static JsonResolver JSON_RESOLVER;

    @BeforeAll
    static void setup() {
        DECISION_RULE_TYPE_ENGINE = new DecisionRuleTypeEngineImpl();
        JSON_RESOLVER = new JsonResolver();
    }

    @Test
    void getDMNAnnotatedAdjustedMap() {
        Map<String, Object> rsniInputs = new HashMap<>();
        rsniInputs.put("SOMETHING", "true");
        DMNAnnotated dmnAnnotated = new DMNAnnotated("first", "last");
        rsniInputs.put("dmnAnnotated", dmnAnnotated);
        NOTDMNAnnotated  notDMNAnnotated = new NOTDMNAnnotated("first", "last");
        rsniInputs.put("notDMNAnnotated", notDMNAnnotated);
        Map<String, Object> jsonResolvedInputs = JSON_RESOLVER.resolveAll(rsniInputs);
        Map<String, Object> retrieved = DECISION_RULE_TYPE_ENGINE.getDMNAnnotatedAdjustedMap(rsniInputs, jsonResolvedInputs);
        assertThat(retrieved)
                .containsEntry("SOMETHING", "true")
                .containsEntry("dmnAnnotated", dmnAnnotated);
        assertThat(retrieved.get("notDMNAnnotated")).isInstanceOf(Map.class);
    }

    @Test
    void isDMNAnnotatedBean() {
        DMNAnnotated dmnAnnotated = new DMNAnnotated("first", "last");
        assertThat(DECISION_RULE_TYPE_ENGINE.isDMNAnnotatedBean(dmnAnnotated)).isTrue();
        NOTDMNAnnotated  notDMNAnnotated = new NOTDMNAnnotated("first", "last");
        assertThat(DECISION_RULE_TYPE_ENGINE.isDMNAnnotatedBean(notDMNAnnotated)).isFalse();
    }

    @Test
    void isDMNAnnotatedClass() {
        assertThat(DECISION_RULE_TYPE_ENGINE.isDMNAnnotatedClass(DMNAnnotated.class)).isTrue();
        assertThat(DECISION_RULE_TYPE_ENGINE.isDMNAnnotatedClass(NOTDMNAnnotated.class)).isFalse();
    }

    private static class DMNAnnotated {
        private String firstName;
        private String lastName;

        public DMNAnnotated(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @FEELProperty("first name")
        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    private static class NOTDMNAnnotated {
        private String firstName;
        private String lastName;

        public NOTDMNAnnotated(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}