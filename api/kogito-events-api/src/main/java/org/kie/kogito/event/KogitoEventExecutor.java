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
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        return getEventExecutor(numOfThreads, blockQueueSize, KogitoEventExecutor.THREAD_NAME);
    }

    public static ExecutorService getEventExecutor(int numOfThreads, int blockQueueSize, String threadNamePrefix) {
        return new KogitoThreadPoolExecutor(numOfThreads, blockQueueSize, threadNamePrefix);
    }

    private KogitoEventExecutor() {
    }

    private static class KogitoThreadPoolExecutor extends ThreadPoolExecutor {

        private final Deque<Runnable> overflowBuffer = new LinkedList<>();

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            Runnable queued;
            synchronized (overflowBuffer) {
                queued = overflowBuffer.pollFirst();
                if (queued != null && !getQueue().offer(queued)) {
                    overflowBuffer.addFirst(queued);
                }
            }
            if (queued == null) {
                KogitoEmitterStatus.setStatus(true);
            }
        }

        public KogitoThreadPoolExecutor(int numThreads, int queueSize, final String threadNamePrefix) {
            super(1, numThreads, 1L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(queueSize));
            setThreadFactory(new KogitoThreadFactory(threadNamePrefix));
            setRejectedExecutionHandler(new NonBlockingRejectedExecutionHandler());
        }

        private class NonBlockingRejectedExecutionHandler implements RejectedExecutionHandler {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (!executor.isShutdown()) {
                    KogitoEmitterStatus.setStatus(false);
                    overflowBuffer.addLast(r);
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
}
