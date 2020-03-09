package org.kie.addons.monitoring.system.metrics;

import java.util.concurrent.ConcurrentHashMap;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

public class SystemMetricsCollector {

    private static final String STATUS_CODE_NAME = "api_http_response_code";

    private static final String STATUS_CODE_HELP = "Request status code.";

    private static final String[] HANDLER_LABEL = new String[]{"handler"};

    private static final String[] HANDLER_IDENTIFIER_LABELS = new String[]{"handler", "identifier"};

    private static final String ELAPSED_TIME_NAME = "api_execution_elapsed_nanosecond";

    private static final String ELAPSED_TIME_HELP = "Endpoint execution elapsed nanoseconds, 3 minutes time window.";

    private static final String EXCEPTIONS_NAME = "api_http_stacktrace_exceptions";

    private static final String EXCEPTIONS_HELP = "System exceptions details";

    private static final ConcurrentHashMap<CountersTypesEnum, Counter> counters = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<SummaryTypes, Summary> summaries = new ConcurrentHashMap<>();

    private SystemMetricsCollector(){}

    public static void registerStatusCodeRequest(String handler, String statusCode){
        counters.computeIfAbsent(CountersTypesEnum.REQUESTS_STATUS_CODE,
                                   k -> Counter.build().name(STATUS_CODE_NAME)
                                           .help(STATUS_CODE_HELP)
                                           .labelNames(HANDLER_IDENTIFIER_LABELS).register())
                                  .labels(handler, statusCode).inc();
    }

    public static void registerElapsedTimeSampleMetrics(String handler, double elapsedTime){
        summaries.computeIfAbsent(SummaryTypes.ELAPSED_TIME,
                                   k -> Summary.build() // Calculate quantiles over a sliding window of time - default = 10 minutes
                                           .quantile(0.1, 0.01)   // Add 10th percentile with 5% tolerated error
                                           .quantile(0.25, 0.05)
                                           .quantile(0.50, 0.05)
                                           .quantile(0.75, 0.05)
                                           .quantile(0.9, 0.05)
                                           .quantile(0.99, 0.01)
                                           .maxAgeSeconds(180)
                                           .name(ELAPSED_TIME_NAME)
                                           .help(ELAPSED_TIME_HELP)
                                           .labelNames(HANDLER_LABEL)
                                           .register())
                    .labels(handler).observe(elapsedTime);
    }

    public static void registerException(String handler, String stackTrace){
        counters.computeIfAbsent(CountersTypesEnum.EXCEPTIONS,
                                 k -> Counter.build().name(EXCEPTIONS_NAME)
                                         .help(EXCEPTIONS_HELP)
                                         .labelNames(HANDLER_IDENTIFIER_LABELS).register())
                                .labels(handler, stackTrace).inc();
    }
 }