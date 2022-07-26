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

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.providers.PublisherDecorator;

@ApplicationScoped
public class ThrottlingPublisherDecorator implements PublisherDecorator {

    @Override
    public Multi<? extends Message<?>> decorate(Multi<? extends Message<?>> multi, String s) {
        final boolean isEventsThrottlingEnabled = ConfigProvider.getConfig()
                .getOptionalValue("kogito.quarkus.events.throttling.enabled", Boolean.class)
                .orElse(Boolean.TRUE);
        if (isEventsThrottlingEnabled) {
            return multi.plug(ThrottledMultiOperator::new);
        }
        return multi;
    }
}
