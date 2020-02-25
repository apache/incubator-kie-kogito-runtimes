package org.kie.addons.systemmonitoring.metrics;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.prometheus.client.Counter;
import jdk.internal.platform.Metrics;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.kogito.dmn.rest.DMNResult;

public class DMNResultMetricsBuilder {

    private static final ConcurrentHashMap<CountersTypesEnum, Counter> counters = new ConcurrentHashMap<>();

    public static Counter GetCounter(){
        return counters.computeIfAbsent(CountersTypesEnum.DECISIONS,
                                 k -> Counter.build().name(MetricsConstants.DECISIONS_NAME)
                                         .help(MetricsConstants.DECISIONS_HELP)
                                         .labelNames(MetricsConstants.DECISION_LABELS).register());
    }

    public static void generateMetrics(String handler, DMNResult dmnResult){
        List<DMNDecisionResult> decisionResults = dmnResult.getDecisionResults();
        for (DMNDecisionResult decision : decisionResults){
            Object result = decision.getResult();
            if (result instanceof String){
                System.out.println(String.format("Got %s result %s", handler, result.toString()));
                GetCounter().labels(result.toString(), decision.getDecisionName()).inc();
            };
        }
    }
}
