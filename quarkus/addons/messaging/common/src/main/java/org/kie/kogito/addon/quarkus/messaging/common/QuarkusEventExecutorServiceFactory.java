/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.addon.quarkus.messaging.common;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.event.EventExecutorServiceFactory;
import org.kie.kogito.event.KogitoEmitterController;
import org.kie.kogito.event.KogitoEventExecutor;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class QuarkusEventExecutorServiceFactory implements EventExecutorServiceFactory {

    @ConfigProperty(name = KogitoEventExecutor.MAX_THREADS_PROPERTY, defaultValue = KogitoEventExecutor.DEFAULT_MAX_THREADS)
    int numThreads;

    @ConfigProperty(name = KogitoEventExecutor.QUEUE_SIZE_PROPERTY, defaultValue = KogitoEventExecutor.DEFAULT_QUEUE_SIZE)
    int queueSize;

    @Inject
    KogitoEmitterController emitterStatus;

    public ExecutorService getExecutorService(String channelName) {
        return KogitoEventExecutor.getEventExecutor(numThreads, queueSize, Optional.of(emitterStatus), channelName);
    }
}
