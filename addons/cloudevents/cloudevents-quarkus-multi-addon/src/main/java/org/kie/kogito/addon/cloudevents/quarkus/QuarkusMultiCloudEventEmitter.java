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
package org.kie.kogito.addon.cloudevents.quarkus;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.addon.cloudevents.quarkus.decorators.MessageDecorator;
import org.kie.kogito.addon.cloudevents.quarkus.decorators.MessageDecoratorFactory;
import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.event.EventEmitter;
import org.kie.kogito.event.EventMarshaller;
import org.kie.kogito.event.impl.DefaultEventMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;
import io.smallrye.reactive.messaging.ChannelRegistry;
import io.smallrye.reactive.messaging.extension.EmitterConfiguration;
import io.smallrye.reactive.messaging.extension.MediatorManager;

@Startup(0)
public class QuarkusMultiCloudEventEmitter implements EventEmitter {

    private static final Pattern OUTGOING_PATTERN = Pattern.compile("mp\\.messaging\\.outgoing\\.([a-zA-Z].*)\\..*");
    private static Logger logger = LoggerFactory.getLogger(QuarkusMultiCloudEventEmitter.class);

    private MessageDecorator messageDecorator;

    @Inject
    private ChannelRegistry channelRegistry;

    @Inject
    private MediatorManager mediatorManager;

    @Inject
    private ConfigBean configBean;

    @PostConstruct
    private void init() {
        QuarkusMultiCloudUtils.getChannels(OUTGOING_PATTERN).stream().map(this::emitterConf).forEach(mediatorManager::addEmitter);
        messageDecorator = MessageDecoratorFactory.newInstance(configBean.useCloudEvents().orElse(true));
    }

    private EmitterConfiguration emitterConf(String name) {
        return new EmitterConfiguration(name, false, null, null);
    }

    // TODO @Inject change when this class is added https://github.com/kiegroup/kogito-runtimes/pull/1195/files#diff-32eeb9c913dc61f24f8b4d27a8dca87f5e907de83b81e210e59bd67193a9cdfd
    EventMarshaller marshaller = new DefaultEventMarshaller();

    @Override
    public <T> CompletionStage<Void> emit(T e, String type, Optional<Function<T, Object>> processDecorator) {
        final Message<String> message = this.messageDecorator.decorate(marshaller.marshall(
                configBean.useCloudEvents().orElse(true) ? processDecorator.map(d -> d.apply(e)).orElse(e) : e));
        Emitter<String> emitter = (Emitter<String>) channelRegistry.getEmitter(type);
        if (emitter != null) {
            emitter.send(message);
        } else {
            logger.warn("Cannot found channel {}. Please add it to application.properties", type);
        }
        return message.getAck().get();
    }
}
