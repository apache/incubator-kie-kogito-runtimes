/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.kie.kogito.event.CloudEventUnmarshaller;
import org.kie.kogito.event.CloudEventUnmarshallerFactory;
import org.kie.kogito.event.Converter;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.Subscription;
import org.kie.kogito.event.impl.CloudEventConverter;
import org.kie.kogito.event.impl.JacksonCloudEventDataConverter;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;

public class KafkaEventReceiver implements EventReceiver {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventReceiver.class);
    private final Collection<Subscription<?, CloudEvent>> subscriptions = new ArrayList<>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void onEvent(CloudEvent value) {
        for (Subscription subscription : subscriptions) {
            try {
                subscription.getConsumer().apply(subscription.getConverter().convert(value));
            } catch (IOException e) {
                logger.info("Problem deserializing event {}", value, e);
            }
        }
    }

    @Override
    public <T> void subscribe(Function<DataEvent<T>, CompletionStage<?>> consumer, Class<T> dataClass) {
        subscriptions.add(new Subscription<>(consumer, new CloudEventConverter<>(dataClass, new CloudEventUnmarshallerFactory<CloudEvent>() {
            @Override
            public <S> CloudEventUnmarshaller<CloudEvent, S> unmarshaller(Class<S> targetClass) {
                return new CloudEventUnmarshaller<CloudEvent, S>() {
                    @Override
                    public Converter<CloudEvent, CloudEvent> cloudEvent() {
                        return c -> c;
                    }

                    @Override
                    public Converter<CloudEvent, CloudEventData> binaryCloudEvent() {
                        return c -> c.getData();
                    }

                    @Override
                    public Converter<CloudEventData, S> data() {
                        return new JacksonCloudEventDataConverter<>(ObjectMapperFactory.listenerAware(), targetClass);
                    }
                };
            }
        })));
    }
}
