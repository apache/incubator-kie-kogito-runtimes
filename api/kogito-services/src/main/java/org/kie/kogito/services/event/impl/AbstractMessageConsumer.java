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
package org.kie.kogito.services.event.impl;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.event.EventDispatcher;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.EventUnmarshaller;
import org.kie.kogito.event.SubscriptionInfo;
import org.kie.kogito.event.SubscriptionInfo.SubscriptionInfoBuilder;
import org.kie.kogito.event.process.ProcessDataEvent;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessageConsumer<M extends Model, D> {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractMessageConsumer.class);

    private String trigger;
    private EventDispatcher<M> eventDispatcher;

    // in general we should favor the non-empty constructor
    // but there is an issue with Quarkus https://github.com/quarkusio/quarkus/issues/2949#issuecomment-513017781
    // use this in conjuction with setParams()
    public AbstractMessageConsumer() {
    }

    public AbstractMessageConsumer(Application application,
            Process<M> process,
            String trigger,
            EventReceiver eventReceiver,
            Class<D> dataClass,
            boolean useCloudEvents,
            ProcessService processService,
            ExecutorService executorService,
            EventUnmarshaller<Object> eventUnmarshaller) {
        init(application, process, trigger, eventReceiver, dataClass, useCloudEvents, processService, executorService, eventUnmarshaller, null);
    }

    public void init(Application application,
            Process<M> process,
            String trigger,
            EventReceiver eventReceiver,
            Class<D> dataClass,
            boolean useCloudEvents,
            ProcessService processService,
            ExecutorService executorService,
            EventUnmarshaller<Object> eventUnmarshaller,
            Set<String> correlations) {
        this.trigger = trigger;
        this.eventDispatcher = new ProcessEventDispatcher<>(process, getModelConverter().orElse(null), processService, executorService, correlations, getDataResolver(useCloudEvents));
        SubscriptionInfoBuilder builder = SubscriptionInfo.builder().converter(eventUnmarshaller).type(trigger);
        if (useCloudEvents) {
            builder.outputClass(ProcessDataEvent.class).parametrizedClasses(dataClass);
        } else {
            builder.outputClass(dataClass);
        }
        eventReceiver.subscribe(this::consume, builder.createSubscriptionInfo());
        logger.info("Consumer for {} started", trigger);
    }

    protected UnaryOperator<Object> getDataResolver(boolean useCloudEvents) {
        return useCloudEvents ? AbstractMessageConsumer::cloudEventResolver : AbstractMessageConsumer::eventResolver;
    }

    protected static Object cloudEventResolver(Object object) {
        return ((ProcessDataEvent) object).getData();
    }

    protected static Object eventResolver(Object object) {
        return object;
    }

    private CompletionStage<?> consume(Object payload) {
        logger.trace("Received {} for trigger {}", payload, trigger);
        return eventDispatcher.dispatch(trigger, payload)
                .thenAccept(v -> logger.trace("Consume completed {} for trigger {}", payload, trigger));
    }

    protected Optional<Function<Object, M>> getModelConverter() {
        return Optional.empty();
    }
}
