/*
 *  Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package $Package$;

import java.util.concurrent.ExecutorService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.event.KogitoEventExecutor;
import org.kie.kogito.services.async.ProcessAsyncExecutor;

@ApplicationScoped
public class AsyncExecutorProducer {

    @ConfigProperty(name = KogitoEventExecutor.MAX_THREADS_PROPERTY, defaultValue = KogitoEventExecutor.DEFAULT_MAX_THREADS)
    int numThreads;

    @ConfigProperty(name = KogitoEventExecutor.QUEUE_SIZE_PROPERTY, defaultValue = KogitoEventExecutor.DEFAULT_QUEUE_SIZE)
    int queueSize;

    @Produces
    public ProcessAsyncExecutor getExecutorService() {
        return new ProcessAsyncExecutor(KogitoEventExecutor.getEventExecutor(numThreads, queueSize, ProcessAsyncExecutor.THREAD_PREFIX));
    }

    public void close(@Disposes ExecutorService executor) {
        executor.shutdownNow();
    }
}