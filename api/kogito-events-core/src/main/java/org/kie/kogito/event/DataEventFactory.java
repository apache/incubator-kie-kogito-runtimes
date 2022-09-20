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

import java.util.Collections;
import java.util.Set;

import org.kie.kogito.correlation.CompositeCorrelation;
import org.kie.kogito.event.impl.CloudEventWrapDataEvent;
import org.kie.kogito.event.process.ProcessDataEvent;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;

public class DataEventFactory {

    public static <T> DataEvent<T> from(T event) {
        return new ProcessDataEvent<T>(event);
    }

    public static <T> DataEvent<T> from(CloudEvent event, Unmarshaller<CloudEventData, T> unmarshaller) {
        return new CloudEventWrapDataEvent<T>(event, unmarshaller);
    }

    public static <T> DataEvent<T> from(T event, String trigger, KogitoProcessInstance pi) {
        // TODO marshall this using CloudEvent api
        ProcessDataEvent<T> cloudEvent = new ProcessDataEvent<>(trigger,
                "",
                event,
                pi.getStringId(),
                pi.getProcess().getVersion(),
                pi.getParentProcessInstanceId(),
                pi.getRootProcessInstanceId(),
                pi.getProcessId(),
                pi.getRootProcessId(),
                String.valueOf(pi.getState()),
                null,
                pi.getProcess().getType(),
                pi.getReferenceId());
        //setting correlation as extension attributes
        pi.unwrap().correlation()
                .stream()
                .map(c -> CompositeCorrelation.class.isInstance(c) ? CompositeCorrelation.class.cast(c).getValue() : Collections.singleton(c))
                .flatMap(Set::stream)
                .forEach(c -> cloudEvent.addExtensionAttribute(c.getKey(), c.getValue()));
        return cloudEvent;
    }
}
