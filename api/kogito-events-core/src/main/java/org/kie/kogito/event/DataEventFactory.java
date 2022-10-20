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
package org.kie.kogito.event;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.kie.kogito.correlation.CompositeCorrelation;
import org.kie.kogito.event.impl.CloudEventWrapDataEvent;
import org.kie.kogito.event.process.ProcessDataEvent;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;

import static org.kie.kogito.internal.process.runtime.KogitoProcessInstance.STATE_ABORTED;
import static org.kie.kogito.internal.process.runtime.KogitoProcessInstance.STATE_ACTIVE;
import static org.kie.kogito.internal.process.runtime.KogitoProcessInstance.STATE_COMPLETED;
import static org.kie.kogito.internal.process.runtime.KogitoProcessInstance.STATE_ERROR;
import static org.kie.kogito.internal.process.runtime.KogitoProcessInstance.STATE_PENDING;
import static org.kie.kogito.internal.process.runtime.KogitoProcessInstance.STATE_SUSPENDED;

public class DataEventFactory {

    public static <T> DataEvent<T> from(T event) {
        return new ProcessDataEvent<>(event);
    }

    public static <T> DataEvent<T> from(CloudEvent event, Converter<CloudEventData, T> unmarshaller) {
        return new CloudEventWrapDataEvent<>(event, unmarshaller);
    }

    public static <T> DataEvent<T> from(CloudEvent event) {
        return new CloudEventWrapDataEvent<>(event);
    }

    private DataEventFactory() {
    }

    public static <T> DataEvent<T> from(T eventData, String trigger, KogitoProcessInstance pi) {
        ProcessDataEvent<T> ce = new ProcessDataEvent<>(eventData);
        ce.setSpecVersion(SpecVersion.V1);
        ce.setId(UUID.randomUUID().toString());
        ce.setType(trigger);
        ce.setSource(URI.create("/process/" + pi.getProcessId()));
        ce.setTime(OffsetDateTime.now());
        ce.setId(UUID.randomUUID().toString());

        ce.setKogitoParentProcessInstanceId(pi.getParentProcessInstanceId());

        ce.setKogitoRootProcessId(pi.getRootProcessId());
        ce.setKogitoParentProcessInstanceId(pi.getRootProcessInstanceId());
        ce.setKogitoReferenceId(pi.getReferenceId());
        ce.setKogitoBusinessKey(pi.getBusinessKey());
        ce.setKogitoProcessInstanceId(pi.getId());
        ce.setKogitoProcessId(pi.getProcessId());
        ce.setKogitoProcessType(pi.getProcess().getType());
        ce.setKogitoProcessInstanceVersion(pi.getProcess().getVersion());
        ce.setKogitoProcessInstanceState(fromState(pi.getState()));

        //setting correlation as extension attributes
        pi.unwrap().correlation()
                .stream()
                .map(c -> CompositeCorrelation.class.isInstance(c) ? CompositeCorrelation.class.cast(c).getValue() : Collections.singleton(c))
                .flatMap(Set::stream)
                .forEach(c -> ce.addExtensionAttribute(c.getKey(), c.getValue()));
        return ce;
    }

    private static String fromState(int state) {
        switch (state) {
            case STATE_ABORTED:
                return "Aborted";
            case STATE_ACTIVE:
                return "Active";
            case STATE_COMPLETED:
                return "Completed";
            case STATE_ERROR:
                return "Error";
            case STATE_PENDING:
                return "Pending";
            case STATE_SUSPENDED:
                return "Suspended";
            default:
                return null;
        }
    }
}
