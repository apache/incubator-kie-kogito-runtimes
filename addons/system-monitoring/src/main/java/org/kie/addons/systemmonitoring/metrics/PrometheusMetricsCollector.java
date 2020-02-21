package org.kie.addons.systemmonitoring.metrics;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

public class PrometheusMetricsCollector implements IMetricsCollector {
    private static final long NANOSECONDS_PER_MICROSECOND = 1_000_000;

    private static long toMicro(long second) {
        return second * NANOSECONDS_PER_MICROSECOND;
    }

    private static double[] rangeMicro(int start, int end) {
        return IntStream.range(start, end).mapToDouble(l -> toMicro((long) l)).toArray();
    }

    protected static double millisToSeconds(long millis) {
        return millis / 1000.0;
    }

    private static final double[] RULE_TIME_BUCKETS;

    static {
        RULE_TIME_BUCKETS = rangeMicro(1, 10);
    }

    private static final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Histogram> histograms = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Gauge> gauges = new ConcurrentHashMap<>();

    public static Histogram GetHistogram(String name){
        if (!histograms.containsKey(name)){
            Histogram tmp = Histogram.build()
                    .name(name)
                    .help("Api call elapsed execution time")
                    .labelNames("value", "handler")
                    .buckets(RULE_TIME_BUCKETS)
                    .register();
            histograms.put(name, tmp);
        }
        return histograms.get(name);
    }

    public static Counter GetCounter(String name){
        // TODO: improve code placing the registration of the Counter in a @PostConstruct in the endpoint
        if (!counters.containsKey(name)){
            Counter tmp = Counter.build().name(name).help("Total api request count").labelNames("identifier", "handler").register();
            counters.put(name, tmp);
        }
        return counters.get(name);
    }

    public static Gauge GetGauge(String name){
        // TODO: improve code placing the registration of the Gauge in a @PostConstruct in the endpoint
        if (!gauges.containsKey(name)){
            Gauge tmp = Gauge.build().name(name).help("Total api request count").labelNames("identifier", "handler").register();
            gauges.put(name, tmp);
        }
        return gauges.get(name);
    }
 }