package org.kie.addons.monitoring.system.metrics;

import java.util.concurrent.ConcurrentHashMap;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;

public class SystemMetricsCollector {

    private static final ConcurrentHashMap<CountersTypesEnum, Counter> counters = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<SummaryTypes, Summary> summaries = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<GaugeTypesEnum, Gauge> gauges = new ConcurrentHashMap<>();

    private SystemMetricsCollector(){}

    public static void registerStatusCodeRequest(String handler, String statusCode){
        counters.computeIfAbsent(CountersTypesEnum.REQUESTS_STATUS_CODE,
                                   k -> Counter.build().name(MetricsConstants.STATUS_CODE_NAME)
                                           .help(MetricsConstants.STATUS_CODE_HELP)
                                           .labelNames(MetricsConstants.HANDLER_IDENTIFIER_LABELS).register())
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
                                           .name(MetricsConstants.ELAPSED_TIME_NAME)
                                           .help(MetricsConstants.ELAPSED_TIME_HELP)
                                           .labelNames(MetricsConstants.HANDLER_LABEL)
                                           .register())
                    .labels(handler).observe(elapsedTime);
    }

    public static void registerException(String handler, String stackTrace){
        counters.computeIfAbsent(CountersTypesEnum.EXCEPTIONS,
                                 k -> Counter.build().name(MetricsConstants.EXCEPTIONS_NAME)
                                         .help(MetricsConstants.EXCEPTIONS_HELP)
                                         .labelNames(MetricsConstants.HANDLER_IDENTIFIER_LABELS).register())
                                .labels(handler, stackTrace).inc();
    }

    public static void registerProcessorsSample(int totalProcessors){
        gauges.computeIfAbsent(GaugeTypesEnum.PROCESSORS,
                               k -> Gauge.build().name(MetricsConstants.PROCESSORS_NAME)
                                       .help(MetricsConstants.PROCESSORS_HELP)
                                       .labelNames().register())
                               .labels().set(totalProcessors);
    }

    public static void registerSystemMemorySample(double totalMemory, double freeMemory) {
        Gauge gauge = gauges.computeIfAbsent(GaugeTypesEnum.MEMORY,
                               k -> Gauge.build().name(MetricsConstants.MEMORY_NAME)
                                       .help(MetricsConstants.MEMORY_METRICS_HELP)
                                       .labelNames(MetricsConstants.MEMORY_METRICS_LABEL).register());

        gauge.labels("totalMemory").set(totalMemory);
        gauge.labels("freeMemory").set(freeMemory);
    }
 }