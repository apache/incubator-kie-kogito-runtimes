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
package org.kie.kogito.addon.cloudevents.quarkus.decorators;

import org.eclipse.microprofile.reactive.messaging.Message;

/**
 * Creates a Microprofile message without adding any metadata on top of the given payload.
 */
public class NoOpMessageDecorator implements MessageDecorator {

    @Override
    public <T> Message<T> decorate(T payload) {
        return Message.of(payload);
    }
}
