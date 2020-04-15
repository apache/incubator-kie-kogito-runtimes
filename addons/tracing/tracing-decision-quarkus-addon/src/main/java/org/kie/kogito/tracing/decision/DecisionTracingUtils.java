package org.kie.kogito.tracing.decision;

import java.util.Map;

import org.kie.dmn.api.core.DMNContext;

import static org.kie.kogito.decision.DecisionModel.EVALUATION_ID_KEY;

public class DecisionTracingUtils {

    public static String extractEvaluationId(Map<String, Object> contextMetadata) {
        return contextMetadata != null && contextMetadata.containsKey(EVALUATION_ID_KEY)
                ? contextMetadata.get(EVALUATION_ID_KEY).toString()
                : null;
    }

    public static String extractEvaluationId(DMNContext context) {
        return extractEvaluationId(context.getMetadata().asMap());
    }

}
