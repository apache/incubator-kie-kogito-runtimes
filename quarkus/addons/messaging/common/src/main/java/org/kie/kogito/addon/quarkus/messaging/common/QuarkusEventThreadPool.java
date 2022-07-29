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

import java.util.Deque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kie.kogito.event.KogitoEventStreams;
import org.kie.kogito.event.KogitoThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuarkusEventThreadPool extends ThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(QuarkusEventThreadPool.class);

    private final Deque<Runnable> overflowBuffer = new ConcurrentLinkedDeque<>();
    private final QuarkusEmitterController kogitoEmitter;
    private final String channelName;

    public QuarkusEventThreadPool(int numThreads, int queueSize, QuarkusEmitterController kogitoEmitter, String channelName) {
        super(1, numThreads, 1L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(queueSize));
        setThreadFactory(new KogitoThreadPoolFactory(KogitoEventStreams.THREAD_NAME));
        setRejectedExecutionHandler(new NonBlockingRejectedExecutionHandler());
        this.kogitoEmitter = kogitoEmitter;
        this.channelName = channelName;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Runnable queued = overflowBuffer.pollFirst();
        if (queued != null) {
            if (overflowBuffer.isEmpty()) {
                logger.trace("Resuming emission");
                kogitoEmitter.resume(channelName);
            }
            logger.trace("Trying to add runnable {} back to the queue", queued);
            super.submit(queued);
            logger.trace("Runnable {} added back to the queue", queued);
        }

    }

    private class NonBlockingRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                logger.trace("Rejecting runnable {}", r);
                kogitoEmitter.stop(channelName);
                overflowBuffer.addLast(r);
            }
        }
    }
}
