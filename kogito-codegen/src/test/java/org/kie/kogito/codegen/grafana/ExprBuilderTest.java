package org.kie.kogito.codegen.grafana;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.grafana.model.functions.ExprBuilder;
import org.kie.kogito.codegen.grafana.model.functions.GrafanaFunction;
import org.kie.kogito.codegen.grafana.model.functions.IncreaseFunction;
import org.kie.kogito.codegen.grafana.model.functions.SumFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExprBuilderTest {

    @Test
    public void GivenATarget_WhenNullGrafanaFunctionsAreApplied_ThenTheOriginalTargetIsReturned() {
        // Arrange
        String target = "api_test{hanlder=\"hello\"}";

        // Act
        String result = ExprBuilder.apply(target, null);

        // Assert
        assertEquals(target, result);
    }

    @Test
    public void GivenATarget_WhenNoGrafanaFunctionsAreApplied_ThenTheOriginalTargetIsReturned() {
        // Arrange
        String target = "api_test{hanlder=\"hello\"}";

        // Act
        String result = ExprBuilder.apply(target, new HashMap<>());

        // Assert
        assertEquals(target, result);
    }

    @Test
    public void GivenATarget_WhenGrafanaFunctionsAreApplied_ThenTheOriginalTargetIsReturned() {
        // Arrange
        String target = "api_test{hanlder=\"hello\"}";
        HashMap<Integer, GrafanaFunction> map = new HashMap();
        map.put(1, new SumFunction());
        map.put(2, new IncreaseFunction("10m"));
        String expectedResult = "increase(sum(api_test{hanlder=\"hello\"})[10m])";

        // Act
        String result = ExprBuilder.apply(target, map);

        // Assert
        assertEquals(expectedResult, result);
    }
}
