package org.kie.kogito.addons.quarkus.k8s;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;

/**
 * Collection of utilities for Kubernetes API integration
 */
public class KubernetesServiceUtil {

    public static final String SECURE_HTTP_PROTOCOL = "https";
    public static final String NONSECURE_HTTP_PROTOCOL = "http";
    public static final int SECURE_HTTP_PORT = 443;
    public static final int SECURE_NONSTANDARD_HTTP_PORT = 8443;
    /**
     * Protocol, Host, Port
     */
    private static final String FORMAT_SVC_URL = "%s://%s:%s";
    private static final List<Integer> STANDARD_PORTS = new ArrayList<>() {{
        add(SECURE_HTTP_PORT);
        add(SECURE_NONSTANDARD_HTTP_PORT);
        add(80);
        add(8080);
    }};
    public static final String CLUSTER_IP_TYPE = "ClusterIP";

    /**
     * Builds a collection of URLs based on the given {@link Service}.
     * <p/>
     * This method is biased by standard ports, meaning that if the service has exposed one of those (80, 443, and etc.), this is the URL that will be generated.
     * Ports on unconventional ports such as 8775 will have its URL generated only if standards ports are not found.
     * <p/>
     * This is done to facilitate the service call later in the future.
     * <p/>
     * TODO: users should be able to define the target port or at least the behaviour of this use case.
     *
     * @param service the given service to generate the URL
     * @return the collection of URLs created for standard ports
     * @see #STANDARD_PORTS
     */
    public static List<String> buildURLsForStandardPorts(final Service service) {
        final List<String> urls = new ArrayList<>();
        if (service == null || !CLUSTER_IP_TYPE.equals(service.getSpec().getType())) {
            return urls;
        }
        // prefer the standard ports
        urls.addAll(
                service.getSpec().getPorts().stream()
                        .filter(p -> STANDARD_PORTS.contains(p.getPort()))
                        .map(p -> String.format(FORMAT_SVC_URL,
                                                httpProtocolFromPort(p),
                                                service.getSpec().getClusterIP(),
                                                p))
                        .collect(Collectors.toList()));
        if (urls.isEmpty()) {
            urls.addAll(
                    service.getSpec().getPorts().stream()
                            .map(p -> String.format(FORMAT_SVC_URL,
                                                    httpProtocolFromPort(p),
                                                    service.getSpec().getClusterIP(),
                                                    p))
                            .collect(Collectors.toList()));
        }
        return urls;
    }

    public static String httpProtocolFromPort(final ServicePort port) {
        if (port.getPort() == SECURE_HTTP_PORT || port.getPort() == SECURE_NONSTANDARD_HTTP_PORT) {
            return SECURE_HTTP_PROTOCOL;
        }
        return NONSECURE_HTTP_PROTOCOL;
    }
}
