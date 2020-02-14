package org.kie.addons.systemmonitoring.rest;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

@Path("/system-metrics")
public class MetricsResource {

    public static CollectorRegistry prometheusRegistry = CollectorRegistry.defaultRegistry;

    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response getMetrics() {
        Enumeration<Collector.MetricFamilySamples> mfs = prometheusRegistry.metricFamilySamples();

        StreamingOutput stream = os -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(os));
            TextFormat.write004(writer, mfs);
            writer.flush();
        };

        return Response.ok(stream).build();

    }
}