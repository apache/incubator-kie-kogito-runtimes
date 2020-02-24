package org.kie.addons.systemmonitoring.metrics;

import java.util.concurrent.ConcurrentHashMap;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class SystemMetricsCollector implements IMetricsCollector {

    private static final ConcurrentHashMap<CountersTypesEnum, Counter> counters = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<HistogramTypes, Histogram> histograms = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<GaugeTypesEnum, Gauge> gauges = new ConcurrentHashMap<>();

    public static void RegisterStatusCodeRequest(String handler, String statusCode){
        Counter counter;
        if (!counters.containsKey(CountersTypesEnum.REQUESTS_STATUS_CODE)){
            counter = Counter.build().name("api_http_response_code").help("Request status code.").labelNames("handler", "identifier").register();
            counters.put(CountersTypesEnum.REQUESTS_STATUS_CODE, counter);
        }
        else{
            counter = counters.get(CountersTypesEnum.REQUESTS_STATUS_CODE);
        }

        counter.labels(handler, statusCode).inc();
    }

    public static void RegisterElapsedTimeSampleMetrics(String handler, double elapsedTime){
        Histogram hist;
        if (!histograms.containsKey(HistogramTypes.ELAPSED_TIME)){
            hist = HistogramBuilder.BuildElapsedTimeHistogram(new String[]{"handler"});
            histograms.put(HistogramTypes.ELAPSED_TIME, hist);
        }
        else{
            hist = histograms.get(HistogramTypes.ELAPSED_TIME);
        }

        hist.labels(handler).observe(elapsedTime);
    }

    public static void RegisterException(String stackTrace){
        Counter counter;
        if (!counters.containsKey(CountersTypesEnum.EXCEPTIONS)){
            counter = Counter.build().name("api_http_stacktrace_exceptions").help("System exceptions details").labelNames("identifier").register();
            counters.put(CountersTypesEnum.EXCEPTIONS, counter);
        }
        else{
            counter = counters.get(CountersTypesEnum.EXCEPTIONS);
        }

        counter.labels(stackTrace).inc();
    }

    public static void RegisterProcessorsSample(int totalProcessors){
        Gauge gauge;
        if (!gauges.containsKey(GaugeTypesEnum.PROCESSORS)) {
            gauge = Gauge.build().name("system_available_processors").help("System Memory usage").labelNames().register();
            gauges.put(GaugeTypesEnum.PROCESSORS, gauge);
        } else {
            gauge = gauges.get(GaugeTypesEnum.PROCESSORS);
        }

        gauge.labels().set(totalProcessors);
    }


    public static void RegisterSystemMemorySample(double totalMemory, double freeMemory) {
        Gauge gauge;
        if (!gauges.containsKey(GaugeTypesEnum.MEMORY)) {
            gauge = Gauge.build().name("system_memory_usage").help("System Memory usage").labelNames("type").register();
            gauges.put(GaugeTypesEnum.MEMORY, gauge);
        } else {
            gauge = gauges.get(GaugeTypesEnum.MEMORY);
        }

        gauge.labels("totalMemory").set(totalMemory);
        gauge.labels("freeMemory").set(freeMemory);
    }
 }