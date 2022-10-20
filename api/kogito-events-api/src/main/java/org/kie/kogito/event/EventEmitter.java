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

/**
 * Generic event emitter. Implementation is responsible to interact with the external event service and
 * transform the data event into the format expected by the external service.
 */
public interface EventEmitter {
    /**
     * Publish the cloud event object, properly transformed, into an external event service.
     * 
     * @param dataEvent The DataEvent
     */
    CompletionStage<?> emit(DataEvent<?> dataEvent);
}
