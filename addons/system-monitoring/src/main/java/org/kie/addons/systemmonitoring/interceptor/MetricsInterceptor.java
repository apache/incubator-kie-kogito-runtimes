package org.kie.addons.systemmonitoring.interceptor;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.kie.addons.systemmonitoring.metrics.PrometheusMetricsCollector;

public class MetricsInterceptor implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        System.out.println("Logging status code " + responseContext.getStatusInfo().getStatusCode());
        try{
            PrometheusMetricsCollector.GetCounter("api_http_requests_total").labels("endpoint", requestContext.getUriInfo().getMatchedURIs().get(0)).inc();
            PrometheusMetricsCollector.GetCounter("api_http_response_code").labels(String.valueOf(responseContext.getStatusInfo().getStatusCode()), requestContext.getUriInfo().getMatchedURIs().get(0)).inc();
        }
        catch(Throwable e){
            e.printStackTrace();
        }
        PrometheusMetricsCollector.GetGauge("system_available_processors").labels("value", "totalProcessors").set(Runtime.getRuntime().availableProcessors());
        PrometheusMetricsCollector.GetGauge("system_memory_usage").labels("value", "totalMemory").set(Runtime.getRuntime().totalMemory());
        PrometheusMetricsCollector.GetGauge("system_memory_usage").labels("value", "freeMemory").set(Runtime.getRuntime().freeMemory());
    }
}