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

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

public class MessageProxy<T> implements Message<T> {

    private final Message<T> base;
    private final BiConsumer<? super Void, ? super Throwable> callback;

    public MessageProxy(
            final Message<T> base,
            final BiConsumer<? super Void, ? super Throwable> callback) {
        this.base = base;
        this.callback = callback;
    }

    @Override
    public <P> Message<P> withPayload(P payload) {
        return base.withPayload(payload);
    }

    @Override
    public Message<T> withMetadata(Iterable<Object> metadata) {
        return base.withMetadata(metadata);
    }

    @Override
    public Message<T> withMetadata(Metadata metadata) {
        return base.withMetadata(metadata);
    }

    @Override
    public Message<T> withAck(Supplier<CompletionStage<Void>> supplier) {
        return base.withAck(supplier);
    }

    @Override
    public Message<T> withNack(Function<Throwable, CompletionStage<Void>> nack) {
        return base.withNack(nack);
    }

    @Override
    public T getPayload() {
        return base.getPayload();
    }

    @Override
    public Metadata getMetadata() {
        return base.getMetadata();
    }

    @Override
    public <M> Optional<M> getMetadata(Class<? extends M> clazz) {
        return base.getMetadata(clazz);
    }

    @Override
    public Supplier<CompletionStage<Void>> getAck() {
        return base.getAck();
    }

    @Override
    public Function<Throwable, CompletionStage<Void>> getNack() {
        return base.getNack();
    }

    @Override
    public CompletionStage<Void> ack() {
        return base.ack().whenComplete(callback);
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason) {
        return base.nack(reason).whenComplete(callback);
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, Metadata metadata) {
        return base.nack(reason, metadata).whenComplete(callback);
    }

    @Override
    public <C> C unwrap(Class<C> unwrapType) {
        return base.unwrap(unwrapType);
    }

    @Override
    public Message<T> addMetadata(Object metadata) {
        return base.addMetadata(metadata);
    }
}
