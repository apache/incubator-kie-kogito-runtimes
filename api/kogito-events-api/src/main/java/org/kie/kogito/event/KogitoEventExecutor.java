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
package org.kie.kogito.event;

import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KogitoEventExecutor {

    public static final String MAX_THREADS_PROPERTY = "kogito.quarkus.events.threads.poolSize";
    public static final int DEFAULT_MAX_THREADS_INT = 10;
    public static final String DEFAULT_MAX_THREADS = "10";
    public static final int DEFAULT_QUEUE_SIZE_INT = 1;
    public static final String DEFAULT_QUEUE_SIZE = "1";
    public static final String QUEUE_SIZE_PROPERTY = "kogito.quarkus.events.threads.queueSize";
    public static final String BEAN_NAME = "kogito-event-executor";
    public static final String THREAD_NAME = "kogito-event-executor";

    public static ExecutorService getEventExecutor() {
        return getEventExecutor(DEFAULT_MAX_THREADS_INT, DEFAULT_QUEUE_SIZE_INT);
    }

    public static ExecutorService getEventExecutor(int numOfThreads, int blockQueueSize) {
        return getEventExecutor(numOfThreads, blockQueueSize, Optional.empty(), null);
    }

    public static ExecutorService getEventExecutor(int numOfThreads, int blockQueueSize, Optional<KogitoEmitterController> kogitoEmitter, String channelName) {
        return kogitoEmitter.<ExecutorService> map(emitter -> new KogitoThreadPoolExecutor(numOfThreads, blockQueueSize, emitter, channelName))
                .orElse(new ThreadPoolExecutor(1, numOfThreads, 1L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(blockQueueSize), new KogitoThreadFactory(THREAD_NAME),
                        new ThreadPoolExecutor.CallerRunsPolicy()));
    }

    private KogitoEventExecutor() {
    }

    private static class KogitoThreadPoolExecutor extends ThreadPoolExecutor {

        private static final Logger logger = LoggerFactory.getLogger(KogitoThreadPoolExecutor.class);

        private final Deque<Runnable> overflowBuffer = new ConcurrentLinkedDeque<>();
        private final KogitoEmitterController kogitoEmitter;
        private final String channelName;

        public KogitoThreadPoolExecutor(int numThreads, int queueSize, KogitoEmitterController kogitoEmitter, String channelName) {
            super(1, numThreads, 1L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(queueSize));
            setThreadFactory(new KogitoThreadFactory(THREAD_NAME));
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

    private static class KogitoThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);
        private String threadNamePrefix;

        public KogitoThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            String threadName = threadNamePrefix + "-" + counter.getAndIncrement();
            Thread th = new Thread(r);
            th.setName(threadName);
            th.setDaemon(true);
            return th;
        }
    }
}
