package org.kie.addons.monitoring.system.metrics;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kie.addons.monitoring.system.metrics.dmnhandlers.BigDecimalHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.BooleanHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.StringHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.TypeHandler;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.kogito.codegen.grafana.SupportedDecisionTypes;
import org.kie.kogito.dmn.rest.DMNResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMNResultMetricsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DMNResultMetricsBuilder.class);

    private static final ConcurrentMap<Class, TypeHandler> handlers = generateHandlers();

    private DMNResultMetricsBuilder(){}

    private static ConcurrentMap<Class, TypeHandler> generateHandlers(){
        ConcurrentHashMap<Class, TypeHandler> handlers = new ConcurrentHashMap<>();
        handlers.put(String.class, new StringHandler(SupportedDecisionTypes.fromInternalToStandard(String.class)));
        handlers.put(Boolean.class, new BooleanHandler(SupportedDecisionTypes.fromInternalToStandard(Boolean.class)));
        handlers.put(BigDecimal.class, new BigDecimalHandler(SupportedDecisionTypes.fromInternalToStandard(BigDecimal.class)));
        return handlers;
    }

    public static ConcurrentMap<Class, TypeHandler> getHandlers(){
        return handlers;
    }

    public static void generateMetrics(DMNResult dmnResult){
        if (dmnResult == null){
            LOGGER.warn("DMNResultMetricsBuilder can't register the metrics because the dmn result is null.");
            return;
        }

        List<DMNDecisionResult> decisionResults = dmnResult.getDecisionResults();
        for (DMNDecisionResult decision : decisionResults){
            Object result = decision.getResult();
            if (SupportedDecisionTypes.isSupported(result.getClass())){
                handlers.get(result.getClass()).record(decision.getDecisionName(), result);
            }
        }
    }
}
