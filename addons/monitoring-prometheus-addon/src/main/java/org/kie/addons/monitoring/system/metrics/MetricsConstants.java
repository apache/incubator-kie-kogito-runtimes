package org.kie.addons.monitoring.system.metrics;

public class MetricsConstants {

    private MetricsConstants(){}

    public static final String STATUS_CODE_NAME = "api_http_response_code";

    public static final String STATUS_CODE_HELP = "Request status code.";

    public static final String[] HANDLER_LABEL = new String[]{"handler"};

    public static final String[] HANDLER_IDENTIFIER_LABELS = new String[]{"handler", "identifier"};

    public static final String ELAPSED_TIME_NAME = "api_execution_elapsed_nanosecond";

    public static final String ELAPSED_TIME_HELP = "Endpoint execution elapsed nanoseconds, 3 minutes time window.";

    public static final String EXCEPTIONS_NAME = "api_http_stacktrace_exceptions";

    public static final String EXCEPTIONS_HELP = "System exceptions details";

    public static final String PROCESSORS_NAME = "system_available_processors";

    public static final String PROCESSORS_HELP = "System Memory usage";

    public static final String MEMORY_NAME = "system_memory_usage";

    public static final String MEMORY_METRICS_HELP = "System memory usage.";

    public static final String MEMORY_METRICS_LABEL = "type";

    public static final String DECISIONS_NAME_SUFFIX = "_dmn_result";

    public static final String DECISIONS_HELP = "Decision output.";
}

