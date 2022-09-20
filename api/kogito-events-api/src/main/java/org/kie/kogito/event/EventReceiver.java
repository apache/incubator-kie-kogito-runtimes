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
package org.kie.kogito.event;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Event receiver interface
 */
public interface EventReceiver {

    /**
     * Subscribe an event consumer for a receiver
     * 
     * @param consumer consumer function that accepts the model object and return a completion state with the result of the consumption.
     * @param converted function responsible for converting the object received from the external event source into a model object.
     */
    <S, T> void subscribe(Function<T, CompletionStage<?>> consumer, Unmarshaller<S, T> unmarshaller);
}
