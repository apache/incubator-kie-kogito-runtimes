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
package org.kie.kogito.addon.cloudevents.quarkus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment.Strategy;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.event.EventReceiver;
import org.kie.kogito.event.InputTriggerAware;
import org.kie.kogito.event.SubscriptionInfo;

import io.smallrye.reactive.messaging.ChannelRegistar;
import io.smallrye.reactive.messaging.DefaultMediatorConfiguration;
import io.smallrye.reactive.messaging.MediatorConfiguration;
import io.smallrye.reactive.messaging.Shape;
import io.smallrye.reactive.messaging.annotations.Merge;
import io.smallrye.reactive.messaging.annotations.Merge.Mode;
import io.smallrye.reactive.messaging.extension.MediatorManager;

@ApplicationScoped
public class QuarkusMultiCloudEventReceiver implements ChannelRegistar, EventReceiver {

    @Inject
    private Instance<InputTriggerAware> channels;
    @Inject
    private MediatorManager mediatorManager;
    @Inject
    private BeanManager beanManager;

    private Bean<?> getBean(InputTriggerAware channel) {
        Set<Bean<?>> beans = beanManager.getBeans(InputTriggerAware.class);
        for (Bean<?> bean : beans) {
            if (bean.getBeanClass().isAssignableFrom(channel.getClass())) {
                return bean;
            }
        }
        throw new IllegalStateException("No bean found for " + channel.getClass());
    }

    MediatorConfiguration mediatorConf(InputTriggerAware channel) {

        return new DefaultMediatorConfiguration(
                getMethod(channel),
                getBean(channel)) {

            @Override
            public List<String> getIncoming() {
                return Collections.singletonList(channel.getInputTrigger());
            }

            @Override
            public Shape shape() {
                return Shape.SUBSCRIBER;
            }

            @Override
            public Consumption consumption() {
                return Consumption.MESSAGE;
            }

            @Override
            public boolean isBlocking() {
                return false;
            }

            @Override
            public Acknowledgment.Strategy getAcknowledgment() {
                return Strategy.MANUAL;
            }

            @Override
            public Merge.Mode getMerge() {
                return Mode.MERGE;
            }
        };
    }

    private Method getMethod(InputTriggerAware channel) {
        Method[] methods = channel.getClass().getMethods();
        for (Method m : methods) {
            if (m.getParameterCount() == 1 && Message.class.isAssignableFrom(m.getParameterTypes()[0])) {
                return m;
            }
        }
        throw new IllegalStateException("Cannot find method that accept Message as input parameter in " + methods);
    }

    @Override
    public void initialize() {
        Collection<MediatorConfiguration> mediators = new ArrayList<>();
        channels.forEach(channel -> mediators.add(mediatorConf(channel)));
        if (!mediators.isEmpty()) {
            mediatorManager.addAnalyzed(mediators);
        }
    }

    @Override
    public <T> void subscribe(Function<T, CompletionStage<?>> consumer, SubscriptionInfo<String, T> converter) {
        // Automatic subscription
    }

}
