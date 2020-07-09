/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.tracing.decision;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.kie.kogito.Application;
import org.kie.kogito.tracing.decision.event.CloudEventUtils;
import org.kie.kogito.tracing.decision.event.model.ModelEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SpringBootModelEventEmitter implements EventEmitter {

    private final Application application;
    private final KafkaTemplate<String, String> template;
    private final String kafkaTopicName;

    @Autowired
    public SpringBootModelEventEmitter(final Application application,
                                       final KafkaTemplate<String, String> template,
                                       final @Value(value = "${kogito.addon.tracing.decision.kafka.topic.name:kogito-tracing-model}") String kafkaTopicName) {
        this.application = application;
        this.template = template;
        this.kafkaTopicName = kafkaTopicName;
    }

    @Async
    @PostConstruct
    public void publishDecisionModels() {
        application.decisionModels().resources().forEach(resource -> {
            //Fire a new ModelEvent containing the model, name and namespace
            emit(CloudEventUtils.encode(CloudEventUtils.build("id",
                                                              URI.create(URLEncoder.encode(ModelEvent.class.getName(), StandardCharsets.UTF_8)),
                                                              new ModelEvent(ModelEvent.GAV.from(resource.getGav()),
                                                                             resource.getModelName(),
                                                                             resource.getNamespace(),
                                                                             resource.getIdentifier(),
                                                                             resource.getModelType(),
                                                                             resource.get()),
                                                              ModelEvent.class)));
        });
    }

    @Override
    public void emit(final String payload) {
        template.send(kafkaTopicName, payload);
    }
}
