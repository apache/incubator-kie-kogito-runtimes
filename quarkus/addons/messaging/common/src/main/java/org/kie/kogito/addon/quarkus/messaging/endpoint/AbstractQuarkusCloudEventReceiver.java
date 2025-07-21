/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.addon.quarkus.messaging.endpoint;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.addon.quarkus.messaging.common.QuarkusCloudEventConverter;
import org.kie.kogito.addon.quarkus.messaging.common.QuarkusDataEventConverter;
import org.kie.kogito.event.CloudEventUnmarshallerFactory;
import org.kie.kogito.event.Converter;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.EventUnmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQuarkusCloudEventReceiver<I> implements EventReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractQuarkusCloudEventReceiver.class);

    private EventUnmarshaller<I> eventDataUnmarshaller;

    private CloudEventUnmarshallerFactory<I> cloudEventUnmarshaller;

    protected void setEventDataUnmarshaller(EventUnmarshaller<I> eventDataUnmarshaller) {
        this.eventDataUnmarshaller = eventDataUnmarshaller;
    }

    protected void setCloudEventUnmarshaller(CloudEventUnmarshallerFactory<I> cloudEventUnmarshaller) {
        this.cloudEventUnmarshaller = cloudEventUnmarshaller;
    }

    protected <T> Converter<Message<I>, DataEvent<T>> getConverter(Class<T> objectClass) {
        if (cloudEventUnmarshaller != null) {
            return new QuarkusCloudEventConverter<>(cloudEventUnmarshaller.unmarshaller(objectClass));
        } else if (eventDataUnmarshaller != null) {
            return new QuarkusDataEventConverter<>(objectClass, eventDataUnmarshaller);
        } else {
            throw new IllegalStateException("No unmarshaller set for receiver " + this);
        }
    }
}
