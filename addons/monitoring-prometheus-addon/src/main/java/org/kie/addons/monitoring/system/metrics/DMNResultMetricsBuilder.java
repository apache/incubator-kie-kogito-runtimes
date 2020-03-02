package org.kie.addons.monitoring.system.metrics;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.addons.monitoring.system.metrics.dmnhandlers.BooleanHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.DoubleHandler;
import org.kie.addons.monitoring.system.metrics.dmnhandlers.IntegerHandler;
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
        handlers.put(String.class, new StringHandler(String.class));
        handlers.put(Boolean.class, new BooleanHandler(Boolean.class));
        handlers.put(Integer.class, new IntegerHandler(Integer.class));
        handlers.put(Double.class, new DoubleHandler(Double.class));
        handlers.put(boolean.class, new BooleanHandler(boolean.class));
        handlers.put(int.class, new IntegerHandler(int.class));
        handlers.put(double.class, new DoubleHandler(double.class));

        return handlers;
    }

    public static void generateMetrics(String handler, DMNResult dmnResult){
        List<DMNDecisionResult> decisionResults = dmnResult.getDecisionResults();
        for (DMNDecisionResult decision : decisionResults){
            Object result = decision.getResult();
            System.out.println(result.getClass().toString());
            if (SupportedDecisionTypes.isSupported(result.getClass())){
                // TODO: remove sop
                System.out.println(String.format("Got %s result %s", handler, result.toString()));
                handlers.get(result.getClass()).record(handler, result);
            };
        }
    }
}
