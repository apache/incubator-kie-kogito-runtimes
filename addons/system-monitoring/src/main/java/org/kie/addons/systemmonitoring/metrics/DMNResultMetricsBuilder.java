package org.kie.addons.systemmonitoring.metrics;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.prometheus.client.Counter;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.kogito.dmn.rest.DMNResult;

public class DMNResultMetricsBuilder {

    private static final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public static Counter GetCounter(String name){
        if (!counters.containsKey(name)){
            Counter tmp = Counter.build().name(name).help("Decision information").labelNames("identifier", "handler").register();
            counters.put(name, tmp);
        }
        return counters.get(name);
    }

    public static void generateMetrics(String handler, DMNResult result){
        List<DMNDecisionResult> decisionResults = result.getDecisionResults();
        for (DMNDecisionResult decision : decisionResults){
            Object result1 = decision.getResult();
            if (result1 instanceof String){
                System.out.println(String.format("Got %s result %s", handler, result1.toString()));
                GetCounter("dmn_result").labels(result1.toString(), decision.getDecisionName()).inc();
            };
        }
    }
}
