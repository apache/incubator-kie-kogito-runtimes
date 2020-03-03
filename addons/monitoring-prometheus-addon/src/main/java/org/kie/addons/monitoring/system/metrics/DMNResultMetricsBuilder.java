package org.kie.addons.monitoring.system.metrics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.addons.monitoring.system.metrics.dmnhandlers.BigDecimalHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.BooleanHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.StringHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.TypeHandler;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.kogito.codegen.grafana.SupportedDecisionTypes;
import org.kie.kogito.dmn.rest.DMNResult;

public class DMNResultMetricsBuilder {

    private static final ConcurrentHashMap<Class, TypeHandler> handlers = generateHandlers();

    private static ConcurrentHashMap<Class, TypeHandler> generateHandlers(){
        // TODO: RETHINK THIS
        ConcurrentHashMap<Class, TypeHandler> handlers = new ConcurrentHashMap<>();
        handlers.put(String.class, new StringHandler(SupportedDecisionTypes.fromInternalToStandard(String.class)));
        handlers.put(Boolean.class, new BooleanHandler(SupportedDecisionTypes.fromInternalToStandard(Boolean.class)));
        handlers.put(BigDecimal.class, new BigDecimalHandler(SupportedDecisionTypes.fromInternalToStandard(BigDecimal.class)));
        return handlers;
    }

    public static void generateMetrics(String handler, DMNResult dmnResult){
        List<DMNDecisionResult> decisionResults = dmnResult.getDecisionResults();
        for (DMNDecisionResult decision : decisionResults){
            Object result = decision.getResult();
            if (SupportedDecisionTypes.isSupported(result.getClass())){
                // TODO: remove sop
                System.out.println(String.format("Got %s result %s", handler, result.toString()));
                handlers.get(result.getClass()).record(handler, result);
            };
        }
    }
}
