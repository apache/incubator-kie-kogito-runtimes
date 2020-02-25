package org.kie.addons.systemmonitoring.metrics;

public class MetricsConstants {
    public static final String STATUS_CODE_NAME = "api_http_response_code";

    public static final String STATUS_CODE_HELP = "Request status code.";

    public static final String[] STATUS_CODE_LABELS = new String[]{"handler", "identifier"};

    public static final String ELAPSED_TIME_NAME = "api_execution_elapsed_nanosecond";

    public static final String ELAPSED_TIME_HELP = "Endpoint execution elapsed nanoseconds.";

    public static final String EXCEPTIONS_NAME = "api_http_stacktrace_exceptions";

    public static final String EXCEPTIONS_HELP = "System exceptions details";

    public static final String EXCEPTIONS_LABEL = "identifier";

    public static final String PROCESSORS_NAME = "system_available_processors";

    public static final String PROCESSORS_HELP = "System Memory usage";

    public static final String MEMORY_NAME = "system_memory_usage";

    public static final String MEMORY_METRICS_HELP = "System memory usage.";

    public static final String MEMORY_METRICS_LABEL = "type";

    public static final String DECISIONS_NAME = "dmn_result";

    public static final String DECISIONS_HELP = "Decision output.";

    public static final String[] DECISION_LABELS = new String[]{"identifier", "handler"};
}

