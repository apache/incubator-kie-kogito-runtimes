/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.addons.quarkus.knative.serving.customfunctions;

import java.net.URI;
import java.util.Map;

import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalog;
import org.kie.kogito.addons.k8s.resource.catalog.KubernetesServiceCatalogKey;
import org.kie.kogito.internal.process.runtime.KogitoWorkItem;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.runtime.KogitoWorkItemManager;
import org.kie.kogito.process.workitem.WorkItemExecutionException;
import org.kogito.workitem.rest.RestWorkItemHandler;

import static org.kie.kogito.addons.k8s.resource.catalog.KubernetesProtocol.KNATIVE;
import static org.kogito.workitem.rest.RestWorkItemHandler.REQUEST_TIMEOUT_IN_MILLIS;

public final class KnativeWorkItemHandler implements KogitoWorkItemHandler {

    protected static final long DEFAULT_REQUEST_TIMEOUT_VALUE = 10_000L;

    public static final String REQUEST_TIMEOUT_PROPERTY_NAME = "kogito.addon.knative-serving.request-timeout";

    public static final String APPLICATION_CLOUDEVENTS_JSON_CHARSET_UTF_8 = "application/cloudevents+json; charset=UTF-8";

    public static final String NAME = "knative";

    public static final String ID = "id";

    public static final String PATH_PROPERTY_NAME = "knative_function_path";

    public static final String SERVICE_PROPERTY_NAME = "knative_function_service";

    public static final String PAYLOAD_FIELDS_PROPERTY_NAME = "knative_function_payload_fields";

    public static final String PAYLOAD_FIELDS_DELIMITER = ";";

    public static final String CLOUDEVENT_SENT_AS_PLAIN_JSON_ERROR_MESSAGE = "A Knative custom function argument cannot be a CloudEvent when the 'asCloudEvent' property are not set to 'true'";

    private final KogitoWorkItemHandler delegate;

    private final KubernetesServiceCatalog kubernetesServiceCatalog;

    private final Long requestTimeoutInMillis;

    public KnativeWorkItemHandler(KogitoWorkItemHandler delegate, KubernetesServiceCatalog kubernetesServiceCatalog) {
        this(delegate, kubernetesServiceCatalog, null);
    }

    public KnativeWorkItemHandler(KogitoWorkItemHandler delegate, KubernetesServiceCatalog kubernetesServiceCatalog,
            Long requestTimeoutInMillis) {
        this.delegate = delegate;
        this.kubernetesServiceCatalog = kubernetesServiceCatalog;
        this.requestTimeoutInMillis = requestTimeoutInMillis != null ? requestTimeoutInMillis : DEFAULT_REQUEST_TIMEOUT_VALUE;
    }

    @Override
    public void executeWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        Map<String, Object> parameters = workItem.getParameters();

        parameters.put(RestWorkItemHandler.URL, getUrl(parameters));
        parameters.put(REQUEST_TIMEOUT_IN_MILLIS, requestTimeoutInMillis);

        delegate.executeWorkItem(workItem, manager);
    }

    private String getUrl(Map<String, Object> parameters) {
        return getServiceAddress(parameters) + parameters.remove(PATH_PROPERTY_NAME);
    }

    private String getServiceAddress(Map<String, Object> parameters) {
        String service = (String) parameters.remove(SERVICE_PROPERTY_NAME);

        return kubernetesServiceCatalog.getServiceAddress(new KubernetesServiceCatalogKey(KNATIVE, service))
                .map(URI::toString)
                .orElseThrow(() -> new WorkItemExecutionException("The Knative service '" + service
                        + "' could not be found."));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void abortWorkItem(KogitoWorkItem workItem, KogitoWorkItemManager manager) {
        delegate.abortWorkItem(workItem, manager);
    }
}
