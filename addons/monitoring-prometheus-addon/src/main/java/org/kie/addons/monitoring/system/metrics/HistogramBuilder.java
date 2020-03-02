package org.kie.addons.monitoring.system.metrics;

import java.util.stream.IntStream;

import io.prometheus.client.Histogram;

public class HistogramBuilder {
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

    static Histogram BuildElapsedTimeHistogram(String[] labels){
        return Histogram.build()
                .name(MetricsConstants.ELAPSED_TIME_NAME)
                .help(MetricsConstants.ELAPSED_TIME_HELP)
                .labelNames(labels)
                .buckets(RULE_TIME_BUCKETS)
                .register();
    }
}
