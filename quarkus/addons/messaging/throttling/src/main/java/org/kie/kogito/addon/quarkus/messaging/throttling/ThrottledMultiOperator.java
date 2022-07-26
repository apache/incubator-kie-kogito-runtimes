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
package org.kie.kogito.addon.quarkus.messaging.throttling;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.event.KogitoEventExecutor;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.AbstractMultiOperator;
import io.smallrye.mutiny.operators.multi.MultiOperatorProcessor;
import io.smallrye.mutiny.subscription.MultiSubscriber;

public class ThrottledMultiOperator extends AbstractMultiOperator<Message<?>, Message<?>> {

    private final int limit;

    private final AtomicInteger processedMessageCount = new AtomicInteger(0);

    public ThrottledMultiOperator(Multi<? extends Message<?>> upstream) {
        super(upstream);
        final Config config = ConfigProvider.getConfig();
        final int factor = config
                .getOptionalValue("kogito.quarkus.events.throttling.threadQueueSizeFactor", Integer.class)
                .orElse(2);
        this.limit =
                config.getOptionalValue(KogitoEventExecutor.MAX_THREADS_PROPERTY, Integer.class)
                        .orElse(KogitoEventExecutor.DEFAULT_MAX_THREADS_INT)
                        +
                        (config.getOptionalValue(KogitoEventExecutor.QUEUE_SIZE_PROPERTY, Integer.class)
                                .orElse(KogitoEventExecutor.DEFAULT_QUEUE_SIZE_INT) / factor);
    }

    @Override
    public void subscribe(MultiSubscriber<? super Message<?>> downstream) {
        upstream.subscribe().withSubscriber(new CustomProcessor(downstream));
    }

    private class CustomProcessor extends MultiOperatorProcessor<Message<?>, Message<?>> {

        CustomProcessor(MultiSubscriber<? super Message<?>> downstream) {
            super(downstream);
        }

        @Override
        public void onItem(Message<?> item) {
            processedMessageCount.incrementAndGet();
            super.onItem(new MessageProxy<>(item, (r, ex) -> {
                if (processedMessageCount.decrementAndGet() == limit) {
                    super.request(1);
                }
            }));
        }

        @Override
        public void request(final long n) {
            if (processedMessageCount.get() <= limit) {
                super.request(1);
            }
        }
    }
}
