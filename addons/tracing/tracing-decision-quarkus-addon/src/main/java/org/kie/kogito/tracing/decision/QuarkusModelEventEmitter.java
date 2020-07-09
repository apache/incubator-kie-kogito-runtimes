/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tracing.decision;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.runtime.Startup;
import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.kie.kogito.Application;
import org.kie.kogito.tracing.decision.event.CloudEventUtils;
import org.kie.kogito.tracing.decision.event.model.ModelEvent;
import org.reactivestreams.Publisher;

@Startup
@Singleton
public class QuarkusModelEventEmitter implements EventEmitter {

    private final Application application;
    private final PublishSubject<String> eventSubject;

    @Inject
    public QuarkusModelEventEmitter(final Application application) {
        this.application = application;
        this.eventSubject = PublishSubject.create();
    }

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

    @Outgoing("kogito-tracing-model")
    public Publisher<String> getEventPublisher() {
        return eventSubject.toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public void emit(final String payload) {
        eventSubject.onNext(payload);
    }
}
