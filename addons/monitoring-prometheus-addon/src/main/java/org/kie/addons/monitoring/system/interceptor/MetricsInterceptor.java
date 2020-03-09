package org.kie.addons.monitoring.system.interceptor;

import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.kie.addons.monitoring.system.metrics.SystemMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsInterceptor implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsInterceptor.class);

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        List<String> matchedUris = requestContext.getUriInfo().getMatchedURIs();
        if (!matchedUris.isEmpty()){
            SystemMetricsCollector.registerStatusCodeRequest(matchedUris.get(0), String.valueOf(responseContext.getStatusInfo().getStatusCode()));
        }
        else // Log the number of requests that did not match any Uri -> 404 not found.
        {
            SystemMetricsCollector.registerStatusCodeRequest("NOT FOUND", String.valueOf(responseContext.getStatusInfo().getStatusCode()));
        }
    }
}