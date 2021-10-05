package org.kie.kogito.addons.quarkus.k8s;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.fabric8.kubernetes.api.model.Service;

/**
 * Reference of the discovered endpoint
 */
public class Endpoint {

    private List<String> URLs;
    private Map<String, String> headers;

    public static Endpoint fromKubeService(Service service) {
        final Endpoint endpoint = new Endpoint();


        return null;
    }

    public List<String> getURLs() {
        return Collections.unmodifiableList(URLs);
    }

    public void setURLs(List<String> URLs) {
        this.URLs = URLs;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                " URLs='" + URLs + '\'' +
                ", headers=" + headers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Endpoint endpoint = (Endpoint) o;
        return Objects.equals(URLs, endpoint.URLs) && Objects.equals(headers, endpoint.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(URLs, headers);
    }
}
