/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addon.cloudevents.spring;

import org.kie.kogito.event.CloudEventDataFactory;
import org.kie.kogito.event.CloudEventMarshaller;
import org.kie.kogito.event.EventMarshaller;
import org.kie.kogito.event.impl.JacksonCloudEventDataFactory;
import org.kie.kogito.event.impl.StringCloudEventMarshaller;
import org.kie.kogito.event.impl.StringEventMarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class EventMarshallerProducer {

    @Autowired
    ObjectMapper mapper;

    @Bean
    public EventMarshaller<String> stringEventMarshaller() {
        return new StringEventMarshaller(mapper);
    }

    @Bean
    public CloudEventMarshaller<String> stringCloudEventMarshaller() {
        return new StringCloudEventMarshaller(mapper);
    }

    @Bean
    public CloudEventDataFactory defaulfCloudEventFactory() {
        return new JacksonCloudEventDataFactory(mapper);
    }
}
