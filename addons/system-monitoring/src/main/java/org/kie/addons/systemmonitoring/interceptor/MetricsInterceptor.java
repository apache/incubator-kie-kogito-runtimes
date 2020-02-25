package org.kie.addons.systemmonitoring.interceptor;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.kie.addons.systemmonitoring.metrics.SystemMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsInterceptor implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsInterceptor.class);

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.debug("Logging status code " + responseContext.getStatusInfo().getStatusCode());
        List<String> matchedUris = requestContext.getUriInfo().getMatchedURIs();

        if (matchedUris.size() != 0){
            SystemMetricsCollector.RegisterStatusCodeRequest(matchedUris.get(0), String.valueOf(responseContext.getStatusInfo().getStatusCode()));
        }
        else // Log the number of requests that did not match any Uri -> 404 not found.
        {
            SystemMetricsCollector.RegisterStatusCodeRequest("", String.valueOf(responseContext.getStatusInfo().getStatusCode()));
        }

        SystemMetricsCollector.RegisterSystemMemorySample(Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory());
        SystemMetricsCollector.RegisterProcessorsSample(Runtime.getRuntime().availableProcessors());
    }
}