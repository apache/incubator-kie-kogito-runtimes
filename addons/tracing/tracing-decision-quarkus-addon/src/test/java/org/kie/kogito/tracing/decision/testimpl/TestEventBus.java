package org.kie.kogito.tracing.decision.testimpl;

import java.util.LinkedList;
import java.util.List;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryContext;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import org.kie.dmn.feel.util.Pair;

public class TestEventBus implements EventBus {

    private final List<Pair<String, Object>> calls = new LinkedList<>();

    public List<Pair<String, Object>> getCalls() {
        return calls;
    }

    @Override
    public EventBus send(String s, @Nullable Object o) {
        calls.add(new Pair<>(s, o));
        return this;
    }

    @Override
    public <T> EventBus send(String s, @Nullable Object o, Handler<AsyncResult<Message<T>>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventBus send(String s, @Nullable Object o, DeliveryOptions deliveryOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EventBus send(String s, @Nullable Object o, DeliveryOptions deliveryOptions, Handler<AsyncResult<Message<T>>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventBus publish(String s, @Nullable Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventBus publish(String s, @Nullable Object o, DeliveryOptions deliveryOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MessageConsumer<T> consumer(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MessageConsumer<T> consumer(String s, Handler<Message<T>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MessageConsumer<T> localConsumer(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MessageConsumer<T> localConsumer(String s, Handler<Message<T>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MessageProducer<T> sender(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MessageProducer<T> sender(String s, DeliveryOptions deliveryOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MessageProducer<T> publisher(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> MessageProducer<T> publisher(String s, DeliveryOptions deliveryOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventBus registerCodec(MessageCodec messageCodec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventBus unregisterCodec(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EventBus registerDefaultCodec(Class<T> aClass, MessageCodec<T, ?> messageCodec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventBus unregisterDefaultCodec(Class aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start(Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EventBus addOutboundInterceptor(Handler<DeliveryContext<T>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EventBus removeOutboundInterceptor(Handler<DeliveryContext<T>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EventBus addInboundInterceptor(Handler<DeliveryContext<T>> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> EventBus removeInboundInterceptor(Handler<DeliveryContext<T>> handler) {
        throw new UnsupportedOperationException();
    }

}
