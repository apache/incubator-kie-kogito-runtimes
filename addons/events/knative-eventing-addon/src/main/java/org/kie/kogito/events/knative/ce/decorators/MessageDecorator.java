/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.events.knative.ce.decorators;

import org.eclipse.microprofile.reactive.messaging.Message;

/**
 * {@link MessageDecorator}s decorates the {@link Message} envelope with metadata and additional information in a given context.
 */
public interface MessageDecorator {

    /**
     * Decorates the given reactive message
     *
     * @param payload payload to decorate
     * @param <T>     payload type
     * @return payload in Message format decorated
     */
    <T> Message<T> decorate(T payload);

}
