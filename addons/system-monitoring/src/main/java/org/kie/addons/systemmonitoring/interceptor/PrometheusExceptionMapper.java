package org.kie.addons.systemmonitoring.interceptor;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.kie.addons.systemmonitoring.metrics.SystemMetricsCollector;

public class PrometheusExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        SystemMetricsCollector.RegisterException(e.getStackTrace()[0].toString());
        return Response.serverError().entity(e.getStackTrace()).status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
