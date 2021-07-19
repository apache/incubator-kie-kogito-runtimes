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

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.kie.kogito.Model;
import org.kie.kogito.process.ProcessService;
import org.kie.kogito.services.event.EventConsumer;
import org.kie.kogito.services.event.EventConsumerFactory;

public class DefaultEventConsumerFactory implements EventConsumerFactory {

    @Override
    public <M extends Model, D> EventConsumer<M> get(ProcessService processService, ExecutorService executorService, Function<D, M> function, boolean cloudEvents) {
        return cloudEvents
                ? new CloudEventConsumer<>(processService, executorService, function)
                : new DataEventConsumer<>(processService, executorService, function);
    }

}
