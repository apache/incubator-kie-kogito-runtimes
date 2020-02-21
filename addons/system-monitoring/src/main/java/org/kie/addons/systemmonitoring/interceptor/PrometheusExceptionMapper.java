package org.kie.addons.systemmonitoring.interceptor;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.kie.addons.systemmonitoring.metrics.PrometheusMetricsCollector;

public class PrometheusExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        PrometheusMetricsCollector.GetCounter("api_http_stacktrace_exceptions").labels(e.getStackTrace()[0].toString(), "internal").inc();
        return Response.serverError().entity(e.getStackTrace()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
