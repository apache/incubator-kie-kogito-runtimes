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

package org.kie.kogito.jobs.knative.eventing.quarkus;

import java.net.URI;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.kie.kogito.jobs.api.event.JobCloudEvent;
import org.kie.kogito.jobs.messaging.quarkus.AbstractReactiveMessagingJobsService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.reactivemessaging.http.runtime.OutgoingHttpMetadata;

@ApplicationScoped
public class KnativeEventingJobsService extends AbstractReactiveMessagingJobsService {

    static final String CLOUD_EVENTS_CONTENT_TYPE = "application/cloudevents+json";
    private static final String KOGITO_ADDON = "jobs-knative-eventing";

    /**
     * Metadata to include the content-type for structured CloudEvents messages
     */
    static final Supplier<OutgoingHttpMetadata> OUTGOING_HTTP_METADATA = () -> new OutgoingHttpMetadata.Builder()
            .addHeader(HttpHeaders.CONTENT_TYPE, CLOUD_EVENTS_CONTENT_TYPE)
            .build();

    @Inject
    public KnativeEventingJobsService(
            @ConfigProperty(name = "kogito.service.url") URI serviceUrl,
            ObjectMapper objectMapper,
            @Channel(KOGITO_JOB_SERVICE_JOB_REQUEST_EVENTS_CHANNEL) Emitter<String> eventsEmitter) {
        super(serviceUrl, objectMapper, eventsEmitter);
    }

    @Override
    public Metadata buildMetadata(JobCloudEvent<?> event) {
        return Metadata.of(OUTGOING_HTTP_METADATA.get());
    }

    @Override
    protected String getAddonName() {
        return KOGITO_ADDON;
    }
}
