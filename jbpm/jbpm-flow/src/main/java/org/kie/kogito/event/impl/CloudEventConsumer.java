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
package org.kie.kogito.event.impl;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.event.EventConsumer;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessService;
import org.kie.kogito.services.event.ProcessDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudEventConsumer<M extends Model, D, E extends ProcessDataEvent<D>> implements EventConsumer<M, E> {

    private static final Logger logger = LoggerFactory.getLogger(CloudEventConsumer.class);

    private Optional<Function<Object, M>> modelConverter;
    private ProcessService processService;
    private ExecutorService executor;
    private Function<E, D> dataFunction;
    private KogitoReferenceCorrelationResolver kogitoReferenceCorrelationResolver = new KogitoReferenceCorrelationResolver();
    private ProcessEventDispatcher<M> processEventDispatcher;

    public CloudEventConsumer(ProcessService processService, ExecutorService executor, Optional<Function<Object, M>> modelConverter, Function<E, D> dataFunction) {
        this.processService = processService;
        this.executor = executor;
        this.modelConverter = modelConverter;
        this.dataFunction = dataFunction;

    }

    @Override
    public CompletionStage<?> consume(Application application, Process<M> process, E cloudEvent, String trigger) {
        //todo
        processEventDispatcher = new ProcessEventDispatcher<M>(process, modelConverter.get(), processService);
        return processEventDispatcher.dispatch(trigger, cloudEvent);
    }
}
