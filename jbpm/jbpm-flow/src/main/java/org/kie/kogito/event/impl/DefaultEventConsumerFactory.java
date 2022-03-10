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
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.kie.kogito.Model;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventConsumer;
import org.kie.kogito.event.EventConsumerFactory;
import org.kie.kogito.process.ProcessService;

public class DefaultEventConsumerFactory implements EventConsumerFactory {

    @Override
    public <M extends Model, D> EventConsumer<M, D> get(ProcessService processService, ExecutorService executorService, Optional<Function<Object, M>> modelConverter, boolean cloudEvents,
            Function<DataEvent<D>, D> dataFunction) {
        return cloudEvents
                ? (EventConsumer<M, D>) new CloudEventConsumer<>(processService, executorService, modelConverter, dataFunction.compose(c -> c))
                : new DataEventConsumer<>(processService, executorService, modelConverter);
    }

}
