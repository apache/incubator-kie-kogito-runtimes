package org.kie.addons.monitoring.system.metrics;

import java.util.concurrent.ConcurrentHashMap;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class SystemMetricsCollector {

    private static final ConcurrentHashMap<CountersTypesEnum, Counter> counters = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<HistogramTypes, Histogram> histograms = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<GaugeTypesEnum, Gauge> gauges = new ConcurrentHashMap<>();

    public static void registerStatusCodeRequest(String handler, String statusCode){
        counters.computeIfAbsent(CountersTypesEnum.REQUESTS_STATUS_CODE,
                                   k -> Counter.build().name(MetricsConstants.STATUS_CODE_NAME)
                                           .help(MetricsConstants.STATUS_CODE_HELP)
                                           .labelNames(MetricsConstants.HANDLER_IDENTIFIER_LABELS).register())
                                  .labels(handler, statusCode).inc();
    }

    public static void registerElapsedTimeSampleMetrics(String handler, double elapsedTime){
        histograms.computeIfAbsent(HistogramTypes.ELAPSED_TIME,
                                   key -> HistogramBuilder.BuildElapsedTimeHistogram(new String[]{"handler"}))
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