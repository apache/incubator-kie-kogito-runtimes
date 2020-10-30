/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tracing.decision;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vertx.core.eventbus.EventBus;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This class must always extend <code>org.kie.kogito.tracing.decision.DecisionTracingListener</code>
 * for code generation plugins to correctly detect if this addon is enabled.
 */
@ApplicationScoped
public final class QuarkusDecisionTracingListener extends DecisionTracingListener {

    @Inject
    public QuarkusDecisionTracingListener(
            EventBus bus,
            QuarkusDecisionTracingCollector collector,
            @ConfigProperty(name = "kogito.addon.tracing.decision.asyncEnabled", defaultValue = "true") boolean asyncEnabled
    ) {
        if (asyncEnabled) {
            setEventConsumer(event -> bus.send("kogito-tracing-decision_EvaluateEvent", event));
        } else {
            setEventConsumer(collector::onEvent);
        }
    }
}
