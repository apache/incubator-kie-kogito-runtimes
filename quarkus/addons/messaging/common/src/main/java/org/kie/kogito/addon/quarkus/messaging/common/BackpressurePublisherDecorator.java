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

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.event.KogitoEmitterStatus;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.AbstractMultiOperator;
import io.smallrye.mutiny.operators.multi.MultiOperatorProcessor;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import io.smallrye.reactive.messaging.providers.PublisherDecorator;

@ApplicationScoped
public class BackpressurePublisherDecorator implements PublisherDecorator {

    @Override
    public Multi<? extends Message<?>> decorate(Multi<? extends Message<?>> publisher, String channelName) {
        return publisher.plug(BackpressureOperator::new);
    }

    private static class BackpressureOperator extends AbstractMultiOperator<Message<?>, Message<?>> {

        public BackpressureOperator(Multi<? extends Message<?>> upstream) {
            super(upstream);
        }

        @Override
        public void subscribe(MultiSubscriber<? super Message<?>> downstream) {
            upstream.subscribe().withSubscriber(new BackpressureProcessor(downstream));
        }
    }

    private static class BackpressureProcessor extends MultiOperatorProcessor<Message<?>, Message<?>> {

        public BackpressureProcessor(MultiSubscriber<? super Message<?>> downstream) {
            super(downstream);
        }

        @Override
        public void request(final long n) {
            if (KogitoEmitterStatus.getStatus()) {
                super.request(n);
            }
        }
    }

}
