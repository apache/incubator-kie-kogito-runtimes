/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package com.myspace.demo;



import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.event.impl.DefaultEventMarshaller;
import org.kie.kogito.events.knative.ce.decorators.MessageDecorator;
import org.kie.kogito.events.knative.ce.decorators.MessageDecoratorFactory;
import org.kie.kogito.services.event.DataEventAttrBuilder;
import org.kie.kogito.services.event.EventMarshaller;


import java.util.Optional;

public class MessageProducer {

    Object emitter;

    Optional<Boolean> useCloudEvents = Optional.of(true);

    Optional<MessageDecorator> decorator = MessageDecoratorFactory.newInstance();

    EventMarshaller marshaller = new DefaultEventMarshaller();

    public void configure() {

    }

    public void produce(ProcessInstance pi, $Type$ eventData) {
        if (decorator.isPresent()) {
        } else {
        }
    }
    

    private String marshall(ProcessInstance pi, $Type$ eventData) {
        return marshaller.marshall(eventData, e -> new $DataEventType$(
            DataEventAttrBuilder.toType("$channel$", pi),
            DataEventAttrBuilder.toSource(pi),
            e,
            pi.getId(),
            pi.getParentProcessInstanceId(),
            pi.getRootProcessInstanceId(),
            pi.getProcessId(),
            pi.getRootProcessId(),
            String.valueOf(pi.getState()),pi.getReferenceId()), useCloudEvents);
    }
}
